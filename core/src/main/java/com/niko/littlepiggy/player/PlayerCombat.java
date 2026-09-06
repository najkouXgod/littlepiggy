package com.niko.littlepiggy.player;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.utils.ObjectSet;

import com.niko.littlepiggy.combat.AttackData;
import com.niko.littlepiggy.combat.Damageable;
import com.niko.littlepiggy.combat.KnockbackMode;

public class PlayerCombat {

    private enum CombatState {
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
     * Bakåtvolten är en separat attack. Den har mindre horisontell
     * knockback än dashen men kastar fienden tydligt uppåt.
     */
    private static final float BACKFLIP_DAMAGE = 20f;
    private static final float BACKFLIP_KNOCKBACK_X = 2f;
    private static final float BACKFLIP_KNOCKBACK_Y = 7f;
    private static final float BACKFLIP_HITBOX_TIME = 0.45f;
    private static final float BACKFLIP_HITBOX_WIDTH = 0.85f;
    private static final float BACKFLIP_HITBOX_HEIGHT = 0.85f;
    private static final float BACKFLIP_HITBOX_OFFSET_Y = 0.08f;

    private final PlayerPhysics physics;

    private final ObjectSet<Damageable> dashHitTargets = new ObjectSet<>();
    private final ObjectSet<Damageable> backflipHitTargets = new ObjectSet<>();

    private CombatState state = CombatState.IDLE;

    private float stateTime;
    private float chargeTime;
    private float chargePercent;

    private int dashDirection = 1;
    private int backflipDirection = 1;

    private Fixture activeDashHitbox;
    private Fixture activeBackflipHitbox;
    private float backflipHitboxTime;

    public PlayerCombat(PlayerPhysics physics) {
        this.physics = physics;
    }

    public void update(
            float delta,
            boolean chargeHeld,
            boolean facingLeft) {

        updateBackflipHitbox(delta);

        switch (state) {

            case IDLE:
                if (chargeHeld) {
                    beginCharge(facingLeft);
                }
                break;

            case CHARGING:

                chargeTime = Math.min(
                        chargeTime + delta,
                        MAX_CHARGE_TIME);

                // Stå still medan attacken laddas.
                physics.setHorizontalVelocity(0f);

                if (!chargeHeld) {
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

                    state = CombatState.IDLE;
                    stateTime = 0f;
                }

                break;
        }
    }

    /**
     * Kallas exakt när PlayerController startar luft-hoppet/bakåtvolten.
     * Hitboxen lever oberoende av dash-state-maskinen.
     */
    public void startBackflipAttack(boolean facingLeft) {

        if (activeBackflipHitbox != null) {
            physics.destroyFixture(activeBackflipHitbox);
        }

        backflipDirection = facingLeft ? -1 : 1;
        backflipHitboxTime = 0f;
        backflipHitTargets.clear();

        PlayerAttackHitbox hitboxData = new PlayerAttackHitbox(
                this,
                PlayerAttackHitbox.Type.BACKFLIP);

        activeBackflipHitbox = physics.createAttackHitbox(
                BACKFLIP_HITBOX_WIDTH,
                BACKFLIP_HITBOX_HEIGHT,
                0f,
                BACKFLIP_HITBOX_OFFSET_Y,
                hitboxData);
    }

    private void updateBackflipHitbox(float delta) {

        if (activeBackflipHitbox == null) {
            return;
        }

        backflipHitboxTime += delta;

        if (backflipHitboxTime >= BACKFLIP_HITBOX_TIME) {
            physics.destroyFixture(activeBackflipHitbox);
            activeBackflipHitbox = null;
            backflipHitboxTime = 0f;
        }
    }

    public boolean isDashSequence() {

        return state == CombatState.STARTUP
                || state == CombatState.ACTIVE
                || state == CombatState.RECOVERY;
    }

    public String getStateName() {
        return state.name();
    }

    public float getChargePercent() {

        if (state == CombatState.CHARGING) {
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

    private void beginCharge(boolean facingLeft) {

        state = CombatState.CHARGING;

        chargeTime = 0f;
        chargePercent = 0f;
        stateTime = 0f;

        // Direction låses när laddningen börjar.
        dashDirection = facingLeft ? -1 : 1;

        physics.setHorizontalVelocity(0f);
    }

    private void beginStartup() {

        chargePercent = MathUtils.clamp(
                chargeTime / MAX_CHARGE_TIME,
                0f,
                1f);

        state = CombatState.STARTUP;
        stateTime = 0f;
    }

    private void beginDash() {

        state = CombatState.ACTIVE;
        stateTime = 0f;

        dashHitTargets.clear();

        PlayerAttackHitbox hitboxData = new PlayerAttackHitbox(
                this,
                PlayerAttackHitbox.Type.DASH);

        activeDashHitbox = physics.createAttackHitbox(
                DASH.hitboxWidth(),
                DASH.hitboxHeight(),
                DASH.hitboxOffsetX() * dashDirection,
                0f,
                hitboxData);
    }

    private void endDash() {

        if (activeDashHitbox != null) {

            physics.destroyFixture(activeDashHitbox);

            activeDashHitbox = null;
        }

        physics.setHorizontalVelocity(0f);

        state = CombatState.RECOVERY;
        stateTime = 0f;
    }

    public void hit(
            PlayerAttackHitbox.Type type,
            Damageable target) {

        switch (type) {
            case DASH:
                hitWithDash(target);
                break;

            case BACKFLIP:
                hitWithBackflip(target);
                break;
        }
    }

    private void hitWithDash(Damageable target) {

        if (state != CombatState.ACTIVE) {
            return;
        }

        if (dashHitTargets.contains(target)) {
            return;
        }

        dashHitTargets.add(target);

        /*
         * Samma damage/initiala knockback som tidigare.
         * Skillnaden är att mottagaren får veta att knockbacken
         * kom från DASH och kan bromsa den snabbare efteråt.
         */
        float power = 0.4f + chargePercent * 0.6f;

        target.takeDamage(
                DASH.damage() * power);

        target.applyKnockback(
                dashDirection
                        * DASH.knockback()
                        * power,
                0.8f * power,
                KnockbackMode.DASH);
    }

    private void hitWithBackflip(Damageable target) {

        if (activeBackflipHitbox == null) {
            return;
        }

        if (backflipHitTargets.contains(target)) {
            return;
        }

        backflipHitTargets.add(target);

        target.takeDamage(BACKFLIP_DAMAGE);

        target.applyKnockback(
                backflipDirection * BACKFLIP_KNOCKBACK_X,
                BACKFLIP_KNOCKBACK_Y,
                KnockbackMode.NORMAL);
    }

    public boolean isCharging() {
        return state == CombatState.CHARGING;
    }

    public boolean isDashing() {
        return state == CombatState.ACTIVE;
    }

    public boolean blocksMovement() {
        return state != CombatState.IDLE;
    }
}
