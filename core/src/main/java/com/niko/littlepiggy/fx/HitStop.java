package com.niko.littlepiggy.fx;

/*
 * Fryser gameplay-uppdateringen (fysik, AI, spelarlogik) under
 * en kort period vid tunga träffar. Renderingen fortsätter som
 * vanligt så skärmskakning/animationer inte hackar till.
 *
 * Statisk av samma anledning som ScreenShake: en spelloop, en timer.
 */
public final class HitStop {

    private static float timer = 0f;

    private HitStop() {
    }

    /**
     * Triggar hit-stop. Om en längre hit-stop redan pågår
     * (t.ex. från en kill samma frame som en dash-träff)
     * vinner den längsta, ingen adderas ihop.
     */
    public static void trigger(float seconds) {
        timer = Math.max(timer, seconds);
    }

    public static boolean isActive() {
        return timer > 0f;
    }

    /** Anropas en gång per renderad frame med RÅ (oskalad) delta. */
    public static void update(float rawDelta) {

        if (timer > 0f) {
            timer -= rawDelta;
        }
    }
}
