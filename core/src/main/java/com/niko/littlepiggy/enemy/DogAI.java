package com.niko.littlepiggy.enemy;

import com.badlogic.gdx.math.Vector2;

public class DogAI {

    private enum State {
        IDLE,
        CHASE,
        WINDUP,
        LUNGE,
        COOLDOWN
    }

    private static final float CHASE_SPEED = 3f;
    private static final float LUNGE_TRIGGER_RANGE = 1.3f;
    private static final float LUNGE_SPEED = 9f;

    private static final float WINDUP_TIME = 0.35f;
    private static final float LUNGE_DURATION = 0.2f;
    private static final float COOLDOWN_TIME = 0.6f;

    private final DogPhysics physics;

    private State state = State.IDLE;
    private float stateTime;

    private boolean facingLeft;
    private float lockedDirectionX;

    /*
     * Förhindrar att en enda lunge kan skada spelaren flera
     * gånger om flera av spelarens fixtures nuddar samtidigt.
     */
    private boolean lungeHitAvailable;

    private int playerRangeContacts;

    public DogAI(DogPhysics physics) {
        this.physics = physics;
    }

    public void update(
            float delta,
            Vector2 playerPosition) {

        Vector2 myPosition = physics.getPosition();

        float directionX = playerPosition.x - myPosition.x;

        facingLeft = directionX < 0f;

        stateTime += delta;

        switch (state) {

            case WINDUP:
                physics.stopHorizontal();

                if (stateTime >= WINDUP_TIME) {
                    beginLunge();
                }
                break;

            case LUNGE:

                if (stateTime >= LUNGE_DURATION) {
                    enterCooldown();
                }
                break;

            case COOLDOWN:
                physics.stopHorizontal();

                if (stateTime >= COOLDOWN_TIME) {
                    state = State.IDLE;
                    stateTime = 0f;
                }
                break;

            case IDLE:
            case CHASE:
            default:
                updateChase(
                        delta,
                        playerPosition,
                        myPosition,
                        directionX);
                break;
        }
    }

    private void updateChase(
            float delta,
            Vector2 playerPosition,
            Vector2 myPosition,
            float directionX) {

        if (!isPlayerInRange()
                || !physics.hasLineOfSight(playerPosition)) {

            physics.stopHorizontal();
            state = State.IDLE;
            return;
        }

        float distance = myPosition.dst(playerPosition);

        if (distance <= LUNGE_TRIGGER_RANGE) {

            state = State.WINDUP;
            stateTime = 0f;
            lockedDirectionX = directionX;
            physics.stopHorizontal();
            return;
        }

        state = State.CHASE;

        physics.moveTowards(
                directionX,
                CHASE_SPEED);
    }

    private void beginLunge() {

        state = State.LUNGE;
        stateTime = 0f;
        lungeHitAvailable = true;

        physics.lunge(
                lockedDirectionX,
                LUNGE_SPEED);
    }

    private void enterCooldown() {

        state = State.COOLDOWN;
        stateTime = 0f;
        physics.stopHorizontal();
    }

    public boolean isLungeHitAvailable() {
        return lungeHitAvailable && state == State.LUNGE;
    }

    public void consumeLungeHit() {
        lungeHitAvailable = false;
    }

    public void playerEnteredRange() {
        playerRangeContacts++;
    }

    public void playerExitedRange() {

        playerRangeContacts = Math.max(
                0,
                playerRangeContacts - 1);
    }

    public boolean isPlayerInRange() {
        return playerRangeContacts > 0;
    }

    public boolean isFacingLeft() {
        return facingLeft;
    }

    /** Används av animatorn för att visa windup-telegraph. */
    public boolean isWindingUp() {
        return state == State.WINDUP;
    }

    public boolean isLunging() {
        return state == State.LUNGE;
    }
}
