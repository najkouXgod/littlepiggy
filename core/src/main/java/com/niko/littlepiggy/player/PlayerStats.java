package com.niko.littlepiggy.player;

import com.niko.littlepiggy.player.Player;

public class PlayerStats {

    private Player player;
    private int health;

    public PlayerStats(int health) {
        this.health = health;
    }

    public void healUp(int amount) {
        health += amount;
    }

    public void takeDamage(int amount) {
        health -= amount;
        player.applyKnockback(2, 0);
    }

}
