package com.niko.littlepiggy.combat;

public interface Damageable {

    void takeDamage(float amount);

    void applyKnockback(float x, float y);

    /**
     * Specialiserad knockback utan att alla Damageable-klasser måste
     * känna till varje attacktyp. Standardbeteendet är vanlig knockback.
     */
    default void applyKnockback(
            float x,
            float y,
            KnockbackMode mode) {

        applyKnockback(x, y);
    }

    boolean isDead();
}
