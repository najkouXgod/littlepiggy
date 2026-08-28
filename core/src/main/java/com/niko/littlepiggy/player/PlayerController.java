package com.niko.littlepiggy.player;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.math.Vector2;
import com.niko.littlepiggy.debug.DebugConfig;

public class PlayerController {

    private final PlayerPhysics physics;

    private boolean spaceWasPressed;
    private boolean moving;
    private boolean facingLeft;
    private boolean charging;

    private float jumpCharge;
    private float spacePressedTime;

    public PlayerController(PlayerPhysics physics) {
        this.physics = physics;
    }

    public void update(float delta) {

        moving = false;

        handleMovement();
        handleJump(delta);
    }

    private void handleMovement() {

        if (charging) {
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

    private void handleJump(float delta) {

        boolean jumpPressed = Gdx.input.isKeyPressed(Input.Keys.UP);

        /*
         * Är vi i luften ska vi inte kunna börja
         * ladda ett nytt hopp.
         */
        if (!physics.isGrounded()) {

            if (!jumpPressed) {
                resetCharge();
            }

            spaceWasPressed = jumpPressed;
            return;
        }

        /*
         * Knappen hålls nere.
         */
        if (jumpPressed) {

            spacePressedTime += delta;

            if (spacePressedTime >= DebugConfig.TAP_THRESHOLD) {

                Vector2 velocity = physics.getVelocity();

                float newVelocityX = velocity.x *
                        (1f - delta * DebugConfig.CHARGE_FRICTION);

                if (Math.abs(newVelocityX) < 0.1f) {
                    newVelocityX = 0f;
                }

                physics.setHorizontalVelocity(newVelocityX);

                jumpCharge = Math.min(
                        jumpCharge
                                + delta / DebugConfig.CHARGE_TIME,
                        1f);

                charging = true;
            }
        }

        /*
         * Knappen släpptes.
         */
        if (!jumpPressed && spaceWasPressed) {

            if (spacePressedTime < DebugConfig.TAP_THRESHOLD) {

                // Kort tryck
                physics.jump(
                        0,
                        DebugConfig.JUMP_MINPOWER);

            } else if (charging) {

                // Laddat hopp
                float curvedCharge = (float) Math.pow(
                        jumpCharge,
                        1f / DebugConfig.CHARGE_CURVE);

                float jumpStrength = DebugConfig.JUMP_MINPOWER
                        + (DebugConfig.JUMP_MAXPOWER
                                - DebugConfig.JUMP_MINPOWER) * curvedCharge;

                float horizontalBoost = (facingLeft ? -1f : 1f)
                        * 2.6f
                        * curvedCharge;

                physics.jump(
                        horizontalBoost,
                        jumpStrength);
            }

            resetCharge();
        }

        spaceWasPressed = jumpPressed;
    }

    private void resetCharge() {
        spacePressedTime = 0f;
        jumpCharge = 0f;
        charging = false;
    }

    public boolean isMoving() {
        return moving;
    }

    public boolean isFacingLeft() {
        return facingLeft;
    }

    public boolean isCharging() {
        return charging;
    }

    public float getJumpCharge() {
        return jumpCharge;
    }
}
