package com.niko.littlepiggy.player;

import com.badlogic.gdx.math.Vector2;
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

    public Vector2 takeDamage(int amount, Vector2 knockback) {
        health -= amount;
        return knockback;
    }

}
