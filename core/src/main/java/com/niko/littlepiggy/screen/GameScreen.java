package com.niko.littlepiggy.screen;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.Array;

import com.niko.littlepiggy.screen.WinScreen;
import com.niko.littlepiggy.level.Goal;
import com.niko.littlepiggy.ui.HealthBarRenderer;
import com.niko.littlepiggy.projectile.Pellet;
import com.niko.littlepiggy.projectile.ProjectileManager;
import com.niko.littlepiggy.projectile.ProjectileRenderer;
import com.niko.littlepiggy.player.Player;
import com.niko.littlepiggy.player.PlayerStats;
import com.niko.littlepiggy.item.Apple;
import com.niko.littlepiggy.assets.GameAssets;
import com.niko.littlepiggy.enemy.farmer.Farmer;
import com.niko.littlepiggy.enemy.dog.Dog;
import com.niko.littlepiggy.debug.DebugConfig;
import com.niko.littlepiggy.debug.DebugOverlay;
import com.niko.littlepiggy.Main;
import com.niko.littlepiggy.physics.PhysicsManager;
import com.badlogic.gdx.maps.tiled.TiledMap;

import com.niko.littlepiggy.world.GameMap;
import com.niko.littlepiggy.world.MapObjectSpawner;
import com.niko.littlepiggy.world.TerrainCollisionFactory;
import com.niko.littlepiggy.fx.ScreenShake;
import com.niko.littlepiggy.fx.HitStop;
import com.niko.littlepiggy.lighting.LightingManager;
import com.niko.littlepiggy.world.LightObjectSpawner;

import box2dLight.PointLight;

public class GameScreen extends BaseScreen {

    private static final float DEATH_MARGIN = 3f;
    private static final float CAMERA_MARGIN_X = 2f;
    private static final float CAMERA_MARGIN_Y = 1.5f;

    /*
     * Fixed timestep för all gameplay-logik (fysik, AI, spelare).
     * Gör att spelet beter sig identiskt oavsett bildfrekvens,
     * och gör hit-stop trivialt: hit-stop = kör noll steg denna frame.
     */
    private static final float FIXED_TIMESTEP = 1f / 60f;
    private static final float MAX_FRAME_TIME = 0.25f;

    private float accumulator = 0f;

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
    private final Array<Dog> dogs;
    private final Array<Apple> apples;

    private final Texture sky;

    private final SpriteBatch batch;

    private final LightingManager lighting;
    private final Array<PointLight> lamps;
    private final PointLight playerLight;

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

        lighting = new LightingManager(world);

        player = new Player(physics.getWorld(), 3, 3, game.getAssets());

        physics.setContactListener(player);
        farmers = MapObjectSpawner.spawnLayer(
                tiledMap,
                "Farmers",
                (tile, x, y) -> new Farmer(
                        world,
                        game.getAssets(),
                        x,
                        y));
        dogs = MapObjectSpawner.spawnLayer(
                tiledMap,
                "Dogs",
                (tile, x, y) -> new Dog(
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

        lamps = LightObjectSpawner.spawnLights(tiledMap, lighting);

        /*
         * Spelarens egen ljuskälla ("ficklampa"/glöd). Byt färg/radie
         * fritt - se LightingManager.createLamp för vad parametrarna gör.
         */
        playerLight = lighting.createLamp(
                player.getX(),
                player.getY(),
                4f,
                new Color(1f, 0.85f, 0.6f, 1f));

        sky = game.getAssets().getTexture(GameAssets.SKY);

        batch = new SpriteBatch();
    }

    @Override
    public void show() {
        debugOverlay = new DebugOverlay(player);
    }

    @Override
    public void render(float rawDelta) {
        ScreenUtils.clear(Color.BLUE);

        HitStop.update(rawDelta);

        if (!HitStop.isActive()) {

            float delta = Math.min(rawDelta, MAX_FRAME_TIME);

            accumulator += delta;

            while (accumulator >= FIXED_TIMESTEP) {

                stepGameplay(FIXED_TIMESTEP);

                accumulator -= FIXED_TIMESTEP;
            }
        }

        for (int i = farmers.size - 1; i >= 0; i--) {

            Farmer farmer = farmers.get(i);

            if (farmer.isDead()) {
                farmer.destroy();
                farmers.removeIndex(i);
                ScreenShake.addTrauma(0.4f);
                HitStop.trigger(0.08f);
            }
        }

        for (int i = dogs.size - 1; i >= 0; i--) {

            Dog dog = dogs.get(i);

            if (dog.isDead()) {
                dog.destroy();
                dogs.removeIndex(i);
                ScreenShake.addTrauma(0.4f);
                HitStop.trigger(0.08f);
            }
        }

        if (player.isDead() || isPlayerOutOfBounds()) {

            game.setScreen(
                    new GameOverScreen(
                            game,
                            mapName));

            dispose();
            return;
        }
        if (goal != null && goal.isReached()) {
            if (farmers.size == 0 && dogs.size == 0) {
                game.setScreen(new WinScreen(game, mapName));
                dispose();
                return;
            }

            goal.reset();
        }

        for (int i = apples.size - 1; i >= 0; i--) {

            Apple apple = apples.get(i);

            if (apple.isCollected()) {
                apple.removeBody();
                apples.removeIndex(i);
            }
        }

        /*
         * Kameran (inklusive skärmskakning) uppdateras med RÅ delta,
         * inte fixed timestep - det är bara visuellt och ska inte
         * frysas eller hacka till av hit-stop.
         */
        updateCamera(rawDelta);

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
        for (Dog dog : dogs) {
            dog.render(batch);
        }
        for (Apple apple : apples) {
            apple.render(batch);
        }
        if (goal != null) {
            goal.render(batch);
        }

        batch.end();

        playerLight.setPosition(player.getX(), player.getY());
        lighting.update(camera);

        healthBarRenderer.render(
                player.getHealth(),
                player.getMaxHealth(),
                rawDelta);

        if (debugOverlay != null) {
            debugOverlay.update(rawDelta);
            debugOverlay.render();
        }
    }

    /**
     * All gameplay-logik som måste vara deterministisk och
     * som hit-stop ska kunna frysa. Körs 0, 1 eller flera
     * gånger per renderad frame beroende på bildfrekvens.
     */
    private void stepGameplay(float delta) {

        physics.step(delta);

        for (Farmer farmer : farmers) {

            projectileManager.addAll(
                    farmer.update(
                            delta,
                            player.getPosition()));
        }

        for (Dog dog : dogs) {
            dog.update(delta, player.getPosition());
        }

        player.update(delta);

        projectileManager.update(delta);
    }

    private boolean isPlayerOutOfBounds() {

        return player.getY() < -DEATH_MARGIN
                || player.getX() < -DEATH_MARGIN
                || player.getX() > gameMap.getWorldWidth() + DEATH_MARGIN;
    }

    private void updateCamera(float delta) {

        ScreenShake.update(delta);

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
                cameraX + ScreenShake.getOffsetX(),
                cameraY + ScreenShake.getOffsetY(),
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
        lighting.dispose();
        if (debugOverlay != null)
            debugOverlay.dispose();
    }
}
