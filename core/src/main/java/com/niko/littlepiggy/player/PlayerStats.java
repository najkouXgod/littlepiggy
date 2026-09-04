package com.niko.littlepiggy.player;

public class PlayerStats {

    private final float maxHealth;
    private float health;

    public PlayerStats(float maxHealth) {
        this.maxHealth = maxHealth;
        this.health = maxHealth;
    }

    public void heal(float amount) {
        health = Math.min(maxHealth, health + amount);
    }

    public void takeDamage(float amount) {
        health = Math.max(0, health - amount);
    }

    public float getHealth() {
        return health;
    }

    public float getMaxHealth() {
        return maxHealth;
    }

    public boolean isDead() {
        return health <= 0;
    }
}
