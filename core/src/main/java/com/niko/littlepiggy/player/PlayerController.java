package com.niko.littlepiggy.player;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;

import com.niko.littlepiggy.debug.DebugConfig;

public class PlayerController {

    private final PlayerPhysics physics;

    private boolean moving;
    private boolean facingLeft;

    private boolean jumpWasPressed;
    private boolean dashChargeHeld;

    public PlayerController(PlayerPhysics physics) {
        this.physics = physics;
    }

    public void update(
            float delta,
            boolean movementBlocked) {

        moving = false;

        dashChargeHeld = Gdx.input.isKeyPressed(
                Input.Keys.UP);

        handleMovement(movementBlocked);
        handleJump(movementBlocked);
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
                && physics.isGrounded()
                && !movementBlocked) {

            physics.jump(
                    0f,
                    DebugConfig.JUMP_MINPOWER);
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
}
