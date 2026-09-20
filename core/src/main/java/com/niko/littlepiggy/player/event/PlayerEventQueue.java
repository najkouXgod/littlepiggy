package com.niko.littlepiggy.player.event;

import com.badlogic.gdx.utils.Array;

/**
 * Enkel kö mellan Box2D-callbacks och spelarens gameplay-kod.
 *
 * GameContactListener körs mitt i world.step(), där man inte får
 * skapa/förstöra fixtures. Därför gör listenern bara push(...), och
 * Player tömmer kön (drain) i början av nästa update(), utanför
 * physics-steget, där abilities gärna får skapa hitboxar osv.
 *
 * Events som ingen bryr sig om ignoreras helt enkelt.
 */
public class PlayerEventQueue {

    public interface Handler {
        void onPlayerEvent(PlayerEvent event);
    }

    private final Array<PlayerEvent> pending = new Array<>(false, 8);
    private final Array<PlayerEvent> draining = new Array<>(false, 8);

    public void push(PlayerEvent event) {
        pending.add(event);
    }

    /**
     * Levererar alla väntande events i den ordning de kom.
     * Events som pushas medan vi levererar hamnar i nästa drain,
     * så en handler kan aldrig ge oss en oändlig loop.
     */
    public void drain(Handler handler) {

        if (pending.size == 0) {
            return;
        }

        draining.addAll(pending);
        pending.clear();

        for (int i = 0; i < draining.size; i++) {
            handler.onPlayerEvent(draining.get(i));
        }

        draining.clear();
    }
}
