package com.niko.littlepiggy.world;

import box2dLight.PointLight;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.maps.MapLayer;
import com.badlogic.gdx.maps.MapObject;
import com.badlogic.gdx.maps.MapProperties;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.utils.Array;

import com.niko.littlepiggy.lighting.LightingManager;

/**
 * Läser en Tiled OBJECT-layer (inte tile-layer, till skillnad från
 * MapObjectSpawner) och skapar en PointLight per objekt.
 *
 * Varför objekt-layer och inte samma tile-baserade mönster som
 * Farmers/Dogs/Apples? Lampor behöver egna per-instans-värden
 * (radie, färg) som en enskild tile inte kan bära - i Tiled sätter
 * du dessa som "Custom Properties" på varje enskilt objekt.
 *
 * Så här lägger du till en lampa i Tiled:
 * 1. Skapa (eller använd) ett Object Layer som heter exakt "Lights".
 * 2. Lägg till ett Point-objekt (eller vilken objekttyp som helst,
 *    bara positionen används) där lampan ska stå.
 * 3. Ge objektet Custom Properties (högerklicka -> Properties -> +):
 *      - "radius" (float), t.ex. 4.0
 *      - "color" (string), hex utan '#', t.ex. "FFAA55"
 * Båda är valfria, se DEFAULT_-konstanterna nedan för vad som
 * används om du hoppar över dem.
 */
public final class LightObjectSpawner {

    private static final float DEFAULT_RADIUS = 4f;
    private static final String DEFAULT_COLOR_HEX = "FFCC88";

    private LightObjectSpawner() {
    }

    public static Array<PointLight> spawnLights(
            TiledMap map,
            LightingManager lighting) {

        Array<PointLight> lights = new Array<>();

        MapLayer layer = map.getLayers().get("Lights");

        if (layer == null) {
            return lights;
        }

        for (MapObject object : layer.getObjects()) {

            MapProperties props = object.getProperties();

            float pixelX = props.get("x", 0f, Float.class);
            float pixelY = props.get("y", 0f, Float.class);

            float radius = props.get(
                    "radius",
                    DEFAULT_RADIUS,
                    Float.class);

            String colorHex = props.get(
                    "color",
                    DEFAULT_COLOR_HEX,
                    String.class);

            Color color = Color.valueOf(colorHex);

            float worldX = TileCoordinates.pixelToWorld(pixelX);
            float worldY = TileCoordinates.pixelToWorld(pixelY);

            PointLight light = lighting.createLamp(
                    worldX,
                    worldY,
                    radius,
                    color);

            lights.add(light);
        }

        return lights;
    }
}
