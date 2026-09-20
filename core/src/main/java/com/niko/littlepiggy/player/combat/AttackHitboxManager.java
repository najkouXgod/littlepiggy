package com.niko.littlepiggy.player.combat;

import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.utils.Array;

import com.niko.littlepiggy.combat.Damageable;
import com.niko.littlepiggy.player.physics.PlayerPhysics;

/**
 * Äger livscykeln för spelarens attack-hitboxar:
 *
 *  - skapar sensor-fixturen
 *  - räknar ned hur länge den lever och förstör den
 *  - håller koll på vilka targets varje attack redan träffat
 *  - meddelar en HitHandler (PlayerCombat) vid varje NY träff
 *
 * Här finns ingen kunskap om Dash/Backflip/Slam - allt attack-specifikt
 * kommer från AttackSpec. Det är därför PlayerCombat inte behöver
 * "activeXHitbox / xHitboxTimer / xHitTargets" per attack längre.
 *
 * spawn/cancel/update skapar och förstör fixtures och får därför INTE
 * anropas under world.step(). De anropas från Player.update().
 */
public class AttackHitboxManager {

    public interface HitHandler {
        void onAttackHit(
                PlayerAttackHitbox hitbox,
                Damageable target,
                float targetX);
    }

    private final PlayerPhysics physics;
    private final HitHandler hitHandler;

    private final Array<PlayerAttackHitbox> active = new Array<>(false, 4);

    public AttackHitboxManager(
            PlayerPhysics physics,
            HitHandler hitHandler) {

        this.physics = physics;
        this.hitHandler = hitHandler;
    }

    /**
     * @param directionX -1 eller 1. Speglar hitboxens offsetX.
     * @param power      multiplikator som skickas vidare till träffen.
     */
    public PlayerAttackHitbox spawn(
            AttackSpec spec,
            float directionX,
            float power) {

        PlayerAttackHitbox hitbox = new PlayerAttackHitbox(
                this,
                spec,
                directionX,
                power);

        Fixture fixture = physics.createSensor(
                spec.hitboxWidth,
                spec.hitboxHeight,
                spec.hitboxOffsetX * directionX,
                spec.hitboxOffsetY,
                hitbox);

        hitbox.setFixture(fixture);

        active.add(hitbox);

        return hitbox;
    }

    /** Avslutar en hitbox i förtid (t.ex. när Dash tar slut). null är okej. */
    public void cancel(PlayerAttackHitbox hitbox) {

        if (hitbox == null || !hitbox.isActive()) {
            return;
        }

        destroy(hitbox);

        active.removeValue(hitbox, true);
    }

    /** Förstör alla hitboxar (t.ex. vid död eller level-reset). */
    public void cancelAll() {

        for (int i = 0; i < active.size; i++) {
            destroy(active.get(i));
        }

        active.clear();
    }

    public void update(float delta) {

        for (int i = active.size - 1; i >= 0; i--) {

            PlayerAttackHitbox hitbox = active.get(i);

            float duration = hitbox.getSpec().duration;

            // duration 0 = ability avslutar den själv.
            if (duration <= 0f) {
                continue;
            }

            hitbox.addAge(delta);

            if (hitbox.getAge() >= duration) {

                destroy(hitbox);

                active.removeIndex(i);
            }
        }
    }

    /**
     * Anropas via PlayerAttackHitbox.onHit(), dvs. från Box2D-callbacken.
     * Här sker bara dedupe; själva effekten hanteras av HitHandler.
     * (Inga fixtures skapas/förstörs här.)
     */
    void dispatchHit(
            PlayerAttackHitbox hitbox,
            Damageable target,
            float targetX) {

        if (!hitbox.isActive()) {
            return;
        }

        if (!hitbox.registerHit(target)) {
            return;
        }

        hitHandler.onAttackHit(hitbox, target, targetX);
    }

    private void destroy(PlayerAttackHitbox hitbox) {

        physics.destroyFixture(hitbox.getFixture());

        hitbox.deactivate();
    }
}
