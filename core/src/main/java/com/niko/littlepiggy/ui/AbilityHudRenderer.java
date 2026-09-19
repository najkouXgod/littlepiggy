package com.niko.littlepiggy.ui;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Matrix4;

import com.niko.littlepiggy.assets.GameAssets;

public class AbilityHudRenderer {

    private static final float WORLD_WIDTH = 16f;
    private static final float WORLD_HEIGHT = 9f;

    private static final float X = 0.4f;
    private static final float Y = 0f;

    private static final float SIZE = 1.5f;
    private static final float GAP = 0.3f;

    private final SpriteBatch batch;
    private final Matrix4 projection;

    private final TextureRegion backflipIcon;
    private final TextureRegion dashIcon;

    public AbilityHudRenderer(GameAssets assets) {

        batch = new SpriteBatch();

        projection = new Matrix4();

        projection.setToOrtho2D(
                0f,
                0f,
                WORLD_WIDTH,
                WORLD_HEIGHT);

        Texture abilities = assets.getTexture(
                GameAssets.ABILITIES);

        backflipIcon = new TextureRegion(
                abilities,
                0,
                0,
                32,
                32);

        dashIcon = new TextureRegion(
                abilities,
                32,
                0,
                32,
                32);
    }

    public void render(
            boolean backflipReady,
            boolean dashReady) {

        batch.setProjectionMatrix(projection);

        batch.begin();

        /*
         * Backflip / CTRL
         */
        setReadyColor(backflipReady);

        batch.draw(
                backflipIcon,
                X,
                Y,
                SIZE,
                SIZE);

        /*
         * Dash / SHIFT
         */
        setReadyColor(dashReady);

        batch.draw(
                dashIcon,
                X + SIZE + GAP,
                Y,
                SIZE,
                SIZE);

        batch.setColor(Color.WHITE);

        batch.end();
    }

    private void setReadyColor(boolean ready) {

        if (ready) {

            batch.setColor(Color.WHITE);

        } else {

            batch.setColor(
                    0.3f,
                    0.3f,
                    0.3f,
                    0.75f);
        }
    }

    public void dispose() {
        batch.dispose();
    }
}
