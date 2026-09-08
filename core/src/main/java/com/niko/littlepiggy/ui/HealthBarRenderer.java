package com.niko.littlepiggy.ui;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Matrix4;

public class HealthBarRenderer {

    private static final float WORLD_WIDTH = 16f;
    private static final float WORLD_HEIGHT = 9f;

    private static final float X = 0.4f;
    private static final float Y = 8.05f;

    private static final float WIDTH = 5.5f;
    private static final float HEIGHT = 0.55f;

    private static final float BORDER = 0.05f;

    private static final float FILL_SPEED = 3.5f;

    private static final float HEAL_FLASH_DURATION = 0.4f;
    private static final float HEAL_POP_DURATION = 0.25f;
    private static final float HEAL_POP_SCALE = 1.35f;

    private final ShapeRenderer renderer;
    private final Matrix4 projection;

    private float displayedHealthPercent = 1f;
    private float lastHealthPercent = 1f;

    private float healFlashTimer = 0f;
    private float healFlashFromPercent = 0f;
    private float healFlashToPercent = 0f;

    private float popTimer = 0f;

    public HealthBarRenderer() {
        renderer = new ShapeRenderer();
        projection = new Matrix4();
        projection.setToOrtho2D(0, 0, WORLD_WIDTH, WORLD_HEIGHT);
    }

    public void render(float health, float maxHealth, float delta) {

        float healthPercent = clamp01(health / maxHealth);

        if (healthPercent > lastHealthPercent + 0.0001f) {
            triggerHealEffect(lastHealthPercent, healthPercent);
        }

        lastHealthPercent = healthPercent;

        displayedHealthPercent = MathUtils.lerp(
                displayedHealthPercent,
                healthPercent,
                Math.min(1f, FILL_SPEED * delta));

        if (Math.abs(displayedHealthPercent - healthPercent) < 0.002f) {
            displayedHealthPercent = healthPercent;
        }

        updateTimers(delta);

        float pop = getPopScale();
        float width = WIDTH * pop;
        float height = HEIGHT * pop;
        float x = X - (width - WIDTH) / 2f;
        float y = Y - (height - HEIGHT) / 2f;

        renderer.setProjectionMatrix(projection);
        renderer.begin(ShapeRenderer.ShapeType.Filled);

        renderer.setColor(Color.BLACK);
        renderer.rect(x - BORDER, y - BORDER, width + BORDER * 2f, height + BORDER * 2f);

        renderer.setColor(Color.DARK_GRAY);
        renderer.rect(x, y, width, height);

        renderer.setColor(Color.RED);
        renderer.rect(x, y, width * displayedHealthPercent, height);

        if (healFlashTimer > 0f) {
            float alpha = healFlashTimer / HEAL_FLASH_DURATION;
            renderer.setColor(0.6f, 1f, 0.5f, alpha);
            float glowX = x + width * healFlashFromPercent;
            float glowWidth = width * (healFlashToPercent - healFlashFromPercent);
            renderer.rect(glowX, y, glowWidth, height);
        }

        renderer.end();
    }

    private void triggerHealEffect(float fromPercent, float toPercent) {
        healFlashTimer = HEAL_FLASH_DURATION;
        healFlashFromPercent = fromPercent;
        healFlashToPercent = toPercent;
        popTimer = HEAL_POP_DURATION;
    }

    private void updateTimers(float delta) {
        if (healFlashTimer > 0f) {
            healFlashTimer = Math.max(0f, healFlashTimer - delta);
        }
        if (popTimer > 0f) {
            popTimer = Math.max(0f, popTimer - delta);
        }
    }

    private float getPopScale() {
        if (popTimer <= 0f) {
            return 1f;
        }
        float t = popTimer / HEAL_POP_DURATION;
        return 1f + (HEAL_POP_SCALE - 1f) * t;
    }

    private float clamp01(float value) {
        return Math.max(0f, Math.min(1f, value));
    }

    public void dispose() {
        renderer.dispose();
    }
}
