package com.niko.littlepiggy.screen;

import com.niko.littlepiggy.Main;
import com.niko.littlepiggy.assets.GameAssets;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.ScreenUtils;

public class GameOverScreen extends BaseScreen {

    private static final float GAME_OVER_TIME = 2f;

    private final Main game;
    private final String mapName;

    private final SpriteBatch batch;
    private final Texture gameOverTexture;

    private float timer;

    public GameOverScreen(Main game, String mapName) {

        this.game = game;
        this.mapName = mapName;

        batch = new SpriteBatch();

        gameOverTexture = game.getAssets()
                .getTexture(GameAssets.GAME_OVER);
    }

    @Override
    public void render(float delta) {

        timer += delta;

        ScreenUtils.clear(0, 0, 0, 1);

        camera.update();

        batch.setProjectionMatrix(camera.combined);

        batch.begin();

        batch.draw(
                gameOverTexture,
                0,
                0,
                WORLD_WIDTH,
                WORLD_HEIGHT);

        batch.end();

        if (timer >= GAME_OVER_TIME) {
            game.setScreen(
                    new GameScreen(game, mapName));

            dispose();
        }
    }

    @Override
    public void show() {
    }

    @Override
    public void dispose() {
        batch.dispose();
    }
}
