package com.niko.littlepiggy.player.movement;

/**
 * Hur spelaren rör sig "till vardags" (när ingen ability tagit över).
 * Abilities är ORTOGONALA mot detta: en Dash i vatten och en Dash på land
 * är samma DashAbility.
 */
public enum MovementMode {
    NORMAL,

    /** Förberedd, inte implementerad än. Se PlayerMovement.update(). */
    WATER
}
