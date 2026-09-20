package com.niko.littlepiggy.player.ability;

import com.niko.littlepiggy.player.animation.PlayerAnimationState;
import com.niko.littlepiggy.player.combat.AttackSpec;
import com.niko.littlepiggy.player.event.PlayerEvent;
import com.niko.littlepiggy.player.physics.PlayerPhysics;

/**
 * Ground Slam. ENDA ägaren av WINDUP / FALLING / LANDING.
 *
 * - Kan bara startas i luften.
 * - WINDUP: spelaren hänger stilla i luften.
 * - FALLING: snabbt fall rakt ned tills något träffas.
 * - LANDING: impact, crowd-control-hitbox, spelaren står still en kort stund.
 * - Blockerar vanlig movement hela tiden. Har 2 s cooldown (från start).
 *
 * Impact triggas av events, inte av polling i Player:
 * LANDED - foot sensors nuddade mark
 * HIT_ENEMY_FROM_ABOVE - kroppen träffade en enemy
 * Båda ignoreras om vi inte är i FALLING.
 */
public class GroundSlamAbility implements PlayerAbility {

    private enum State {
        IDLE,
        WINDUP,
        FALLING,
        LANDING
    }

    private static final float COOLDOWN = 2.0f;

    private static final float WINDUP_TIME = 0.24f;
    private static final float LANDING_TIME = 0.18f;

    private static final float FALL_SPEED = 14f;

    private static final AttackSpec ATTACK = AttackSpec.builder()
            .damage(18f)
            .knockback(4f, 3f)
            .knockbackAwayFromOwner(true)
            .hitbox(3.0f, 0.9f, 0f, -0.2f)
            .duration(0.14f)
            // Impact känns även om vi inte träffar en fiende.
            .startShake(0.45f)
            // Hitstop bara på första träffen, annars blir flera enemies hackigt.
            .hitStop(0.06f, true)
            .build();

    private final AbilityContext ctx;
    private final PlayerPhysics physics;

    private State state = State.IDLE;

    private float stateTime;
    private float cooldownRemaining;

    public GroundSlamAbility(AbilityContext ctx) {
        this.ctx = ctx;
        this.physics = ctx.physics;
    }

    @Override
    public boolean isTriggered() {
        return ctx.input.isGroundSlamPressed();
    }

    @Override
    public boolean canStart() {
        return state == State.IDLE
                && cooldownRemaining <= 0f
                && !physics.isGrounded();
    }

    @Override
    public void start() {

        state = State.WINDUP;
        stateTime = 0f;

        cooldownRemaining = COOLDOWN;

        hoverInAir();
    }

    @Override
    public void update(float delta) {

        cooldownRemaining = Math.max(
                0f,
                cooldownRemaining - delta);

        switch (state) {

            case WINDUP:

                hoverInAir();

                stateTime += delta;

                if (stateTime >= WINDUP_TIME) {
                    beginFall();
                }

                break;

            case FALLING:

                // Väntar på LANDED / HIT_ENEMY_FROM_ABOVE (onPlayerEvent).
                break;

            case LANDING:

                // Stå still under impact-framesen.
                physics.setHorizontalVelocity(0f);

                stateTime += delta;

                if (stateTime >= LANDING_TIME) {

                    state = State.IDLE;
                    stateTime = 0f;
                }

                break;

            case IDLE:
            default:
                break;
        }
    }

    @Override
    public void onPlayerEvent(PlayerEvent event) {

        if (state != State.FALLING) {
            return;
        }

        if (event == PlayerEvent.LANDED
                || event == PlayerEvent.HIT_ENEMY_FROM_ABOVE) {

            impact();
        }
    }

    private void hoverInAir() {

        physics.setGravityScale(0f);
        physics.setVelocity(0f, 0f);
    }

    private void beginFall() {

        state = State.FALLING;
        stateTime = 0f;

        physics.resetGravityScale();
        physics.setVelocity(0f, -FALL_SPEED);

        /*
         * Om vi redan råkar vara grounded här (t.ex. en enemy flyttade sig
         * under oss under windup) kommer inget nytt LANDED-event, så vi
         * kollar direkt.
         */
        if (physics.isGrounded()) {
            impact();
        }
    }

    private void impact() {

        state = State.LANDING;
        stateTime = 0f;

        physics.resetGravityScale();
        physics.setHorizontalVelocity(0f);

        ctx.combat.startAttack(ATTACK, 1f, 1f);
    }

    @Override
    public boolean isActive() {
        return state != State.IDLE;
    }

    @Override
    public boolean blocksMovement() {
        return state != State.IDLE;
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

    /** Som förut: bara under själva fallet, inte under windup/landing. */
    @Override
    public boolean isImmuneToContactDamage() {
        return state == State.FALLING;
    }

    @Override
    public PlayerAnimationState getAnimationState() {

        switch (state) {

            case WINDUP:
                return PlayerAnimationState.GROUND_SLAM_WINDUP;

            case FALLING:
                return PlayerAnimationState.GROUND_SLAM_FALL;

            case LANDING:
                return PlayerAnimationState.GROUND_SLAM_IMPACT;

            default:
                return null;
        }
    }
}
