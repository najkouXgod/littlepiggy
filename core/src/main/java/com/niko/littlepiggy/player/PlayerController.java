package com.niko.littlepiggy.player;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.niko.littlepiggy.debug.DebugConfig;

public class PlayerController {

    private enum GroundSlamState {
        IDLE,
        WINDUP,
        FALLING,
        LANDING
    }

    private static final float GROUND_SLAM_COOLDOWN = 2.0f;

    private static final float GROUND_SLAM_WINDUP_TIME = 0.24f;
    private static final float GROUND_SLAM_LANDING_TIME = 0.18f;

    private static final float GROUND_SLAM_FALL_SPEED = 14f;

    private GroundSlamState groundSlamState = GroundSlamState.IDLE;

    private float groundSlamStateTime;
    private float groundSlamCooldownRemaining;

    private boolean groundSlamWasPressed;

    private boolean groundSlamStartedThisFrame;
    private boolean groundSlamLandedThisFrame;

    private static final float BACKFLIP_COOLDOWN = 1.0f;

    private final PlayerPhysics physics;

    private boolean moving;
    private boolean facingLeft;

    private boolean jumpWasPressed;
    private boolean backflipWasPressed;
    private boolean dashChargeHeld;

    private boolean normalJumpUsed;
    private boolean wasGrounded;

    private boolean backflipStartedThisFrame;
    private float backflipCooldownRemaining;

    public PlayerController(PlayerPhysics physics) {
        this.physics = physics;
    }

    public void update(float delta, boolean movementBlocked) {

        groundSlamStartedThisFrame = false;
        groundSlamLandedThisFrame = false;

        groundSlamCooldownRemaining = Math.max(
                0f,
                groundSlamCooldownRemaining - delta);

        moving = false;
        backflipStartedThisFrame = false;

        backflipCooldownRemaining = Math.max(
                0f,
                backflipCooldownRemaining - delta);

        dashChargeHeld = Gdx.input.isKeyPressed(Input.Keys.UP);

        updateGroundedState();
        handleGroundSlam(
                delta,
                movementBlocked);

        if (isGroundSlamActive()) {

            dashChargeHeld = false;

            /*
             * Uppdatera input-state även när movement
             * är låst så att Space/Ctrl inte triggas
             * direkt när slammen är färdig.
             */
            jumpWasPressed = Gdx.input.isKeyPressed(
                    Input.Keys.SPACE);

            backflipWasPressed = Gdx.input.isKeyPressed(
                    Input.Keys.CONTROL_LEFT)
                    || Gdx.input.isKeyPressed(
                            Input.Keys.CONTROL_RIGHT);

            return;
        }

        handleMovement(movementBlocked);
        handleJump(movementBlocked);
        handleBackflip(movementBlocked);
    }

    private void handleGroundSlam(
            float delta,
            boolean movementBlocked) {

        boolean slamPressed = Gdx.input.isKeyPressed(Input.Keys.DOWN)
                || Gdx.input.isKeyPressed(Input.Keys.S);

        switch (groundSlamState) {

            case WINDUP:

                physics.holdGroundSlamWindup();

                groundSlamStateTime += delta;

                if (groundSlamStateTime >= GROUND_SLAM_WINDUP_TIME) {

                    groundSlamState = GroundSlamState.FALLING;

                    groundSlamStateTime = 0f;

                    physics.beginGroundSlamFall(
                            GROUND_SLAM_FALL_SPEED);
                }

                break;

            case FALLING:

                /*
                 * Ground-contacten uppdateras av Box2D
                 * innan Player.update körs.
                 */
                if (physics.isGrounded()) {

                    physics.finishGroundSlam();

                    groundSlamState = GroundSlamState.LANDING;

                    groundSlamStateTime = 0f;

                    groundSlamLandedThisFrame = true;
                }

                break;

            case LANDING:

                /*
                 * Håll spelaren still under de tre
                 * impact-framesen.
                 */
                physics.setHorizontalVelocity(0f);

                groundSlamStateTime += delta;

                if (groundSlamStateTime >= GROUND_SLAM_LANDING_TIME) {

                    groundSlamState = GroundSlamState.IDLE;

                    groundSlamStateTime = 0f;
                }

                break;

            case IDLE:
            default:

                if (slamPressed
                        && !groundSlamWasPressed
                        && !movementBlocked
                        && !physics.isGrounded()
                        && groundSlamCooldownRemaining <= 0f) {

                    groundSlamState = GroundSlamState.WINDUP;

                    groundSlamStateTime = 0f;

                    groundSlamCooldownRemaining = GROUND_SLAM_COOLDOWN;

                    groundSlamStartedThisFrame = true;

                    physics.beginGroundSlamWindup();
                }

                break;
        }

        groundSlamWasPressed = slamPressed;
    }

    private void updateGroundedState() {

        boolean grounded = physics.isGrounded();

        /*
         * Återställ vanligt hopp endast när spelaren
         * faktiskt LANDAR.
         *
         * Inte varje frame som grounded är true.
         *
         * Detta förhindrar att foot-sensorn fortfarande
         * räknas som grounded precis efter ett hopp och
         * råkar ge spelaren ett extra SPACE-hopp.
         */
        if (grounded && !wasGrounded) {
            normalJumpUsed = false;
        }

        wasGrounded = grounded;
    }

    private void handleMovement(boolean movementBlocked) {

        if (movementBlocked) {
            return;
        }

        float velocityX = 0f;

        if (Gdx.input.isKeyPressed(Input.Keys.RIGHT)) {

            velocityX = DebugConfig.SPEED;
            facingLeft = false;
            moving = true;
        }

        if (Gdx.input.isKeyPressed(Input.Keys.LEFT)) {

            velocityX = -DebugConfig.SPEED;
            facingLeft = true;
            moving = true;
        }

        physics.setHorizontalVelocity(velocityX);
    }

    private void handleJump(boolean movementBlocked) {

        boolean jumpPressed = Gdx.input.isKeyPressed(Input.Keys.SPACE);

        if (jumpPressed
                && !jumpWasPressed
                && !movementBlocked
                && !normalJumpUsed) {

            physics.jump(
                    0f,
                    DebugConfig.JUMP_MINPOWER);

            /*
             * Vanligt hopp är nu förbrukat tills
             * spelaren faktiskt landar igen.
             */
            normalJumpUsed = true;
        }

        jumpWasPressed = jumpPressed;
    }

    private void handleBackflip(boolean movementBlocked) {

        boolean backflipPressed = Gdx.input.isKeyPressed(Input.Keys.CONTROL_LEFT)
                || Gdx.input.isKeyPressed(Input.Keys.CONTROL_RIGHT);

        if (backflipPressed
                && !backflipWasPressed
                && !movementBlocked
                && backflipCooldownRemaining <= 0f) {

            /*
             * Bakåtvolt får göras när som helst:
             * på marken eller i luften.
             *
             * Den påverkar inte normalJumpUsed.
             */
            physics.backflipJump(
                    0f,
                    DebugConfig.BACKFLIP_JUMP_POWER);

            backflipStartedThisFrame = true;
            backflipCooldownRemaining = BACKFLIP_COOLDOWN;
        }

        backflipWasPressed = backflipPressed;
    }

    public boolean isMoving() {
        return moving;
    }

    public boolean isFacingLeft() {
        return facingLeft;
    }

    public boolean isDashChargeHeld() {
        return dashChargeHeld;
    }

    public boolean didStartBackflip() {
        return backflipStartedThisFrame;
    }

    public boolean isBackflipReady() {
        return backflipCooldownRemaining <= 0f;
    }

    public float getBackflipCooldownPercent() {
        return 1f - Math.min(
                1f,
                backflipCooldownRemaining / BACKFLIP_COOLDOWN);
    }

    public boolean didStartGroundSlam() {
        return groundSlamStartedThisFrame;
    }

    public boolean didLandGroundSlam() {
        return groundSlamLandedThisFrame;
    }

    public boolean isGroundSlamFalling() {
        return groundSlamState == GroundSlamState.FALLING;
    }

    public void landGroundSlamOnEnemy() {

        if (groundSlamState != GroundSlamState.FALLING) {
            return;
        }

        physics.finishGroundSlam();

        groundSlamState = GroundSlamState.LANDING;

        groundSlamStateTime = 0f;

        groundSlamLandedThisFrame = true;
    }

    public boolean isGroundSlamActive() {
        return groundSlamState != GroundSlamState.IDLE;
    }

    public boolean isGroundSlamReady() {
        return groundSlamCooldownRemaining <= 0f;
    }

    public float getGroundSlamCooldownPercent() {

        return 1f - Math.min(
                1f,
                groundSlamCooldownRemaining
                        / GROUND_SLAM_COOLDOWN);
    }
}
