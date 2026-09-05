package com.niko.littlepiggy.world;

import com.badlogic.gdx.Gdx;

import com.badlogic.gdx.maps.MapLayer;
import com.badlogic.gdx.maps.MapObject;

import com.badlogic.gdx.maps.objects.PolygonMapObject;
import com.badlogic.gdx.maps.objects.RectangleMapObject;

import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TiledMapTile;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;

import com.badlogic.gdx.math.Polygon;
import com.badlogic.gdx.math.Rectangle;

import com.badlogic.gdx.physics.box2d.*;

public final class TerrainCollisionFactory {

    private TerrainCollisionFactory() {
    }

    public static void buildCollisions(
            World world,
            TiledMap map) {

        for (MapLayer layer : map.getLayers()) {

            if (!(layer instanceof TiledMapTileLayer)) {
                continue;
            }

            TiledMapTileLayer tileLayer =
                    (TiledMapTileLayer) layer;

            float tileWidth =
                    tileLayer.getTileWidth();

            float tileHeight =
                    tileLayer.getTileHeight();

            for (int x = 0;
                    x < tileLayer.getWidth();
                    x++) {

                for (int y = 0;
                        y < tileLayer.getHeight();
                        y++) {

                    TiledMapTileLayer.Cell cell =
                            tileLayer.getCell(x, y);

                    if (cell == null) {
                        continue;
                    }

                    TiledMapTile tile =
                            cell.getTile();

                    if (tile == null) {
                        continue;
                    }

                    float tilePixelX =
                            x * tileWidth;

                    float tilePixelY =
                            y * tileHeight;

                    for (MapObject object
                            : tile.getObjects()) {

                        createCollision(
                                world,
                                object,
                                tilePixelX,
                                tilePixelY);
                    }
                }
            }
        }
    }

    private static void createCollision(
            World world,
            MapObject object,
            float tileX,
            float tileY) {

        if (object instanceof RectangleMapObject) {

            createRectangleCollision(
                    world,
                    (RectangleMapObject) object,
                    tileX,
                    tileY);

        } else if (object instanceof PolygonMapObject) {

            createPolygonCollision(
                    world,
                    (PolygonMapObject) object,
                    tileX,
                    tileY);
        }
    }

    private static void createRectangleCollision(
            World world,
            RectangleMapObject object,
            float tileX,
            float tileY) {

        Rectangle rect =
                object.getRectangle();

        BodyDef bodyDef = new BodyDef();

        bodyDef.type =
                BodyDef.BodyType.StaticBody;

        bodyDef.position.set(
                TileCoordinates.pixelToWorld(
                        tileX
                                + rect.x
                                + rect.width / 2f),

                TileCoordinates.pixelToWorld(
                        tileY
                                + rect.y
                                + rect.height / 2f));

        Body body =
                world.createBody(bodyDef);

        PolygonShape shape =
                new PolygonShape();

        shape.setAsBox(
                TileCoordinates.pixelToWorld(
                        rect.width / 2f),

                TileCoordinates.pixelToWorld(
                        rect.height / 2f));

        Fixture fixture =
                body.createFixture(shape, 0f);

        fixture.setUserData("ground");

        shape.dispose();
    }

    private static void createPolygonCollision(
            World world,
            PolygonMapObject object,
            float tileX,
            float tileY) {

        Polygon polygon =
                object.getPolygon();

        float[] vertices =
                polygon.getVertices();

        int vertexCount =
                vertices.length / 2;

        /*
         * Triangle = 3.
         * Box2D stöder convex polygon 3-8 vertices.
         */
        if (vertexCount < 3
                || vertexCount > 8) {

            Gdx.app.error(
                    "TerrainCollisionFactory",
                    "Unsupported polygon: "
                            + vertexCount
                            + " vertices");

            return;
        }

        float[] box2dVertices =
                new float[vertices.length];

        for (int i = 0;
                i < vertices.length;
                i += 2) {

            box2dVertices[i] =
                    TileCoordinates.pixelToWorld(
                            polygon.getX()
                                    + vertices[i]);

            box2dVertices[i + 1] =
                    TileCoordinates.pixelToWorld(
                            polygon.getY()
                                    + vertices[i + 1]);
        }

        BodyDef bodyDef =
                new BodyDef();

        bodyDef.type =
                BodyDef.BodyType.StaticBody;

        /*
         * Polygon-punkterna är lokala till tilen.
         */
        bodyDef.position.set(
                TileCoordinates.pixelToWorld(tileX),
                TileCoordinates.pixelToWorld(tileY));

        Body body =
                world.createBody(bodyDef);

        PolygonShape shape =
                new PolygonShape();

        shape.set(box2dVertices);

        Fixture fixture =
                body.createFixture(shape, 0f);

        fixture.setUserData("ground");

        shape.dispose();
    }
}
