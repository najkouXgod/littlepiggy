package com.niko.littlepiggy.enemy;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.utils.Array;

import com.niko.littlepiggy.combat.Damageable;
import com.niko.littlepiggy.assets.GameAssets;
import com.niko.littlepiggy.projectile.Pellet;

public class Farmer implements Damageable {

    private static final float MAX_HEALTH = 60f;

    private final FarmerPhysics physics;
    private final FarmerAI ai;
    private final FarmerAnimator animator;
    private float health = MAX_HEALTH;

    public Farmer(
            World world,
            GameAssets assets,
            float x,
            float y) {

        physics = new FarmerPhysics(
                world,
                x,
                y);

        /*
         * Box2D-fixtures måste fortfarande kunna
         * identifieras som just denna Farmer.
         */
        physics.setOwner(this);

        ai = new FarmerAI(physics, this);

        animator = new FarmerAnimator(assets);
    }

    public Array<Pellet> update(
            float delta,
            Vector2 playerPosition) {

        Array<Pellet> pellets = ai.update(
                delta,
                playerPosition);

        animator.update(
                delta,
                physics.getX(),
                physics.getY(),
                ai.isFacingLeft());

        return pellets;
    }

    public void render(SpriteBatch batch) {
        animator.render(batch);
    }

    public void playerEnteredRange() {
        ai.playerEnteredRange();
    }

    public void playerExitedRange() {
        ai.playerExitedRange();
    }

    public Vector2 getPosition() {
        return physics.getPosition();
    }

    @Override
    public void takeDamage(float amount) {

        health = Math.max(
                0f,
                health - amount);
    }

    @Override
    public void applyKnockback(
            float x,
            float y) {

        physics.applyImpulse(x, y);
    }

    @Override
    public boolean isDead() {
        return health <= 0f;
    }

    public void destroy() {
        physics.destroy();
    }
}
