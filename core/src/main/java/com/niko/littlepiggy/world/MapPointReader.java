package com.niko.littlepiggy.world;

import com.badlogic.gdx.maps.MapLayer;
import com.badlogic.gdx.maps.MapObject;
import com.badlogic.gdx.maps.objects.PointMapObject;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.math.Vector2;

public final class MapPointReader {

    private MapPointReader() {
    }

    public static Vector2 getPoint(
            TiledMap map,
            String layerName) {

        MapLayer layer =
                map.getLayers().get(layerName);

        if (layer == null) {
            throw new IllegalStateException(
                    "Map layer saknas: "
                            + layerName);
        }

        for (MapObject object
                : layer.getObjects()) {

            if (object
                    instanceof PointMapObject) {

                Vector2 point =
                        ((PointMapObject) object)
                                .getPoint();

                return new Vector2(
                        TileCoordinates.pixelToWorld(
                                point.x),

                        TileCoordinates.pixelToWorld(
                                point.y));
            }
        }

        throw new IllegalStateException(
                "Ingen Point hittades i lagret: "
                        + layerName);
    }
}
