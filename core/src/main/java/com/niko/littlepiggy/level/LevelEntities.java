package com.niko.littlepiggy.level;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.utils.Array;

import com.niko.littlepiggy.Main;

import com.niko.littlepiggy.enemy.farmer.Farmer;
import com.niko.littlepiggy.enemy.dog.Dog;

import com.niko.littlepiggy.item.Apple;

import com.niko.littlepiggy.projectile.ProjectileManager;

import com.niko.littlepiggy.world.MapObjectSpawner;

import com.niko.littlepiggy.fx.ScreenShake;
import com.niko.littlepiggy.fx.HitStop;

public class LevelEntities {

    private final Array<Farmer> farmers;
    private final Array<Dog> dogs;
    private final Array<Apple> apples;

    private final Goal goal;

    public LevelEntities(
            TiledMap tiledMap,
            World world,
            Main game) {

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
    }

    public void update(
            float delta,
            Vector2 playerPosition,
            ProjectileManager projectileManager) {

        for (Farmer farmer : farmers) {

            projectileManager.addAll(
                    farmer.update(
                            delta,
                            playerPosition));
        }

        for (Dog dog : dogs) {

            dog.update(
                    delta,
                    playerPosition);
        }
    }

    public void cleanupDeadEnemies() {

        for (int i = farmers.size - 1; i >= 0; i--) {

            Farmer farmer = farmers.get(i);

            if (farmer.isDead()) {

                farmer.destroy();

                farmers.removeIndex(i);

                triggerEnemyDeathEffects();
            }
        }

        for (int i = dogs.size - 1; i >= 0; i--) {

            Dog dog = dogs.get(i);

            if (dog.isDead()) {

                dog.destroy();

                dogs.removeIndex(i);

                triggerEnemyDeathEffects();
            }
        }
    }

    private void triggerEnemyDeathEffects() {

        ScreenShake.addTrauma(0.4f);

        HitStop.trigger(0.08f);
    }

    public void cleanupCollectedItems() {

        for (int i = apples.size - 1; i >= 0; i--) {

            Apple apple = apples.get(i);

            if (apple.isCollected()) {

                apple.removeBody();

                apples.removeIndex(i);
            }
        }
    }

    public void render(SpriteBatch batch) {

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
    }

    public boolean areAllEnemiesDead() {

        return farmers.size == 0
                && dogs.size == 0;
    }

    public boolean isGoalReached() {

        return goal != null
                && goal.isReached();
    }

    public void resetGoal() {

        if (goal != null) {
            goal.reset();
        }
    }

    public Goal getGoal() {
        return goal;
    }
}
