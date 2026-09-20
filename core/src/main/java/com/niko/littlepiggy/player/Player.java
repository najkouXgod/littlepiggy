package com.niko.littlepiggy.player;

import com.niko.littlepiggy.combat.Damageable;
import com.niko.littlepiggy.fx.ScreenShake;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.World;

import com.niko.littlepiggy.assets.GameAssets;
import com.niko.littlepiggy.player.ability.AbilityContext;
import com.niko.littlepiggy.player.ability.AbilityManager;
import com.niko.littlepiggy.player.animation.PlayerAnimationState;
import com.niko.littlepiggy.player.animation.PlayerAnimator;
import com.niko.littlepiggy.player.combat.PlayerCombat;
import com.niko.littlepiggy.player.event.PlayerEvent;
import com.niko.littlepiggy.player.event.PlayerEventQueue;
import com.niko.littlepiggy.player.input.PlayerInput;
import com.niko.littlepiggy.player.movement.PlayerMovement;
import com.niko.littlepiggy.player.physics.PlayerPhysics;
import com.niko.littlepiggy.combat.Faction;

/**
 * Tunn facade/koordinator. Player kopplar ihop subsystemen men innehåller
 * ingen ability-logik och inga ability-specifika flaggor:
 *
 * PlayerInput - vad spelaren vill göra
 * PlayerPhysics - Box2D-body
 * PlayerMovement - gå, facing, vanligt hopp, movement modes
 * AbilityManager - Dash, Backflip, Ground Slam (och framtida)
 * PlayerCombat - attacker, damage, knockback, hitstop
 * PlayerAnimator - visar ett animation state
 * PlayerStats - health
 * PlayerEventQueue - Box2D/världen -> gameplay
 */
public class Player implements Damageable {

    private final PlayerStats playerStats;
    private final PlayerInput input;
    private final PlayerPhysics physics;
    private final PlayerMovement movement;
    private final AbilityManager abilities;
    private final PlayerCombat combat;
    private final PlayerAnimator animator;
    private final PlayerEventQueue events = new PlayerEventQueue();

    private final PlayerEventQueue.Handler eventHandler = this::dispatchEvent;

    private boolean wasGrounded;

    public Player(
            World world,
            float startX,
            float startY,
            GameAssets assets) {

        playerStats = new PlayerStats(100f);

        input = new PlayerInput();

        physics = new PlayerPhysics(
                world,
                startX,
                startY);

        physics.setOwner(this);

        combat = new PlayerCombat(
                physics,
                assets);

        movement = new PlayerMovement(
                physics,
                input);

        abilities = new AbilityManager(
                new AbilityContext(
                        input,
                        physics,
                        combat,
                        movement));

        animator = new PlayerAnimator(assets);
    }

    public void update(float delta) {

        input.update();

        /*
         * Events från Box2D-callbacks (och grounded-förändringar) hanteras
         * här, EFTER physics-steget - då är det säkert att abilities skapar
         * och förstör fixtures.
         */
        pollGroundEvents();
        events.drain(eventHandler);

        abilities.update(delta);

        movement.update(
                delta,
                abilities.blocksMovement());

        combat.update(delta);

        animator.update(
                delta,
                resolveAnimationState(),
                physics.getX(),
                physics.getY(),
                movement.isFacingLeft());
    }

    /**
     * Ground contacts uppdateras av Box2D (foot sensors). Vi översätter
     * förändringen till events en gång per update, så alla subsystem
     * ser samma LANDED/LEFT_GROUND.
     */
    private void pollGroundEvents() {

        boolean grounded = physics.isGrounded();

        if (grounded && !wasGrounded) {
            events.push(PlayerEvent.LANDED);

        } else if (!grounded && wasGrounded) {
            events.push(PlayerEvent.LEFT_GROUND);
        }

        wasGrounded = grounded;
    }

    private void dispatchEvent(PlayerEvent event) {

        movement.onPlayerEvent(event);
        abilities.onPlayerEvent(event);
    }

    /** Ability-poser vinner över vanlig movement-animation. */
    private PlayerAnimationState resolveAnimationState() {

        PlayerAnimationState abilityState = abilities.getAnimationState();

        return abilityState != null
                ? abilityState
                : movement.getAnimationState();
    }

    public void render(SpriteBatch batch) {
        animator.render(batch);
    }

    /*
     * ------------------------------------------------------------
     * Events in från världen (GameContactListener)
     * ------------------------------------------------------------
     */

    /**
     * Säkert att anropa från Box2D-callbacks; eventet hanteras i nästa
     * Player.update().
     */
    public void pushEvent(PlayerEvent event) {
        events.push(event);
    }

    /** Ignorerar spelaren kontaktskada (Dog m.m.) just nu? */
    public boolean isImmuneToContactDamage() {
        return abilities.isImmuneToContactDamage();
    }

    public void beginGroundContact() {
        physics.beginGroundContact();
    }

    public void endGroundContact() {
        physics.endGroundContact();
    }

    /*
     * ------------------------------------------------------------
     * HUD / debug - delegerar bara till rätt subsystem
     * ------------------------------------------------------------
     */

    /** Dash-state som text (IDLE/CHARGING/STARTUP/ACTIVE/RECOVERY). */
    public String getCombatState() {
        return abilities.dash().getStateName();
    }

    public boolean isDashing() {
        return abilities.dash().isDashing();
    }

    public boolean isCharging() {
        return abilities.dash().isCharging();
    }

    public float getDashCharge() {
        return abilities.dash().getChargePercent();
    }

    public float getDashChargeTime() {
        return abilities.dash().getChargeTime();
    }

    public boolean isDashReady() {
        return abilities.dash().isReady();
    }

    public boolean isGroundSlamReady() {
        return abilities.groundSlam().isReady();
    }

    public float getGroundSlamCooldownPercent() {
        return abilities.groundSlam().getCooldownPercent();
    }

    public boolean isBackflipReady() {
        return abilities.backflip().isReady();
    }

    public float getBackflipCooldownPercent() {
        return abilities.backflip().getCooldownPercent();
    }

    /*
     * ------------------------------------------------------------
     * Position / physics
     * ------------------------------------------------------------
     */

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

    /*
     * ------------------------------------------------------------
     * Damageable / stats
     * ------------------------------------------------------------
     */

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
    public Faction getFaction() {
        return Faction.PLAYER;
    }

    @Override
    public boolean isDead() {
        return playerStats.isDead();
    }
}
