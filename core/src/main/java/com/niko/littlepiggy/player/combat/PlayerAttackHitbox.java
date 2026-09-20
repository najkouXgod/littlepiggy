package com.niko.littlepiggy.player.combat;

import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.utils.ObjectSet;

import com.niko.littlepiggy.combat.Damageable;

/**
 * En levande attack-hitbox. Är fixturens userData, så GameContactListener
 * kan göra:
 *
 * hitbox.onHit(target, targetX);
 *
 * utan att veta vilken attack det är. Den vet inte heller vilken ability
 * som startade den.
 *
 * Objektet håller attackens runtime-state (vilka targets som redan träffats,
 * hur länge den levt). Livscykeln styrs av AttackHitboxManager.
 */
public class PlayerAttackHitbox {

    private final AttackHitboxManager manager;
    private final AttackSpec spec;
    private final float directionX;
    private final float power;

    private final ObjectSet<Damageable> hitTargets = new ObjectSet<>();

    private Fixture fixture;
    private float age;
    private boolean active = true;

    PlayerAttackHitbox(
            AttackHitboxManager manager,
            AttackSpec spec,
            float directionX,
            float power) {

        this.manager = manager;
        this.spec = spec;
        this.directionX = directionX;
        this.power = power;
    }

    /** Anropas av GameContactListener när sensorn överlappar en Damageable. */
    public void onHit(Damageable target, float targetX) {
        manager.dispatchHit(this, target, targetX);
    }

    public AttackSpec getSpec() {
        return spec;
    }

    /** -1 eller 1. Riktningen attacken startades i. */
    public float getDirectionX() {
        return directionX;
    }

    /** Multiplikator på damage och knockback (Dash: charge-styrka). */
    public float getPower() {
        return power;
    }

    /** Antal olika targets som träffats hittills (inklusive den aktuella). */
    public int getHitCount() {
        return hitTargets.size;
    }

    public boolean isActive() {
        return active;
    }

    /* --- Package-private: bara AttackHitboxManager ändrar dessa --- */

    Fixture getFixture() {
        return fixture;
    }

    void setFixture(Fixture fixture) {
        this.fixture = fixture;
    }

    float getAge() {
        return age;
    }

    void addAge(float delta) {
        age += delta;
    }

    /** @return true om target inte var träffad förut. */
    boolean registerHit(Damageable target) {
        return hitTargets.add(target);
    }

    void deactivate() {
        active = false;
        fixture = null;
    }
}
