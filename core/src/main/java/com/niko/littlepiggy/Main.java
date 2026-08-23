package com.niko.littlepiggy;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.ScreenUtils;

/** {@link com.badlogic.gdx.ApplicationListener} implementation shared by all platforms. */
public class Main extends Game {

    private GameAssets assets;

    @Override
    public void create() {
        assets = new GameAssets();
        assets.loadAll();
        assets.finishLoading();

        setScreen( new GameScreen(this,"testmap"));
    }

    public GameAssets getAssets() {
        return assets;
    }

    public void dispose() {
        super.dispose();
        assets.dispose();
    }
}
