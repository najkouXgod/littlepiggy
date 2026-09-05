package com.niko.littlepiggy.player;

import com.niko.littlepiggy.combat.Damageable;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.World;

import com.niko.littlepiggy.assets.GameAssets;

public class Player implements Damageable {

    private final PlayerCombat combat;
    private final PlayerStats playerStats;
    private final PlayerPhysics physics;
    private final PlayerController controller;
    private final PlayerAnimator animator;

    public Player(
            World world,
            float startX,
            float startY,
            GameAssets assets) {

        playerStats = new PlayerStats(100f);

        physics = new PlayerPhysics(
                world,
                startX,
                startY);

        combat = new PlayerCombat(physics);

        /*
         * Viktigt:
         * Box2D-body:n identifieras fortfarande som Player.
         *
         * Det gör att t.ex. Farmer range-sensorn fortfarande
         * kan använda:
         *
         * body.getUserData() instanceof Player
         */
        physics.setOwner(this);

        controller = new PlayerController(physics);

        animator = new PlayerAnimator(assets);
    }

    public void update(float delta) {

        controller.update(
                delta,
                combat.blocksMovement());

        combat.update(
                delta,
                controller.isDashChargeHeld(),
                controller.isFacingLeft());

        animator.update(
                delta,
                physics.getX(),
                physics.getY(),
                controller.isMoving(),
                combat.isCharging(),
                combat.isDashSequence(),
                controller.isFacingLeft());
    }

    public void render(SpriteBatch batch) {
        animator.render(batch);
    }

    public String getCombatState() {
        return combat.getStateName();
    }

    public boolean isDashing() {
        return combat.isDashing();
    }

    public float getDashCharge() {
        return combat.getChargePercent();
    }

    public float getDashChargeTime() {
        return combat.getChargeTime();
    }

    public float getX() {
        return physics.getX();
    }

    public float getY() {
        return physics.getY();
    }

    public Vector2 getPosition() {
        return physics.getPosition();
    }

    public Vector2 getVelocity() {
        return physics.getVelocity();
    }

    public boolean isGrounded() {
        return physics.isGrounded();
    }

    public void beginGroundContact() {
        physics.beginGroundContact();
    }

    public void endGroundContact() {
        physics.endGroundContact();
    }

    public boolean isCharging() {
        return combat.isCharging();
    }

    @Override
    public void applyKnockback(float x, float y) {
        physics.applyImpulse(x, y);
    }

    @Override
    public void takeDamage(float amount) {
        playerStats.takeDamage(amount);
    }

    public void heal(float amount) {
        playerStats.heal(amount);
    }

    public float getHealth() {
        return playerStats.getHealth();
    }

    public float getMaxHealth() {
        return playerStats.getMaxHealth();
    }

    @Override
    public boolean isDead() {
        return playerStats.isDead();
    }
}
