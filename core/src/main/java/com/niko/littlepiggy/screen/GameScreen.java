package com.niko.littlepiggy.screen;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.Array;

import com.niko.littlepiggy.level.Goal;
import com.niko.littlepiggy.ui.HealthBarRenderer;
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
import com.badlogic.gdx.maps.tiled.TiledMap;

import com.niko.littlepiggy.world.GameMap;
import com.niko.littlepiggy.world.MapObjectSpawner;
import com.niko.littlepiggy.world.TerrainCollisionFactory;

public class GameScreen extends BaseScreen {

    private static final float CAMERA_MARGIN_X = 2f;
    private static final float CAMERA_MARGIN_Y = 1.5f;

    private DebugOverlay debugOverlay;

    private final Main game;
    private final String mapName;
    private final Goal goal;

    private final PhysicsManager physics;
    private final GameMap gameMap;
    private final ProjectileManager projectileManager;

    private final ProjectileRenderer projectileRenderer;
    private final HealthBarRenderer healthBarRenderer;

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
        healthBarRenderer = new HealthBarRenderer();

        gameMap = new GameMap(mapName);

        TiledMap tiledMap = gameMap.getTiledMap();

        World world = physics.getWorld();

        player = new Player(physics.getWorld(), 9, 11, game.getAssets());

        physics.setContactListener(player);
        farmers = MapObjectSpawner.spawnLayer(
                tiledMap,
                "Farmers",
                (tile, x, y) -> new Farmer(
                        world,
                        game.getAssets(),
                        x,
                        y));
        apples = MapObjectSpawner.spawnLayer(
                tiledMap,
                "Apples",
                (tile, x, y) -> new Apple(
                        world,
                        tile.getTextureRegion(),
                        x,
                        y));
        goal = MapObjectSpawner.spawnSingle(
                tiledMap,
                "Goal",
                (tile, x, y) -> new Goal(
                        world,
                        tile.getTextureRegion(),
                        x,
                        y));

        TerrainCollisionFactory.buildCollisions(
                world,
                tiledMap);

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

        for (int i = farmers.size - 1; i >= 0; i--) {

            Farmer farmer = farmers.get(i);

            if (farmer.isDead()) {
                farmer.destroy();
                farmers.removeIndex(i);
            }
        }

        if (player.isDead()) {
            game.setScreen(
                    new GameOverScreen(game, mapName));

            dispose();
            return;
        }
        if (goal != null && goal.isReached()) {
            game.setScreen(new GameOverScreen(game, mapName));
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

        updateCamera();

        player.update(delta);

        projectileManager.update(delta);

        batch.setProjectionMatrix(camera.combined);

        batch.begin();

        batch.draw(sky, camera.position.x - 16f, camera.position.y - 4.5f, 32f, 9f);

        batch.end();

        gameMap.render(camera);

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
        if (goal != null) {
            goal.render(batch);
        }

        batch.end();

        healthBarRenderer.render(
                player.getHealth(),
                player.getMaxHealth());

        if (debugOverlay != null) {
            debugOverlay.update(delta);
            debugOverlay.render();
        }
    }

    private void updateCamera() {

        float halfWidth = camera.viewportWidth * camera.zoom / 2f;

        float halfHeight = camera.viewportHeight * camera.zoom / 2f;

        float mapWidth = gameMap.getWorldWidth();

        float mapHeight = gameMap.getWorldHeight();

        float minX = halfWidth - CAMERA_MARGIN_X;

        float maxX = mapWidth - halfWidth + CAMERA_MARGIN_X;

        float minY = halfHeight - CAMERA_MARGIN_Y;

        float maxY = mapHeight - halfHeight + CAMERA_MARGIN_Y;

        float cameraX = MathUtils.clamp(
                player.getX(),
                minX,
                maxX);

        float cameraY = MathUtils.clamp(
                player.getY(),
                minY,
                maxY);

        camera.position.set(
                cameraX,
                cameraY,
                0f);

        camera.update();
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
        gameMap.dispose();
        physics.dispose();
        projectileRenderer.dispose();
        healthBarRenderer.dispose();
        if (debugOverlay != null)
            debugOverlay.dispose();
    }
}
