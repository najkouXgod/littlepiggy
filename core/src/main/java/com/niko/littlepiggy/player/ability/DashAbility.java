package com.niko.littlepiggy.player.ability;

import com.badlogic.gdx.math.MathUtils;

import com.niko.littlepiggy.assets.GameAssets;
import com.niko.littlepiggy.combat.AttackData;
import com.niko.littlepiggy.combat.KnockbackMode;
import com.niko.littlepiggy.player.animation.PlayerAnimationState;
import com.niko.littlepiggy.player.combat.AttackSpec;
import com.niko.littlepiggy.player.combat.PlayerAttackHitbox;
import com.niko.littlepiggy.player.physics.PlayerPhysics;

/**
 * Dash (håll UP för att ladda, släpp för att dasha).
 *
 * Äger hela state machinen som tidigare låg i PlayerCombat:
 * CHARGING -> STARTUP -> ACTIVE -> RECOVERY -> IDLE, samt charge,
 * riktning och fart. Combat får bara höra "starta/avsluta dash-attacken".
 *
 * Dash blockerar vanlig movement i alla states utom IDLE.
 */
public class DashAbility implements PlayerAbility {

    private enum State {
        IDLE,
        CHARGING,
        STARTUP,
        ACTIVE,
        RECOVERY
    }

    /*
     * Full dash laddas på 0.4 s. Charge-animationen är också
     * 4 x 0.10 s, så full charge känns tydlig men mycket snabbare.
     */
    private static final float MAX_CHARGE_TIME = 0.4f;

    /*
     * Även en nästan oladdad dash ska kännas användbar direkt.
     */
    private static final float MIN_DASH_SPEED = 7f;
    private static final float MAX_DASH_SPEED = 13f;

    private static final AttackData DASH = new AttackData(
            30f,
            10f,
            0.01f,
            0.22f,
            0.10f,
            0.55f,
            0.55f,
            0.45f);

    /*
     * Damage och knockback skalas med "power" (0.4 - 1.0 beroende på
     * charge) när attacken startas. duration 0: vi avslutar hitboxen
     * själva när ACTIVE tar slut.
     *
     * Mottagaren får veta att knockbacken kom från DASH (KnockbackMode.DASH)
     * och kan bromsa den snabbare efteråt.
     */
    private static final AttackSpec ATTACK = AttackSpec.builder()
            .damage(DASH.damage())
            .knockback(DASH.knockback(), 0.8f)
            .knockbackMode(KnockbackMode.DASH)
            .hitbox(
                    DASH.hitboxWidth(),
                    DASH.hitboxHeight(),
                    DASH.hitboxOffsetX(),
                    0f)
            .startSound(GameAssets.SFX_DASH_SWIPE)
            .hitSound(GameAssets.SFX_DASH_HIT)
            .hitShake(0.25f)
            .hitStop(0.05f, false)
            .build();

    private final AbilityContext ctx;
    private final PlayerPhysics physics;

    private State state = State.IDLE;

    private float stateTime;
    private float chargeTime;
    private float chargePercent;

    private float dashDirection = 1f;

    private PlayerAttackHitbox activeHitbox;

    public DashAbility(AbilityContext ctx) {
        this.ctx = ctx;
        this.physics = ctx.physics;
    }

    @Override
    public boolean isTriggered() {
        return ctx.input.isDashHeld();
    }

    @Override
    public boolean canStart() {
        return state == State.IDLE;
    }

    /** Börjar ladda. Riktningen låses här. */
    @Override
    public void start() {

        state = State.CHARGING;

        chargeTime = 0f;
        chargePercent = 0f;
        stateTime = 0f;

        dashDirection = ctx.facingDirection();

        physics.setHorizontalVelocity(0f);
    }

    @Override
    public void update(float delta) {

        switch (state) {

            case IDLE:
                break;

            case CHARGING:

                chargeTime = Math.min(
                        chargeTime + delta,
                        MAX_CHARGE_TIME);

                // Stå still medan attacken laddas.
                physics.setHorizontalVelocity(0f);

                if (!ctx.input.isDashHeld()) {
                    beginStartup();
                }

                break;

            case STARTUP:

                stateTime += delta;

                if (stateTime >= DASH.startupTime()) {
                    beginDash();
                }

                break;

            case ACTIVE:

                stateTime += delta;

                float speed = MathUtils.lerp(
                        MIN_DASH_SPEED,
                        MAX_DASH_SPEED,
                        chargePercent);

                physics.setHorizontalVelocity(
                        dashDirection * speed);

                if (stateTime >= DASH.activeTime()) {
                    endDash();
                }

                break;

            case RECOVERY:

                stateTime += delta;

                if (stateTime >= DASH.recoveryTime()) {

                    state = State.IDLE;
                    stateTime = 0f;
                }

                break;
        }
    }

    private void beginStartup() {

        chargePercent = MathUtils.clamp(
                chargeTime / MAX_CHARGE_TIME,
                0f,
                1f);

        state = State.STARTUP;
        stateTime = 0f;
    }

    private void beginDash() {

        state = State.ACTIVE;
        stateTime = 0f;

        float power = 0.4f + chargePercent * 0.6f;

        activeHitbox = ctx.combat.startAttack(
                ATTACK,
                dashDirection,
                power);
    }

    private void endDash() {

        ctx.combat.endAttack(activeHitbox);

        activeHitbox = null;

        physics.setHorizontalVelocity(0f);

        state = State.RECOVERY;
        stateTime = 0f;
    }

    /*
     * ------------------------------------------------------------
     * PlayerAbility - status
     * ------------------------------------------------------------
     */

    @Override
    public boolean isActive() {
        return state != State.IDLE;
    }

    @Override
    public boolean blocksMovement() {
        return state != State.IDLE;
    }

    /** Dash har ingen cooldown - den är redo när den inte pågår. */
    @Override
    public boolean isReady() {
        return state == State.IDLE;
    }

    @Override
    public float getCooldownPercent() {
        return state == State.IDLE ? 1f : 0f;
    }

    @Override
    public PlayerAnimationState getAnimationState() {

        switch (state) {

            case CHARGING:
                return PlayerAnimationState.DASH_CHARGE;

            case STARTUP:
            case ACTIVE:
            case RECOVERY:
                return PlayerAnimationState.DASH;

            default:
                return null;
        }
    }

    /*
     * ------------------------------------------------------------
     * Dash-specifika getters för HUD/debug (via Player)
     * ------------------------------------------------------------
     */

    public boolean isCharging() {
        return state == State.CHARGING;
    }

    public boolean isDashing() {
        return state == State.ACTIVE;
    }

    public float getChargePercent() {

        if (state == State.CHARGING) {
            return MathUtils.clamp(
                    chargeTime / MAX_CHARGE_TIME,
                    0f,
                    1f);
        }

        return chargePercent;
    }

    public float getChargeTime() {
        return chargeTime;
    }

    public String getStateName() {
        return state.name();
    }
}
