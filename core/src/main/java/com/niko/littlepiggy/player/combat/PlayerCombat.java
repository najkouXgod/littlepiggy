package com.niko.littlepiggy.player.combat;

import com.niko.littlepiggy.assets.GameAssets;
import com.niko.littlepiggy.combat.Damageable;
import com.niko.littlepiggy.fx.HitStop;
import com.niko.littlepiggy.fx.ScreenShake;
import com.niko.littlepiggy.player.physics.PlayerPhysics;

/**
 * Combat-systemet: utför attacker åt abilities.
 *
 * Abilities bestämmer NÄR en attack startar/slutar. PlayerCombat
 * bestämmer VAD som händer: skapa hitbox, ljud, screenshake, damage,
 * knockback och hitstop. Här finns ingen state machine och ingen
 * kunskap om Dash/Backflip/Ground Slam.
 */
public class PlayerCombat implements AttackHitboxManager.HitHandler {

    private final PlayerPhysics physics;
    private final GameAssets assets;
    private final AttackHitboxManager hitboxes;

    public PlayerCombat(PlayerPhysics physics, GameAssets assets) {
        this.physics = physics;
        this.assets = assets;
        this.hitboxes = new AttackHitboxManager(physics, this);
    }

    public void update(float delta) {
        hitboxes.update(delta);
    }

    /**
     * Startar en attack.
     *
     * @param directionX -1 eller 1 (attackens riktning)
     * @param power      multiplikator på damage/knockback (1 = normalt)
     * @return handle för attacker med duration 0, som abilityn själv måste
     *         avsluta med endAttack(). Kan ignoreras för tidsstyrda attacker.
     */
    public PlayerAttackHitbox startAttack(
            AttackSpec spec,
            float directionX,
            float power) {

        if (spec.startSound != null) {
            assets.playSound(spec.startSound);
        }

        if (spec.startShake > 0f) {
            ScreenShake.addTrauma(spec.startShake);
        }

        return hitboxes.spawn(spec, directionX, power);
    }

    public void endAttack(PlayerAttackHitbox hitbox) {
        hitboxes.cancel(hitbox);
    }

    public void cancelAllAttacks() {
        hitboxes.cancelAll();
    }

    /**
     * Anropas (via AttackHitboxManager) första gången en attack träffar
     * en viss target. Körs i Box2D-callback, så inga fixtures skapas här.
     */
    @Override
    public void onAttackHit(
            PlayerAttackHitbox hitbox,
            Damageable target,
            float targetX) {

        AttackSpec spec = hitbox.getSpec();
        float power = hitbox.getPower();

        if (spec.hitSound != null) {
            assets.playSound(spec.hitSound);
        }

        if (spec.hitShake > 0f) {
            ScreenShake.addTrauma(spec.hitShake);
        }

        if (spec.hitStop > 0f
                && (!spec.hitStopOncePerAttack
                        || hitbox.getHitCount() == 1)) {

            HitStop.trigger(spec.hitStop);
        }

        target.takeDamage(spec.damage * power);

        float directionX = hitbox.getDirectionX();

        if (spec.knockbackAwayFromOwner) {

            directionX = Math.signum(targetX - physics.getX());

            if (directionX == 0f) {
                directionX = 1f;
            }
        }

        target.applyKnockback(
                directionX * spec.knockbackX * power,
                spec.knockbackY * power,
                spec.knockbackMode);
    }
}
