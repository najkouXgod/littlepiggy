package com.niko.littlepiggy.screen;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

import com.niko.littlepiggy.Main;
import com.niko.littlepiggy.assets.GameAssets;

public class WinScreen extends BaseScreen {

    private static final float DISPLAY_TIME = 2f;

    private final Main game;
    private final String mapName;

    private final SpriteBatch batch;
    private final Texture texture;

    private float timer;

    public WinScreen(
            Main game,
            String mapName) {

        super();

        this.game = game;
        this.mapName = mapName;

        batch = new SpriteBatch();

        texture = game.getAssets()
                .getTexture(
                        GameAssets.WIN_SCREEN);
    }

    @Override
    public void render(float delta) {

        timer += delta;

        Gdx.gl.glClearColor(
                0f,
                0f,
                0f,
                1f);

        Gdx.gl.glClear(
                GL20.GL_COLOR_BUFFER_BIT);

        batch.setProjectionMatrix(
                camera.combined);

        batch.begin();

        batch.draw(
                texture,
                0f,
                0f,
                WORLD_WIDTH,
                WORLD_HEIGHT);

        batch.end();

        /*
         * Tillfälligt: starta om samma bana
         * efter win screen.
         *
         * Sen kan detta bytas till level 2.
         */
        if (timer >= DISPLAY_TIME) {

            game.setScreen(
                    new GameScreen(
                            game,
                            mapName));

            dispose();
        }
    }

    @Override
    public void dispose() {

        batch.dispose();

        /*
         * Disposa INTE texture.
         * AssetManager äger den.
         */
    }

    @Override
    public void show() {
    }

    @Override
    public void pause() {
    }

    @Override
    public void resume() {
    }

    @Override
    public void hide() {
    }
}
