package com.niko.littlepiggy.player.combat;

import com.niko.littlepiggy.combat.KnockbackMode;

/**
 * Oföränderlig beskrivning av EN attack: hitbox, skada, knockback och
 * feedback (ljud, screenshake, hitstop).
 *
 * Abilities äger sina AttackSpecs som konstanter och ber PlayerCombat
 * starta dem. Då behöver PlayerCombat aldrig veta vad "Dash" eller
 * "Ground Slam" är - en ny attack är bara en ny AttackSpec.
 *
 * Skapas med AttackSpec.builder().
 */
public final class AttackSpec {

    /* Skada och knockback */
    public final float damage;
    public final float knockbackX;
    public final float knockbackY;
    public final KnockbackMode knockbackMode;

    /**
     * false: knockback X går i attackens riktning (directionX vid start).
     * true:  knockback X går bort från spelarens centrum (t.ex. Ground Slam).
     */
    public final boolean knockbackAwayFromOwner;

    /* Hitbox (relativt spelarens centrum, offsetX speglas av riktning) */
    public final float hitboxWidth;
    public final float hitboxHeight;
    public final float hitboxOffsetX;
    public final float hitboxOffsetY;

    /** Sekunder hitboxen lever. 0 = ability avslutar den själv (endAttack). */
    public final float duration;

    /* Feedback när attacken startar */
    public final String startSound;
    public final float startShake;

    /* Feedback per träffad target */
    public final String hitSound;
    public final float hitShake;
    public final float hitStop;

    /** true: hitstop bara på attackens första träff (bra mot flera enemies). */
    public final boolean hitStopOncePerAttack;

    private AttackSpec(Builder b) {
        damage = b.damage;
        knockbackX = b.knockbackX;
        knockbackY = b.knockbackY;
        knockbackMode = b.knockbackMode;
        knockbackAwayFromOwner = b.knockbackAwayFromOwner;
        hitboxWidth = b.hitboxWidth;
        hitboxHeight = b.hitboxHeight;
        hitboxOffsetX = b.hitboxOffsetX;
        hitboxOffsetY = b.hitboxOffsetY;
        duration = b.duration;
        startSound = b.startSound;
        startShake = b.startShake;
        hitSound = b.hitSound;
        hitShake = b.hitShake;
        hitStop = b.hitStop;
        hitStopOncePerAttack = b.hitStopOncePerAttack;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder {

        private float damage;
        private float knockbackX;
        private float knockbackY;
        private KnockbackMode knockbackMode = KnockbackMode.NORMAL;
        private boolean knockbackAwayFromOwner;
        private float hitboxWidth = 1f;
        private float hitboxHeight = 1f;
        private float hitboxOffsetX;
        private float hitboxOffsetY;
        private float duration;
        private String startSound;
        private float startShake;
        private String hitSound;
        private float hitShake;
        private float hitStop;
        private boolean hitStopOncePerAttack;

        private Builder() {
        }

        public Builder damage(float damage) {
            this.damage = damage;
            return this;
        }

        public Builder knockback(float x, float y) {
            this.knockbackX = x;
            this.knockbackY = y;
            return this;
        }

        public Builder knockbackMode(KnockbackMode mode) {
            this.knockbackMode = mode;
            return this;
        }

        public Builder knockbackAwayFromOwner(boolean value) {
            this.knockbackAwayFromOwner = value;
            return this;
        }

        public Builder hitbox(
                float width,
                float height,
                float offsetX,
                float offsetY) {

            this.hitboxWidth = width;
            this.hitboxHeight = height;
            this.hitboxOffsetX = offsetX;
            this.hitboxOffsetY = offsetY;
            return this;
        }

        public Builder duration(float seconds) {
            this.duration = seconds;
            return this;
        }

        public Builder startSound(String sound) {
            this.startSound = sound;
            return this;
        }

        public Builder startShake(float trauma) {
            this.startShake = trauma;
            return this;
        }

        public Builder hitSound(String sound) {
            this.hitSound = sound;
            return this;
        }

        public Builder hitShake(float trauma) {
            this.hitShake = trauma;
            return this;
        }

        public Builder hitStop(float seconds, boolean oncePerAttack) {
            this.hitStop = seconds;
            this.hitStopOncePerAttack = oncePerAttack;
            return this;
        }

        public AttackSpec build() {
            return new AttackSpec(this);
        }
    }
}
