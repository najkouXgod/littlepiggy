package com.niko.littlepiggy.combat;

public interface Damageable {

    void takeDamage(float amount);

    void applyKnockback(float x, float y);

    boolean isDead();
}
