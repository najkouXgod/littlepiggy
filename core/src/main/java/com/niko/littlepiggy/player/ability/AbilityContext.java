package com.niko.littlepiggy.player.ability;

import com.niko.littlepiggy.player.combat.PlayerCombat;
import com.niko.littlepiggy.player.input.PlayerInput;
import com.niko.littlepiggy.player.movement.PlayerMovement;
import com.niko.littlepiggy.player.physics.PlayerPhysics;

/**
 * Det abilities behöver för att göra sitt jobb, samlat på ett ställe så
 * att konstruktorerna inte växer när en ny ability behöver något nytt.
 *
 * Det är en vanlig konstruktor-parameter, inget DI-ramverk.
 * Movement exponeras bara som facing-riktning (läs, aldrig skriv).
 */
public final class AbilityContext {

    public final PlayerInput input;
    public final PlayerPhysics physics;
    public final PlayerCombat combat;

    private final PlayerMovement movement;

    public AbilityContext(
            PlayerInput input,
            PlayerPhysics physics,
            PlayerCombat combat,
            PlayerMovement movement) {

        this.input = input;
        this.physics = physics;
        this.combat = combat;
        this.movement = movement;
    }

    /** -1 om spelaren tittar åt vänster, annars 1. */
    public float facingDirection() {
        return movement.isFacingLeft() ? -1f : 1f;
    }
}
