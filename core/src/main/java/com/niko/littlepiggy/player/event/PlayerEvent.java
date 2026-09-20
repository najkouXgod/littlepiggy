package com.niko.littlepiggy.player.event;

/**
 * Händelser från Box2D/världen som spelarens subsystem kan reagera på.
 *
 * Events är avsiktligt generella: de beskriver VAD som hände
 * (spelaren landade, träffade en enemy ovanifrån), inte vilken
 * ability som bryr sig om det. Varje subsystem väljer själv vad det
 * reagerar på och ignorerar resten.
 *
 * Används idag:
 *   LANDED, LEFT_GROUND        - härleds i Player.update() från grounded-state
 *   HIT_ENEMY_FROM_ABOVE       - skickas av GameContactListener
 *
 * Förberedda för senare (skickas inte än):
 *   ENTERED_WATER, EXITED_WATER
 */
public enum PlayerEvent {
    LANDED,
    LEFT_GROUND,
    HIT_ENEMY_FROM_ABOVE,
    ENTERED_WATER,
    EXITED_WATER
}
