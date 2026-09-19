package com.niko.littlepiggy.player;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.niko.littlepiggy.debug.DebugConfig;

public class PlayerController {

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

        moving = false;
        backflipStartedThisFrame = false;

        backflipCooldownRemaining = Math.max(
                0f,
                backflipCooldownRemaining - delta);

        dashChargeHeld = Gdx.input.isKeyPressed(Input.Keys.UP);

        updateGroundedState();

        handleMovement(movementBlocked);
        handleJump(movementBlocked);
        handleBackflip(movementBlocked);
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
}
