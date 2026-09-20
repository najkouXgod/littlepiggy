package com.niko.littlepiggy.player;

import com.niko.littlepiggy.combat.Damageable;
import com.niko.littlepiggy.fx.ScreenShake;

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

    private boolean groundSlamEnemyContactPending;

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

        combat = new PlayerCombat(
                physics,
                assets);

        physics.setOwner(this);

        controller = new PlayerController(physics);

        animator = new PlayerAnimator(assets);
    }

    public void update(float delta) {

        controller.update(
                delta,
                combat.blocksMovement());

        /*
         * ContactListener körs under physics.step().
         * Vi väntar därför tills Player.update() innan vi
         * faktiskt ändrar slam-state/skapar attack-hitbox.
         */
        if (groundSlamEnemyContactPending) {

            controller.landGroundSlamOnEnemy();

            groundSlamEnemyContactPending = false;
        }

        if (controller.didStartGroundSlam()) {
            animator.startGroundSlam();
        }

        if (controller.didLandGroundSlam()) {

            animator.landGroundSlam();

            combat.startGroundSlamAttack();
        }

        if (controller.didStartBackflip()) {

            animator.startBackflip();

            combat.startBackflipAttack(
                    controller.isFacingLeft());
        }

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
                physics.isGrounded(),
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

    public boolean isCharging() {
        return combat.isCharging();
    }

    public float getDashCharge() {
        return combat.getChargePercent();
    }

    public float getDashChargeTime() {
        return combat.getChargeTime();
    }

    public boolean isGroundSlamFalling() {
        return controller.isGroundSlamFalling();
    }

    public void requestGroundSlamEnemyContact() {
        groundSlamEnemyContactPending = true;
    }

    public boolean isGroundSlamReady() {
        return controller.isGroundSlamReady();
    }

    public float getGroundSlamCooldownPercent() {
        return controller.getGroundSlamCooldownPercent();
    }

    /*
     * Dash är redo när PlayerCombat inte håller på
     * med charge/startup/dash/recovery.
     */
    public boolean isDashReady() {
        return !combat.blocksMovement();
    }

    public boolean isBackflipReady() {
        return controller.isBackflipReady();
    }

    public float getBackflipCooldownPercent() {
        return controller.getBackflipCooldownPercent();
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

    @Override
    public void applyKnockback(float x, float y) {
        physics.applyImpulse(x, y);
    }

    @Override
    public void takeDamage(float amount) {

        playerStats.takeDamage(amount);

        ScreenShake.addTrauma(0.5f);

        animator.triggerFlash();
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
