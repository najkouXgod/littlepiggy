package com.niko.littlepiggy.player;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.utils.ObjectSet;

import com.niko.littlepiggy.combat.AttackData;
import com.niko.littlepiggy.combat.Damageable;

public class PlayerCombat {

    private enum CombatState {
        IDLE,
        CHARGING,
        STARTUP,
        ACTIVE,
        RECOVERY
    }

    private static final float MAX_CHARGE_TIME = 1.2f;

    private static final float MIN_DASH_SPEED = 5f;
    private static final float MAX_DASH_SPEED = 13f;

    private static final AttackData DASH = new AttackData(
            30f,
            5f,
            0.05f,
            0.22f,
            0.25f,
            0.55f,
            0.55f,
            0.45f);

    private final PlayerPhysics physics;

    private final ObjectSet<Damageable> hitTargets = new ObjectSet<>();

    private CombatState state = CombatState.IDLE;

    private float stateTime;
    private float chargeTime;
    private float chargePercent;

    private int dashDirection = 1;

    private Fixture activeHitbox;

    public PlayerCombat(PlayerPhysics physics) {
        this.physics = physics;
    }

    public void update(
            float delta,
            boolean chargeHeld,
            boolean facingLeft) {

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
        stateTime = 0f;

        /*
         * Direction låses när laddningen börjar.
         */
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

        hitTargets.clear();

        activeHitbox = physics.createAttackHitbox(
                DASH.hitboxWidth(),
                DASH.hitboxHeight(),
                DASH.hitboxOffsetX()
                        * dashDirection,
                this);
    }

    private void endDash() {

        if (activeHitbox != null) {

            physics.destroyFixture(
                    activeHitbox);

            activeHitbox = null;
        }

        physics.setHorizontalVelocity(0f);

        state = CombatState.RECOVERY;
        stateTime = 0f;
    }

    public void hit(Damageable target) {

        if (state != CombatState.ACTIVE) {
            return;
        }

        /*
         * Samma Farmer ska bara kunna träffas
         * en gång under samma dash.
         */
        if (hitTargets.contains(target)) {
            return;
        }

        hitTargets.add(target);

        /*
         * Halvladdad attack gör mindre skada/
         * knockback än full charge.
         */
        float power = 0.4f + chargePercent * 0.6f;

        target.takeDamage(
                DASH.damage() * power);

        target.applyKnockback(
                dashDirection
                        * DASH.knockback()
                        * power,
                0.8f * power);
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
