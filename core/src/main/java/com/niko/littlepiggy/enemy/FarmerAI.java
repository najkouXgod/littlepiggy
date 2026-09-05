package com.niko.littlepiggy.enemy;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;

import com.niko.littlepiggy.projectile.Pellet;

public class FarmerAI {

    private static final int PELLET_COUNT = 4;

    private static final float SPREAD_DEGREES = 8f;

    private static final float SHOOT_INTERVAL = 3.5f;

    private final FarmerPhysics physics;
    private final Farmer owner;

    private boolean facingLeft;

    /*
     * Counter istället för boolean eftersom Player
     * har flera fixtures som kan vara inne i sensorn
     * samtidigt.
     */
    private int playerRangeContacts;

    private float shootCooldown;

    public FarmerAI(FarmerPhysics physics, Farmer owner) {
        this.physics = physics;
        this.owner = owner;
    }

    public Array<Pellet> update(
            float delta,
            Vector2 playerPosition) {

        facingLeft = playerPosition.x < physics.getX();

        if (shootCooldown > 0f) {
            shootCooldown -= delta;
        }

        boolean canShoot = isPlayerInRange()
                && physics.hasLineOfSight(
                        playerPosition)
                && shootCooldown <= 0f;

        if (canShoot) {

            shootCooldown = SHOOT_INTERVAL;

            return createPellets(
                    playerPosition);
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
}
