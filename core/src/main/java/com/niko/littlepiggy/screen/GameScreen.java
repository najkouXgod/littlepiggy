package com.niko.littlepiggy.screen;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.Array;

import com.niko.littlepiggy.projectile.Pellet;
import com.niko.littlepiggy.projectile.ProjectileManager;
import com.niko.littlepiggy.projectile.ProjectileRenderer;
import com.niko.littlepiggy.player.Player;
import com.niko.littlepiggy.player.PlayerStats;
import com.niko.littlepiggy.item.Apple;
import com.niko.littlepiggy.assets.GameAssets;
import com.niko.littlepiggy.enemy.Farmer;
import com.niko.littlepiggy.debug.DebugConfig;
import com.niko.littlepiggy.debug.DebugOverlay;
import com.niko.littlepiggy.Main;
import com.niko.littlepiggy.physics.PhysicsManager;
import com.niko.littlepiggy.world.MapManager;

public class GameScreen extends BaseScreen {

    private DebugOverlay debugOverlay;

    private final Main game;
    private final String mapName;

    private final PhysicsManager physics;
    private final MapManager mapManager;
    private final ProjectileManager projectileManager;
    private final ProjectileRenderer projectileRenderer;

    private final Player player;

    private final Array<Farmer> farmers;
    private final Array<Apple> apples;

    private final Texture sky;

    private final SpriteBatch batch;

    public GameScreen(Main game, String mapName) {
        super();
        this.game = game;
        this.mapName = mapName;

        physics = new PhysicsManager();
        projectileManager = new ProjectileManager(
                physics.getWorld());
        projectileRenderer = new ProjectileRenderer();
        mapManager = new MapManager(mapName);
        mapManager.createCollisions(physics.getWorld());

        player = new Player(physics.getWorld(), 9, 11, game.getAssets());

        physics.setContactListener(player);
        farmers = mapManager.createFarmers(
                physics.getWorld(),
                game.getAssets());
        apples = mapManager.createApples(
                physics.getWorld(),
                game.getAssets());

        sky = game.getAssets().getTexture(GameAssets.SKY);

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

        if (player.isDead()) {
            game.setScreen(
                    new GameOverScreen(game, mapName));

            dispose();
            return;
        }

        for (Farmer farmer : farmers) {

            projectileManager.addAll(
                    farmer.update(
                            delta,
                            player.getPosition()));
        }

        for (int i = apples.size - 1; i >= 0; i--) {

            Apple apple = apples.get(i);

            if (apple.isCollected()) {
                apple.removeBody();
                apples.removeIndex(i);
            }
        }

        player.update(delta);

        for (Farmer farmer : farmers) {
            projectileManager.addAll(
                    farmer.update(
                            delta,
                            player.getPosition()));
        }

        projectileManager.update(delta);

        camera.position.set(player.getX(), player.getY(), 0);
        camera.update();

        batch.setProjectionMatrix(camera.combined);

        batch.begin();

        batch.draw(sky, camera.position.x - 16f, camera.position.y - 4.5f, 32f, 9f);

        batch.end();

        mapManager.render(camera);

        projectileRenderer.render(camera, projectileManager);

        physics.renderDebug(camera);

        batch.begin();

        player.render(batch);
        for (Farmer farmer : farmers) {
            farmer.render(batch);
        }
        for (Apple apple : apples) {
            apple.render(batch);
        }

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
        projectileRenderer.dispose();
        if (debugOverlay != null)
            debugOverlay.dispose();
    }
}
