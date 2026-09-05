package com.niko.littlepiggy.world;

import com.badlogic.gdx.graphics.OrthographicCamera;

import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TmxMapLoader;

import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;

public class GameMap {

    private final TiledMap map;
    private final OrthogonalTiledMapRenderer renderer;

    public GameMap(String mapName) {

        map = new TmxMapLoader().load(
                "maps/testmap/"
                        + mapName
                        + ".tmx");

        renderer = new OrthogonalTiledMapRenderer(
                map,
                1f / TileCoordinates.PPM);
    }

    public TiledMap getTiledMap() {
        return map;
    }

    public float getWorldWidth() {

        int width = map.getProperties()
                .get("width", Integer.class);

        int tileWidth = map.getProperties()
                .get("tilewidth", Integer.class);

        return TileCoordinates.pixelToWorld(
                width * tileWidth);
    }

    public float getWorldHeight() {

        int height = map.getProperties()
                .get("height", Integer.class);

        int tileHeight = map.getProperties()
                .get("tileheight", Integer.class);

        return TileCoordinates.pixelToWorld(
                height * tileHeight);
    }

    public void render(
            OrthographicCamera camera) {

        renderer.setView(camera);
        renderer.render();
    }

    public void dispose() {
        renderer.dispose();
        map.dispose();
    }
}
