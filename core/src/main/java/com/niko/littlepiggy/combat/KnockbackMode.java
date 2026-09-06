package com.niko.littlepiggy.combat;

/**
 * Beskriver hur mottagaren ska behandla knockback efter själva impulsen.
 * NORMAL används av vanliga träffar/projektiler.
 * DASH låter t.ex. Farmer bromsa den horisontella farten snabbare.
 */
public enum KnockbackMode {
    NORMAL,
    DASH
}
