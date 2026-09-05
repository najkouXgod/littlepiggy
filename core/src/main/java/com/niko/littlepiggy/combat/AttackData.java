package com.niko.littlepiggy.combat;

public class AttackData {

    private final float damage;
    private final float knockback;
    private final float startupTime;
    private final float activeTime;
    private final float recoveryTime;
    private final float hitboxWidth;
    private final float hitboxHeight;
    private final float hitboxOffsetX;

    public AttackData(
            float damage,
            float knockback,
            float startupTime,
            float activeTime,
            float recoveryTime,
            float hitboxWidth,
            float hitboxHeight,
            float hitboxOffsetX) {

        this.damage = damage;
        this.knockback = knockback;
        this.startupTime = startupTime;
        this.activeTime = activeTime;
        this.recoveryTime = recoveryTime;
        this.hitboxWidth = hitboxWidth;
        this.hitboxHeight = hitboxHeight;
        this.hitboxOffsetX = hitboxOffsetX;
    }

    public float damage() {
        return damage;
    }

    public float knockback() {
        return knockback;
    }

    public float startupTime() {
        return startupTime;
    }

    public float activeTime() {
        return activeTime;
    }

    public float recoveryTime() {
        return recoveryTime;
    }

    public float hitboxWidth() {
        return hitboxWidth;
    }

    public float hitboxHeight() {
        return hitboxHeight;
    }

    public float hitboxOffsetX() {
        return hitboxOffsetX;
    }
}
