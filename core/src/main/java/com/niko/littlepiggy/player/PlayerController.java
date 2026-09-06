package com.niko.littlepiggy.player;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;

import com.niko.littlepiggy.debug.DebugConfig;

public class PlayerController {

    private static final int MAX_JUMPS = 2;

    private final PlayerPhysics physics;

    private boolean moving;
    private boolean facingLeft;

    private boolean jumpWasPressed;
    private boolean dashChargeHeld;
    private boolean wasGrounded;

    private int jumpsUsed;
    private boolean doubleJumpStartedThisFrame;

    public PlayerController(PlayerPhysics physics) {
        this.physics = physics;
    }

    public void update(
            float delta,
            boolean movementBlocked) {

        moving = false;
        doubleJumpStartedThisFrame = false;

        dashChargeHeld = Gdx.input.isKeyPressed(
                Input.Keys.UP);

        updateGroundedState();
        handleMovement(movementBlocked);
        handleJump(movementBlocked);
    }

    private void updateGroundedState() {

        boolean grounded = physics.isGrounded();

        /*
         * Återställ hopp-räknaren först när spelaren faktiskt
         * landar. Då kan vi inte råka ge tillbaka dubbelhoppet
         * under de första framesen av ett vanligt hopp.
         */
        if (grounded && !wasGrounded) {
            jumpsUsed = 0;
        }

        wasGrounded = grounded;
    }

    private void handleMovement(
            boolean movementBlocked) {

        if (movementBlocked) {
            return;
        }

        float velocityX = 0f;

        if (Gdx.input.isKeyPressed(
                Input.Keys.RIGHT)) {

            velocityX = DebugConfig.SPEED;
            facingLeft = false;
            moving = true;
        }

        if (Gdx.input.isKeyPressed(
                Input.Keys.LEFT)) {

            velocityX = -DebugConfig.SPEED;
            facingLeft = true;
            moving = true;
        }

        physics.setHorizontalVelocity(
                velocityX);
    }

    private void handleJump(
            boolean movementBlocked) {

        boolean jumpPressed = Gdx.input.isKeyPressed(
                Input.Keys.SPACE);

        /*
         * Hoppa bara när SPACE precis tryckts,
         * inte varje frame medan den hålls.
         */
        if (jumpPressed
                && !jumpWasPressed
                && !movementBlocked) {

            if (physics.isGrounded()) {

                physics.jump(
                        0f,
                        DebugConfig.JUMP_MINPOWER);

                jumpsUsed = 1;

            } else if (jumpsUsed == 1
                    && jumpsUsed < MAX_JUMPS) {

                /*
                 * Dubbelhoppet nollställer vertikal fart först.
                 * Annars blir hoppet mycket starkare om SPACE
                 * trycks medan grisen fortfarande rör sig uppåt,
                 * och mycket svagare om den redan faller.
                 */
                physics.doubleJump(
                        0f,
                        DebugConfig.DOUBLE_JUMP_POWER);

                jumpsUsed = 2;
                doubleJumpStartedThisFrame = true;
            }
        }

        jumpWasPressed = jumpPressed;
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

    /**
     * Ett one-frame-event som Player kan skicka vidare till animatorn.
     * Själva animationslängden ägs av PlayerAnimator, inte controllern.
     */
    public boolean didStartDoubleJump() {
        return doubleJumpStartedThisFrame;
    }
}
