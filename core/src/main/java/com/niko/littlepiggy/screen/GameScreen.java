package com.niko.littlepiggy.screen;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.niko.littlepiggy.render.SideScrollerCamera;
import com.niko.littlepiggy.render.WorldBackground;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
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

    private final WorldBackground background;
    private final SideScrollerCamera cameraFollow = new SideScrollerCamera();
    private final Viewport hudViewport = new FitViewport(WORLD_WIDTH, WORLD_HEIGHT);
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
        background = new WorldBackground(game.getAssets().getTexture(GameAssets.SKY),
                level.getMap().getTiledMap().getProperties());
        lighting.setAmbientLight(level.getMap().getTiledMap().getProperties()
                .get("ambientLight", 0.8f, Float.class));

        batch = new SpriteBatch();
    }

    @Override
    public void show() {

        debugOverlay = new DebugOverlay(player);
    }

    @Override
    public void render(float rawDelta) {

        ScreenUtils.clear(Color.BLACK);

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

        viewport.apply();
        batch.setProjectionMatrix(camera.combined);
        batch.begin();
        background.render(batch, camera);
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
        if (debugOverlay != null && debugOverlay.isVisible()) {
            physics.renderDebug(camera);
        }

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
        lighting.update(camera, viewport);
    }

    private void renderHud(float delta) {

        hudViewport.apply();

        healthBarRenderer.render(
                player.getHealth(),
                player.getMaxHealth(),
                delta);

        abilityHudRenderer.render(
                player.isBackflipReady(),
                player.isDashReady());

        if (debugOverlay != null) {

            // Debug overlay uses physical window coordinates.
            Gdx.gl.glViewport(0, 0, Gdx.graphics.getBackBufferWidth(),
                    Gdx.graphics.getBackBufferHeight());
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

        float viewWidth = viewport.getWorldWidth() * camera.zoom;
        float viewHeight = viewport.getWorldHeight() * camera.zoom;
        cameraFollow.update(player.getX(), player.getY(), player.getVelocity().x, delta,
                viewWidth, viewHeight, level.getWorldWidth(), level.getWorldHeight());
        // Shake is a render offset, never fed back into the tracking state.
        camera.position.set(
                SideScrollerCamera.clampCenter(cameraFollow.getX() + ScreenShake.getOffsetX(),
                        viewWidth, level.getWorldWidth()),
                SideScrollerCamera.clampCenter(cameraFollow.getY() + ScreenShake.getOffsetY(),
                        viewHeight, level.getWorldHeight()), 0f);

        camera.update();
    }

    @Override
    public void resize(
            int width,
            int height) {

        super.resize(
                width,
                height);

        hudViewport.update(width, height, true);
        updateCamera(0f);

        if (debugOverlay != null) {

            debugOverlay.resize(
                    width,
                    height);
        }
    }

    @Override
    public void dispose() {

        background.dispose();
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
