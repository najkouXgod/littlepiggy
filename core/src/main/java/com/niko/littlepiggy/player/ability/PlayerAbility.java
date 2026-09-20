package com.niko.littlepiggy.player.ability;

import com.niko.littlepiggy.player.animation.PlayerAnimationState;
import com.niko.littlepiggy.player.event.PlayerEvent;

/**
 * Kontrakt för en spelarförmåga.
 *
 * En ability äger SIN egen state machine och SIN egen cooldown. Ingen
 * annan klass håller parallella booleans om den.
 *
 * Livscykel per frame (styrs av AbilityManager):
 * 1. update(delta) - tick state/cooldown
 * 2. isTriggered() && canStart() -> start() - försök starta
 */
public interface PlayerAbility {

    /** Vill spelarens input starta abilityn just nu? (Läser bara input.) */
    boolean isTriggered();

    /**
     * Tillåter abilityns EGNA regler start (cooldown, rätt state, i luften...).
     * Att andra abilities blockerar kollar AbilityManager.
     */
    boolean canStart();

    void start();

    void update(float delta);

    /** Pågår abilityn (inte bara cooldown)? */
    boolean isActive();

    /** Har abilityn tagit över kroppen så vanlig movement ska pausas? */
    boolean blocksMovement();

    /** Redo att användas (cooldown klar / inte upptagen). För HUD. */
    boolean isReady();

    /** 0..1, där 1 = redo. För HUD. */
    float getCooldownPercent();

    /** Animationen abilityn vill visa just nu, eller null om ingen. */
    PlayerAnimationState getAnimationState();

    /**
     * Ska spelaren just nu ignorera skada från kroppskontakt (t.ex. Dog)?
     * Gäller bara kontaktskada - projektiler m.m. påverkas inte.
     */
    default boolean isImmuneToContactDamage() {
        return false;
    }

    /** Events från världen. Ignorera det du inte bryr dig om. */
    default void onPlayerEvent(PlayerEvent event) {
    }

    /**
     * Anropas på alla ANDRA abilities när en ability startat.
     * Låter t.ex. en "pose"-ability avslutas när en annan tar över.
     */
    default void onOtherAbilityStarted(PlayerAbility other) {
    }
}
