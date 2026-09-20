package com.niko.littlepiggy.enemy.farmer;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.utils.Array;

import com.niko.littlepiggy.combat.Faction;
import com.niko.littlepiggy.combat.Damageable;
import com.niko.littlepiggy.combat.KnockbackMode;
import com.niko.littlepiggy.assets.GameAssets;
import com.niko.littlepiggy.projectile.Pellet;

public class Farmer implements Damageable {

    private static final float MAX_HEALTH = 60f;

    /*
     * Farmer kan falla ungefär 3 world units
     * utan skada.
     */
    private static final float SAFE_FALL_DISTANCE = 3f;

    /*
     * Varje world unit över safe distance ger
     * 15 damage.
     *
     * 4 units -> 15
     * 5 units -> 30
     * 6 units -> 45
     * 7 units -> 60
     */
    private static final float FALL_DAMAGE_PER_UNIT = 15f;

    /*
     * Kartans normala nederkant är y = 0.
     *
     * När Farmer kommit två world units under
     * kartan räknas han som bortfallen.
     */
    private static final float OUT_OF_BOUNDS_DEATH_Y = -2f;

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

        animator = new FarmerAnimator(
                assets);
    }

    public Array<Pellet> update(
            float delta,
            Vector2 playerPosition) {

        physics.update(delta);

        /*
         * Farmer har fallit ned utanför kartan.
         */
        if (physics.getY() < OUT_OF_BOUNDS_DEATH_Y) {

            health = 0f;

            return null;
        }

        /*
         * Kolla fall damage exakt den frame
         * Farmer landar.
         */
        if (physics.didJustLand()) {

            applyFallDamage(
                    physics.getLastFallDistance());
        }

        /*
         * Om fallet dödade Farmer ska han inte
         * hinna skjuta samma frame.
         */
        if (isDead()) {
            return null;
        }

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

    private void applyFallDamage(
            float fallDistance) {

        if (fallDistance <= SAFE_FALL_DISTANCE) {

            return;
        }

        float dangerousDistance = fallDistance
                - SAFE_FALL_DISTANCE;

        float damage = dangerousDistance
                * FALL_DAMAGE_PER_UNIT;

        takeDamage(damage);
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

    /*
     * Kallas av GameContactListener när
     * Farmerns foot sensor träffar terrain.
     */
    public void beginGroundContact() {
        physics.beginGroundContact();
    }

    public void endGroundContact() {
        physics.endGroundContact();
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

        physics.applyImpulse(
                x,
                y);
    }

    @Override
    public void applyKnockback(
            float x,
            float y,
            KnockbackMode mode) {

        physics.applyImpulse(
                x,
                y);

        if (mode == KnockbackMode.DASH) {

            physics.startDashKnockbackBrake();
        }
    }

    @Override
    public boolean isDead() {
        return health <= 0f;
    }

    @Override
    public Faction getFaction() {
        return Faction.ENEMY;
    }

    public void destroy() {
        physics.destroy();
    }
}
