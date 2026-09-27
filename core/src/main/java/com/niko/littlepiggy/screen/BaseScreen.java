package com.niko.littlepiggy.screen;

import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;

public abstract class BaseScreen implements Screen {
    protected static final float WORLD_WIDTH = 16f;
    protected static final float WORLD_HEIGHT = 9f;

    protected final OrthographicCamera camera;
    protected final Viewport viewport;

    public BaseScreen() {
        camera = new OrthographicCamera();
        viewport = new FitViewport(WORLD_WIDTH, WORLD_HEIGHT, camera);
        camera.zoom = 1f;
        camera.viewportWidth = WORLD_WIDTH;
        camera.viewportHeight = WORLD_HEIGHT;
        camera.position.set(WORLD_WIDTH / 2f, WORLD_HEIGHT / 2f, 0);
        camera.update();
    }

    @Override
    public void resize(int width, int height) {
        viewport.update(width, height, false);
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
