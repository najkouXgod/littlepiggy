package com.niko.littlepiggy.player.movement;

import com.niko.littlepiggy.debug.DebugConfig;
import com.niko.littlepiggy.player.animation.PlayerAnimationState;
import com.niko.littlepiggy.player.event.PlayerEvent;
import com.niko.littlepiggy.player.input.PlayerInput;
import com.niko.littlepiggy.player.physics.PlayerPhysics;

/**
 * Vanlig rörelse: gå, facing och vanligt hopp.
 *
 * Äger:
 * - facing direction
 * - normalJumpUsed-regeln
 * - MovementMode (NORMAL nu, WATER/crouch senare)
 *
 * Äger INTE abilities. Backflip, Dash och Ground Slam ligger i
 * AbilityManager. Rörelsen pausas via movementBlocked när en ability
 * har tagit över kroppen.
 *
 * Hoppregeln:
 * - Space fungerar från marken.
 * - Går man av en kant utan att ha hoppat finns fortfarande ETT hopp.
 * - Efter ett hopp är Space låst tills spelaren LANDAR (event LANDED).
 * Att foot-sensorn råkar vara grounded en kort stund efter hoppet
 * ger alltså inget extra hopp.
 * - Backflip och andra abilities rör aldrig normalJumpUsed.
 */
public class PlayerMovement {

    private final PlayerPhysics physics;
    private final PlayerInput input;

    private MovementMode mode = MovementMode.NORMAL;

    private boolean facingLeft;
    private boolean moving;

    private boolean normalJumpUsed;

    public PlayerMovement(PlayerPhysics physics, PlayerInput input) {
        this.physics = physics;
        this.input = input;
    }

    public void update(float delta, boolean movementBlocked) {

        moving = false;

        if (movementBlocked) {
            return;
        }

        switch (mode) {

            case WATER:
                /*
                 * Framtida vattenrörelse (swim up/down, lägre acceleration,
                 * annan maxfart, physics.setBaseGravityScale(...)) läggs här
                 * som en egen metod. Tills dess beter vi oss som NORMAL.
                 */

            case NORMAL:
            default:
                updateNormal();
                break;
        }
    }

    private void updateNormal() {

        float axis = input.getMoveAxis();

        if (axis != 0f) {
            facingLeft = axis < 0f;
            moving = true;
        }

        physics.setHorizontalVelocity(
                axis * DebugConfig.SPEED);

        if (input.isJumpPressed() && !normalJumpUsed) {

            physics.setVerticalVelocity(0f);

            physics.applyImpulse(
                    0f,
                    DebugConfig.JUMP_MINPOWER);

            /*
             * Vanligt hopp är förbrukat tills spelaren
             * faktiskt landar igen.
             */
            normalJumpUsed = true;
        }
    }

    public void onPlayerEvent(PlayerEvent event) {

        switch (event) {

            case LANDED:
                normalJumpUsed = false;
                break;

            case ENTERED_WATER:
                // Framtid: mode = MovementMode.WATER;
                break;

            case EXITED_WATER:
                // Framtid: mode = MovementMode.NORMAL;
                break;

            default:
                break;
        }
    }

    /**
     * Vad spelaren gör "till vardags" när ingen ability har en pose.
     * Crouch/swimming läggs till här senare.
     */
    public PlayerAnimationState getAnimationState() {

        if (moving) {
            return PlayerAnimationState.RUNNING;
        }

        return PlayerAnimationState.IDLE;
    }

    public MovementMode getMode() {
        return mode;
    }

    public void setMode(MovementMode mode) {
        this.mode = mode;
    }

    public boolean isFacingLeft() {
        return facingLeft;
    }

    public boolean isMoving() {
        return moving;
    }
}
