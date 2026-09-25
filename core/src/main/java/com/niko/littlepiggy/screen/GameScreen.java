package com.niko.littlepiggy.screen;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.math.Vector2;

import box2dLight.PointLight;

import com.niko.littlepiggy.Main;

import com.niko.littlepiggy.assets.GameAssets;

import com.niko.littlepiggy.debug.DebugOverlay;

import com.niko.littlepiggy.fx.HitStop;
import com.niko.littlepiggy.fx.ScreenShake;

import com.niko.littlepiggy.level.Level;
import com.niko.littlepiggy.level.LevelEntities;

import com.niko.littlepiggy.lighting.LightingManager;

import com.niko.littlepiggy.physics.PhysicsManager;

import com.niko.littlepiggy.player.Player;

import com.niko.littlepiggy.projectile.ProjectileManager;
import com.niko.littlepiggy.projectile.ProjectileRenderer;

import com.niko.littlepiggy.ui.AbilityHudRenderer;
import com.niko.littlepiggy.ui.HealthBarRenderer;

import com.niko.littlepiggy.lighting.LightObjectSpawner;

public class GameScreen extends BaseScreen {

    private static final float DEATH_MARGIN = 3f;

    private static final float CAMERA_MARGIN_X = 2f;
    private static final float CAMERA_MARGIN_Y = 1.5f;

    private static final float FIXED_TIMESTEP = 1f / 60f;
    private static final float MAX_FRAME_TIME = 0.25f;

    private final Main game;
    private final String levelName;

    private final PhysicsManager physics;

    private final ProjectileManager projectileManager;
    private final ProjectileRenderer projectileRenderer;

    private final Level level;
    private final LevelEntities entities;

    private final Player player;

    private final LightingManager lighting;
    private final Array<PointLight> lamps;

    private final HealthBarRenderer healthBarRenderer;
    private final AbilityHudRenderer abilityHudRenderer;

    private final Texture sky;
    private final SpriteBatch batch;

    private DebugOverlay debugOverlay;

    private float accumulator;

    public GameScreen(
            Main game,
            String levelName) {

        super();

        this.game = game;
        this.levelName = levelName;

        /*
         * Physics
         */
        physics = new PhysicsManager();

        World world = physics.getWorld();

        /*
         * Level
         *
         * Level skapar:
         * - GameMap
         * - terrain collisions
         * - LevelEntities
         */
        level = new Level(
                levelName,
                world,
                game);

        entities = level.getEntities();

        Vector2 playerSpawn = level.getPlayerSpawn();
        player = new Player(
                world,
                playerSpawn.x,
                playerSpawn.y,
                game.getAssets());

        physics.setContactListener(player);

        /*
         * Projectiles
         */
        projectileManager = new ProjectileManager(world);

        projectileRenderer = new ProjectileRenderer();

        /*
         * Lighting
         */
        lighting = new LightingManager(world);

        lamps = LightObjectSpawner.spawnLights(
                level.getMap().getTiledMap(),
                lighting);

        /*
         * HUD
         */
        healthBarRenderer = new HealthBarRenderer();

        abilityHudRenderer = new AbilityHudRenderer(
                game.getAssets());

        /*
         * Rendering
         */
        sky = game.getAssets().getTexture(
                GameAssets.SKY);

        batch = new SpriteBatch();
    }

    @Override
    public void show() {

        debugOverlay = new DebugOverlay(player);
    }

    @Override
    public void render(float rawDelta) {

        ScreenUtils.clear(Color.BLUE);

        /*
         * Hit stop uppdateras med riktig frame-delta.
         */
        HitStop.update(rawDelta);

        /*
         * Gameplay stannar under hit stop.
         */
        if (!HitStop.isActive()) {

            float delta = Math.min(
                    rawDelta,
                    MAX_FRAME_TIME);

            accumulator += delta;

            while (accumulator >= FIXED_TIMESTEP) {

                stepGameplay(
                        FIXED_TIMESTEP);

                accumulator -= FIXED_TIMESTEP;
            }
        }

        /*
         * Ta bort döda enemies och plockade items.
         */
        entities.cleanupDeadEnemies();
        entities.cleanupCollectedItems();

        /*
         * Game over.
         */
        if (player.isDead()
                || isPlayerOutOfBounds()) {

            game.setScreen(
                    new GameOverScreen(
                            game,
                            levelName));

            dispose();

            return;
        }

        /*
         * Level klar.
         *
         * Goal fungerar endast när alla enemies är döda.
         */
        if (entities.isGoalReached()) {

            if (entities.areAllEnemiesDead()) {

                game.setScreen(
                        new WinScreen(
                                game,
                                levelName));

                dispose();

                return;
            }

            entities.resetGoal();
        }

        /*
         * Camera och screen shake är visuellt,
         * därför används rawDelta.
         */
        updateCamera(rawDelta);

        renderWorld();

        renderHud(rawDelta);
    }

    private void stepGameplay(float delta) {

        /*
         * Box2D.
         */
        physics.step(delta);

        /*
         * Alla level entities.
         *
         * Farmer-projectiles läggs också till här.
         */
        entities.update(
                delta,
                player.getPosition(),
                projectileManager);

        /*
         * Player.
         */
        player.update(delta);

        /*
         * Projectiles.
         */
        projectileManager.update(delta);
    }

    private void renderWorld() {

        batch.setProjectionMatrix(
                camera.combined);

        /*
         * Background anchored to the map, in world coordinates.
         * Include the camera margins so the image covers the map edges.
         */
        batch.begin();
        batch.draw(
                sky,
                -CAMERA_MARGIN_X,
                -CAMERA_MARGIN_Y,
                level.getWorldWidth() + 2f * CAMERA_MARGIN_X,
                level.getWorldHeight() + 2f * CAMERA_MARGIN_Y);

        batch.end();

        /*
         * Tiled map.
         */
        level.getMap().render(camera);

        /*
         * Projectiles.
         */
        projectileRenderer.render(
                camera,
                projectileManager);

        /*
         * Box2D debug.
         *
         * Vi kan senare koppla detta till F1/debug-mode.
         */
        physics.renderDebug(camera);

        /*
         * Entities.
         */
        batch.begin();

        player.render(batch);

        entities.render(batch);

        batch.end();

        /*
         * Lights.
         */
        lighting.update(camera);
    }

    private void renderHud(float delta) {

        healthBarRenderer.render(
                player.getHealth(),
                player.getMaxHealth(),
                delta);

        abilityHudRenderer.render(
                player.isBackflipReady(),
                player.isDashReady());

        if (debugOverlay != null) {

            debugOverlay.update(delta);
            debugOverlay.render();
        }
    }

    private boolean isPlayerOutOfBounds() {

        return player.getY() < -DEATH_MARGIN

                || player.getX() < -DEATH_MARGIN

                || player.getX() > level.getWorldWidth()
                        + DEATH_MARGIN;
    }

    private void updateCamera(float delta) {

        ScreenShake.update(delta);

        float halfWidth = camera.viewportWidth
                * camera.zoom
                / 2f;

        float halfHeight = camera.viewportHeight
                * camera.zoom
                / 2f;

        float mapWidth = level.getWorldWidth();

        float mapHeight = level.getWorldHeight();

        float minX = halfWidth
                - CAMERA_MARGIN_X;

        float maxX = mapWidth
                - halfWidth
                + CAMERA_MARGIN_X;

        float minY = halfHeight
                - CAMERA_MARGIN_Y;

        float maxY = mapHeight
                - halfHeight
                + CAMERA_MARGIN_Y;

        float cameraX = MathUtils.clamp(
                player.getX(),
                minX,
                maxX);
        float cameraY = MathUtils.clamp(
                player.getY() + camera.viewportHeight * camera.zoom * 0.25f,
                minY,
                maxY);

        camera.position.set(
                cameraX
                        + ScreenShake.getOffsetX(),

                cameraY
                        + ScreenShake.getOffsetY(),

                0f);

        camera.update();
    }

    @Override
    public void resize(
            int width,
            int height) {

        super.resize(
                width,
                height);

        if (debugOverlay != null) {

            debugOverlay.resize(
                    width,
                    height);
        }
    }

    @Override
    public void dispose() {

        batch.dispose();

        level.dispose();

        physics.dispose();

        projectileRenderer.dispose();

        healthBarRenderer.dispose();

        abilityHudRenderer.dispose();

        lighting.dispose();

        if (debugOverlay != null) {
            debugOverlay.dispose();
        }
    }
}
