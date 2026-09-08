package com.niko.littlepiggy.enemy.dog;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.World;

import com.niko.littlepiggy.assets.GameAssets;
import com.niko.littlepiggy.combat.Damageable;
import com.niko.littlepiggy.combat.KnockbackMode;

public class Dog implements Damageable {

    private static final float MAX_HEALTH = 35f;

    private final DogPhysics physics;
    private final DogAI ai;
    private final DogAnimator animator;

    private float health = MAX_HEALTH;

    public Dog(
            World world,
            GameAssets assets,
            float x,
            float y) {

        physics = new DogPhysics(world, x, y);
        physics.setOwner(this);

        ai = new DogAI(physics);
        animator = new DogAnimator(assets);
    }

    public void update(
            float delta,
            Vector2 playerPosition) {

        ai.update(delta, playerPosition);

        animator.update(
                delta,
                physics.getX(),
                physics.getY(),
                ai.isFacingLeft(),
                ai.isWindingUp(),
                ai.isLunging());
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

    /**
     * Sant precis en gång per lunge - kontaktlyssnaren
     * kollar detta innan den delar ut kontaktskada.
     */
    public boolean isLungeHitAvailable() {
        return ai.isLungeHitAvailable();
    }

    public void consumeLungeHit() {
        ai.consumeLungeHit();
    }

    @Override
    public void takeDamage(float amount) {

        health = Math.max(0f, health - amount);

        animator.triggerFlash();
    }

    @Override
    public void applyKnockback(float x, float y) {
        physics.applyImpulse(x, y);
    }

    @Override
    public void applyKnockback(float x, float y, KnockbackMode mode) {
        physics.applyImpulse(x, y);
    }

    public boolean isDead() {
        return health <= 0f;
    }

    public void destroy() {
        physics.destroy();
    }
}
