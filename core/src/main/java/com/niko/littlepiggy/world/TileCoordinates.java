package com.niko.littlepiggy.world;

public final class TileCoordinates {

    public static final float PPM = 32f;

    private TileCoordinates() {
    }

    public static float pixelToWorld(float pixels) {
        return pixels / PPM;
    }

    public static float tileCenterX(
            int tileX,
            float tileWidth) {

        return pixelToWorld(
                tileX * tileWidth
                        + tileWidth / 2f);
    }

    public static float tileCenterY(
            int tileY,
            float tileHeight) {

        return pixelToWorld(
                tileY * tileHeight
                        + tileHeight / 2f);
    }
}
