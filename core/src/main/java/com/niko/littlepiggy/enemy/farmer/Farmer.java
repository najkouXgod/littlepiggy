package com.niko.littlepiggy.enemy.farmer;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.utils.Array;

import com.niko.littlepiggy.combat.Damageable;
import com.niko.littlepiggy.combat.KnockbackMode;
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

        physics.setOwner(this);

        ai = new FarmerAI(
                physics,
                this,
                assets);

        animator = new FarmerAnimator(assets);
    }

    public Array<Pellet> update(
            float delta,
            Vector2 playerPosition) {

        physics.update(delta);

        Array<Pellet> pellets = ai.update(
                delta,
                playerPosition);

        /*
         * Pellets skapas exakt den frame som
         * aiming är färdig och geväret avfyras.
         */
        if (pellets != null
                && pellets.size > 0) {

            animator.startShooting();
        }

        animator.update(
                delta,
                physics.getX(),
                physics.getY(),
                ai.isFacingLeft(),
                ai.isAiming());

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

        animator.triggerFlash();
    }

    @Override
    public void applyKnockback(
            float x,
            float y) {

        physics.applyImpulse(x, y);
    }

    @Override
    public void applyKnockback(
            float x,
            float y,
            KnockbackMode mode) {

        physics.applyImpulse(x, y);

        if (mode == KnockbackMode.DASH) {
            physics.startDashKnockbackBrake();
        }
    }

    @Override
    public boolean isDead() {
        return health <= 0f;
    }

    public void destroy() {
        physics.destroy();
    }
}
