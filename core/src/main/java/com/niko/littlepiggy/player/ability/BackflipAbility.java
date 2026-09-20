package com.niko.littlepiggy.player.ability;

import com.niko.littlepiggy.assets.GameAssets;
import com.niko.littlepiggy.debug.DebugConfig;
import com.niko.littlepiggy.player.animation.PlayerAnimationState;
import com.niko.littlepiggy.player.combat.AttackSpec;
import com.niko.littlepiggy.player.physics.PlayerPhysics;

/**
 * Backflip (Ctrl).
 *
 * - Kan användas på marken eller i luften.
 * - Ger en vertikal hoppimpuls och startar en hitbox.
 * - Har 1 s cooldown och är oberoende av vanligt hopp
 * (rör aldrig normalJumpUsed).
 * - Blockerar INTE vanlig movement.
 *
 * ACTIVE är "backflip-posen": den varar tills spelaren landat efter att
 * ha varit i luften, eller max MAX_ACTIVE_TIME. Det är den här klassen
 * som äger den regeln - animatorn visar bara BACKFLIP så länge vi är ACTIVE.
 */
public class BackflipAbility implements PlayerAbility {

    private enum State {
        IDLE,
        ACTIVE
    }

    private static final float COOLDOWN = 1.0f;

    /*
     * = 11 frames * 0.055 s, samma längd som backflip-animationen i
     * PlayerAnimator. Ändrar du animationens längd, ändra här också.
     */
    private static final float MAX_ACTIVE_TIME = 0.605f;

    private static final AttackSpec ATTACK = AttackSpec.builder()
            .damage(20f)
            .knockback(2f, 7f)
            .hitbox(0.85f, 0.85f, 0f, 0.08f)
            .duration(0.45f)
            .hitSound(GameAssets.SFX_BACKFLIP_HIT)
            .hitShake(0.25f)
            .hitStop(0.05f, false)
            .build();

    private final AbilityContext ctx;
    private final PlayerPhysics physics;

    private State state = State.IDLE;

    private float cooldownRemaining;
    private float activeTime;
    private boolean airborneSeen;

    public BackflipAbility(AbilityContext ctx) {
        this.ctx = ctx;
        this.physics = ctx.physics;
    }

    @Override
    public boolean isTriggered() {
        return ctx.input.isBackflipPressed();
    }

    @Override
    public boolean canStart() {
        return cooldownRemaining <= 0f;
    }

    @Override
    public void start() {

        physics.setVerticalVelocity(0f);

        physics.applyImpulse(
                0f,
                DebugConfig.BACKFLIP_JUMP_POWER);

        state = State.ACTIVE;
        activeTime = 0f;
        airborneSeen = false;

        cooldownRemaining = COOLDOWN;

        ctx.combat.startAttack(
                ATTACK,
                ctx.facingDirection(),
                1f);
    }

    @Override
    public void update(float delta) {

        cooldownRemaining = Math.max(
                0f,
                cooldownRemaining - delta);

        if (state != State.ACTIVE) {
            return;
        }

        activeTime += delta;

        /*
         * Avsluta först när spelaren faktiskt varit i luften och sedan
         * landar igen. Då stoppar inte Ctrl från marken posen direkt
         * på första framen.
         */
        if (!physics.isGrounded()) {

            airborneSeen = true;

        } else if (airborneSeen) {

            endActive();
            return;
        }

        if (activeTime >= MAX_ACTIVE_TIME) {
            endActive();
        }
    }

    @Override
    public void onOtherAbilityStarted(PlayerAbility other) {

        // En ability som tar över kroppen (Ground Slam, Dash) avslutar posen.
        if (other.blocksMovement()) {
            endActive();
        }
    }

    private void endActive() {
        state = State.IDLE;
        activeTime = 0f;
        airborneSeen = false;
    }

    @Override
    public boolean isActive() {
        return state == State.ACTIVE;
    }

    @Override
    public boolean blocksMovement() {
        return false;
    }

    @Override
    public boolean isReady() {
        return cooldownRemaining <= 0f;
    }

    @Override
    public float getCooldownPercent() {
        return 1f - Math.min(
                1f,
                cooldownRemaining / COOLDOWN);
    }

    @Override
    public PlayerAnimationState getAnimationState() {

        return state == State.ACTIVE
                ? PlayerAnimationState.BACKFLIP
                : null;
    }
}
