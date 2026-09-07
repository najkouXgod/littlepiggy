package com.niko.littlepiggy.fx;

import com.badlogic.gdx.math.MathUtils;

/*
 * Skärmskakning byggd på "trauma"-modellen:
 * https://www.youtube.com/watch?v=tu-Qe66AvtY (Squirrel Eiserloh, GDC)
 *
 * Trauma går från 0 till 1 och avtar över tid. Faktisk skakning
 * är trauma^2, så små träffar knappt märks men en kill eller en
 * träff mot spelaren känns rejäl.
 *
 * Statisk med flit: hela spelet har en enda kamera just nu.
 * Om vi någon gång behöver flera kameror samtidigt (split-screen,
 * flera spelare) blir detta en vanlig instans istället för statisk.
 */
public final class ScreenShake {

    private static final float DECAY_PER_SECOND = 1.8f;
    private static final float MAX_OFFSET = 0.35f; // world units (tiles)
    private static final float FREQUENCY = 18f; // skakningar per sekund

    private static float trauma = 0f;
    private static float time = 0f;

    private ScreenShake() {
    }

    /**
     * amount 0..1. Lägg till från valfri träff/event.
     * Exempel: liten träff ~0.2, kill ~0.4, spelaren tar skada ~0.5.
     */
    public static void addTrauma(float amount) {
        trauma = MathUtils.clamp(trauma + amount, 0f, 1f);
    }

    public static void update(float delta) {
        time += delta;
        trauma = MathUtils.clamp(
                trauma - DECAY_PER_SECOND * delta,
                0f,
                1f);
    }

    public static float getOffsetX() {

        float shake = trauma * trauma;

        return MAX_OFFSET
                * shake
                * MathUtils.sinDeg(time * FREQUENCY * 360f);
    }

    public static float getOffsetY() {

        float shake = trauma * trauma;

        return MAX_OFFSET
                * shake
                * MathUtils.sinDeg(
                        time * FREQUENCY * 360f * 1.3f + 90f);
    }
}
