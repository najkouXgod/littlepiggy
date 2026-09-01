package com.niko.littlepiggy.player;

public class PlayerStats {

    private final float maxHealth;
    private float health;

    public PlayerStats(float maxHealth) {
        this.maxHealth = maxHealth;
        this.health = maxHealth;
    }

    public void healUp(float amount) {
        health = Math.min(maxHealth, health + amount);
    }

    public void takeDamage(float amount) {
        health = Math.max(0, health - amount);

        System.out.println("Player HP: " + health);
    }

    public float getHealth() {
        return health;
    }

    public boolean isDead() {
        return health <= 0;
    }
}
