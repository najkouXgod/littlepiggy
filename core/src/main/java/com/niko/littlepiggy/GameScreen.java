package com.niko.littlepiggy;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.ScreenUtils;
import com.niko.littlepiggy.Apple;

public class GameScreen extends BaseScreen {

    private DebugOverlay debugOverlay;

    private final Main game;

    private final PhysicsManager physics;
    private final MapManager mapManager;

    private final Player player;
    private final Farmer farmer;
    private final Apple apple;

    private final SpriteBatch batch;

    public GameScreen(Main game, String mapName) {
        super();
        this.game = game;

        physics = new PhysicsManager();
        mapManager = new MapManager(mapName);
        mapManager.createCollisions(physics.getWorld());

        player = new Player(physics.getWorld(), 9, 11, game.getAssets());
        physics.setContactListener(player);

        farmer = new Farmer(physics.getWorld(), game.getAssets(), 14, 11);

        apple = new Apple(physics.getWorld(), game.getAssets(), 10, 11);

        batch = new SpriteBatch();
    }

    @Override
    public void show() {
        debugOverlay = new DebugOverlay(player);
    }

    @Override
    public void render(float delta) {
        ScreenUtils.clear(Color.BLUE);

        physics.step(delta);

        if (apple.isCollected()) {
            apple.removeBody();
        }

        player.update();

        camera.position.set(player.getX(), player.getY(), 0);
        camera.update();

        mapManager.render(camera);
        physics.renderDebug(camera);

        batch.setProjectionMatrix(camera.combined);

        batch.begin();

        player.render(batch);
        farmer.render(batch);
        apple.render(batch);

        batch.end();

        if (debugOverlay != null) {
            debugOverlay.update(delta);
            debugOverlay.render();
        }
    }

    @Override
    public void resize(int width, int height) {
        super.resize(width, height);
        if (debugOverlay != null)
            debugOverlay.resize(width, height);
    }

    @Override
    public void dispose() {
        batch.dispose();
        mapManager.dispose();
        physics.dispose();
        if (debugOverlay != null)
            debugOverlay.dispose();
    }
}
