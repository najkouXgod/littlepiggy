package com.niko.littlepiggy.level;

import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.math.Vector2;

import com.niko.littlepiggy.Main;
import com.niko.littlepiggy.world.GameMap;
import com.niko.littlepiggy.world.TerrainCollisionFactory;
import com.niko.littlepiggy.world.MapPointReader;

public class Level {

    private final GameMap map;
    private final LevelEntities entities;

    private final Vector2 playerSpawn;

    public Level(
            String levelName,
            World world,
            Main game) {

        map = new GameMap(levelName);

        TiledMap tiledMap = map.getTiledMap();

        playerSpawn = MapPointReader.getPoint(
                tiledMap,
                "PlayerSpawn");

        TerrainCollisionFactory.buildCollisions(
                world,
                tiledMap);

        entities = new LevelEntities(
                tiledMap,
                world,
                game);
    }

    public GameMap getMap() {
        return map;
    }

    public LevelEntities getEntities() {
        return entities;
    }

    public Vector2 getPlayerSpawn() {
        return playerSpawn.cpy();
    }

    public float getWorldWidth() {
        return map.getWorldWidth();
    }

    public float getWorldHeight() {
        return map.getWorldHeight();
    }

    public void dispose() {
        map.dispose();
    }
}
