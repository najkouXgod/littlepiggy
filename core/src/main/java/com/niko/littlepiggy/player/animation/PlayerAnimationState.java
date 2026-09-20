package com.niko.littlepiggy.player.animation;

/**
 * Vad PlayerAnimator ska VISA. Gameplay-systemen (abilities och movement)
 * bestämmer när det ändras; animatorn tolkar det aldrig vidare.
 *
 * CROUCHING och SWIMMING saknar grafik/gameplay än - animatorn visar
 * idle tills det finns sprites.
 */
public enum PlayerAnimationState {
    IDLE,
    RUNNING,
    CROUCHING,
    SWIMMING,
    DASH_CHARGE,
    DASH,
    BACKFLIP,
    GROUND_SLAM_WINDUP,
    GROUND_SLAM_FALL,
    GROUND_SLAM_IMPACT
}
