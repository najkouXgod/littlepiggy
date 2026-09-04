package com.niko.littlepiggy.ui;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Matrix4;

public class HealthBarRenderer {

    private static final float WORLD_WIDTH = 16f;
    private static final float WORLD_HEIGHT = 9f;

    private static final float X = 0.4f;
    private static final float Y = 8.2f;

    private static final float WIDTH = 4f;
    private static final float HEIGHT = 0.35f;

    private final ShapeRenderer renderer;
    private final Matrix4 projection;

    public HealthBarRenderer() {

        renderer = new ShapeRenderer();

        projection = new Matrix4();

        projection.setToOrtho2D(
                0,
                0,
                WORLD_WIDTH,
                WORLD_HEIGHT);
    }

    public void render(
            float health,
            float maxHealth) {

        float healthPercent = health / maxHealth;

        healthPercent = Math.max(
                0f,
                Math.min(1f, healthPercent));

        renderer.setProjectionMatrix(projection);

        renderer.begin(
                ShapeRenderer.ShapeType.Filled);

        // Bakgrund
        renderer.setColor(Color.DARK_GRAY);

        renderer.rect(
                X,
                Y,
                WIDTH,
                HEIGHT);

        // Health
        renderer.setColor(Color.RED);

        renderer.rect(
                X,
                Y,
                WIDTH * healthPercent,
                HEIGHT);

        renderer.end();
    }

    public void dispose() {
        renderer.dispose();
    }
}
