package com.niko.littlepiggy.fx;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;

/*
 * Vitblink vid träff, en instans per entitet/animator
 * (till skillnad från ScreenShake/HitStop som är globala
 * för hela skärmen - varje farmer måste kunna blinka
 * oberoende av de andra).
 */
public class HitFlash {

    private static final float DURATION = 0.1f;

    private float timer = 0f;

    public void trigger() {
        timer = DURATION;
    }

    public void update(float delta) {
        timer = Math.max(0f, timer - delta);
    }

    public boolean isActive() {
        return timer > 0f;
    }

    public void begin(SpriteBatch batch) {

        if (!isActive()) {
            return;
        }

        batch.setShader(HitFlashShader.get());

        HitFlashShader.get().setUniformf(
                "u_flashAmount",
                timer / DURATION);
    }

    public void end(SpriteBatch batch) {

        if (!isActive()) {
            return;
        }

        batch.setShader(null);
    }
}
