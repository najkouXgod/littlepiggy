package com.niko.littlepiggy.enemy.farmer;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;

import com.niko.littlepiggy.assets.GameAssets;
import com.niko.littlepiggy.projectile.Pellet;

public class FarmerAI {

    private enum State {
        IDLE,
        AIMING
    }

    private static final int PELLET_COUNT = 4;

    private static final float SPREAD_DEGREES = 8f;

    private static final float SHOOT_INTERVAL = 3.5f;

    /*
     * 3 aiming-frames * 0.12 sek.
     */
    private static final float AIM_TIME = 0.36f;

    private final FarmerPhysics physics;
    private final Farmer owner;
    private final GameAssets assets;

    private State state = State.IDLE;

    private boolean facingLeft;

    private int playerRangeContacts;

    private float shootCooldown;
    private float aimTimer;

    public FarmerAI(
            FarmerPhysics physics,
            Farmer owner,
            GameAssets assets) {

        this.physics = physics;
        this.owner = owner;
        this.assets = assets;
    }

    public Array<Pellet> update(
            float delta,
            Vector2 playerPosition) {

        facingLeft = playerPosition.x < physics.getX();

        if (shootCooldown > 0f) {
            shootCooldown -= delta;
        }

        boolean canSeePlayer = isPlayerInRange()
                && physics.hasLineOfSight(
                        playerPosition);

        /*
         * AIMING
         */
        if (state == State.AIMING) {

            /*
             * Om spelaren försvinner ur range eller
             * bakom ett hinder avbryts siktandet.
             */
            if (!canSeePlayer) {

                state = State.IDLE;
                aimTimer = 0f;

                return null;
            }

            aimTimer += delta;

            /*
             * När aiming-animationen är klar
             * avfyras skottet.
             */
            if (aimTimer >= AIM_TIME) {

                state = State.IDLE;
                aimTimer = 0f;

                shootCooldown = SHOOT_INTERVAL;

                assets.playSound(
                        GameAssets.SFX_FARMER_SHOT);

                return createPellets(
                        playerPosition);
            }

            return null;
        }

        /*
         * Börja sikta.
         */
        if (canSeePlayer
                && shootCooldown <= 0f) {

            state = State.AIMING;
            aimTimer = 0f;
        }

        return null;
    }

    private Array<Pellet> createPellets(
            Vector2 playerPosition) {

        Array<Pellet> pellets = new Array<>();

        Vector2 farmerPosition = physics.getPosition();

        Vector2 baseDirection = new Vector2(playerPosition)
                .sub(farmerPosition)
                .nor();

        for (int i = 0; i < PELLET_COUNT; i++) {

            float angle = (MathUtils.random(
                    -SPREAD_DEGREES / 2f,
                    SPREAD_DEGREES / 2f)

                    + MathUtils.random(
                            -SPREAD_DEGREES / 2f,
                            SPREAD_DEGREES / 2f))
                    / 2f;

            Vector2 direction = new Vector2(baseDirection)
                    .rotateDeg(angle);

            float speedMultiplier = MathUtils.random(
                    0.8f,
                    1.2f);

            pellets.add(
                    new Pellet(
                            physics.getWorld(),
                            owner,
                            farmerPosition.x,
                            farmerPosition.y,
                            speedMultiplier,
                            direction));
        }

        return pellets;
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

    public boolean isAiming() {
        return state == State.AIMING;
    }
}
