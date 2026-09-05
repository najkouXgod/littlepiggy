package com.niko.littlepiggy.world;

import com.badlogic.gdx.maps.MapLayer;

import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TiledMapTile;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;

import com.badlogic.gdx.utils.Array;

public final class MapObjectSpawner {

    private MapObjectSpawner() {
    }

    @FunctionalInterface
    public interface SpawnFn<T> {

        T spawn(
                TiledMapTile tile,
                float x,
                float y);
    }

    public static <T> Array<T> spawnLayer(
            TiledMap map,
            String layerName,
            SpawnFn<T> spawnFn) {

        Array<T> entities =
                new Array<>();

        TiledMapTileLayer layer =
                getTileLayer(
                        map,
                        layerName);

        if (layer == null) {
            return entities;
        }

        float tileWidth =
                layer.getTileWidth();

        float tileHeight =
                layer.getTileHeight();

        for (int x = 0;
                x < layer.getWidth();
                x++) {

            for (int y = 0;
                    y < layer.getHeight();
                    y++) {

                TiledMapTileLayer.Cell cell =
                        layer.getCell(x, y);

                if (cell == null
                        || cell.getTile() == null) {

                    continue;
                }

                TiledMapTile tile =
                        cell.getTile();

                float worldX =
                        TileCoordinates.tileCenterX(
                                x,
                                tileWidth);

                float worldY =
                        TileCoordinates.tileCenterY(
                                y,
                                tileHeight);

                T entity =
                        spawnFn.spawn(
                                tile,
                                worldX,
                                worldY);

                if (entity != null) {
                    entities.add(entity);
                }

                /*
                 * Tile:n var bara spawn-data.
                 * Entity:n renderas nu av Java.
                 */
                layer.setCell(
                        x,
                        y,
                        null);
            }
        }

        return entities;
    }

    public static <T> T spawnSingle(
            TiledMap map,
            String layerName,
            SpawnFn<T> spawnFn) {

        TiledMapTileLayer layer =
                getTileLayer(
                        map,
                        layerName);

        if (layer == null) {
            return null;
        }

        float tileWidth =
                layer.getTileWidth();

        float tileHeight =
                layer.getTileHeight();

        for (int x = 0;
                x < layer.getWidth();
                x++) {

            for (int y = 0;
                    y < layer.getHeight();
                    y++) {

                TiledMapTileLayer.Cell cell =
                        layer.getCell(x, y);

                if (cell == null
                        || cell.getTile() == null) {

                    continue;
                }

                TiledMapTile tile =
                        cell.getTile();

                float worldX =
                        TileCoordinates.tileCenterX(
                                x,
                                tileWidth);

                float worldY =
                        TileCoordinates.tileCenterY(
                                y,
                                tileHeight);

                T entity =
                        spawnFn.spawn(
                                tile,
                                worldX,
                                worldY);

                layer.setCell(
                        x,
                        y,
                        null);

                return entity;
            }
        }

        return null;
    }

    private static TiledMapTileLayer getTileLayer(
            TiledMap map,
            String layerName) {

        MapLayer layer =
                map.getLayers().get(
                        layerName);

        if (!(layer instanceof TiledMapTileLayer)) {
            return null;
        }

        return (TiledMapTileLayer) layer;
    }
}
