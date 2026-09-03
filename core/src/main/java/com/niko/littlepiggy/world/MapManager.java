package com.niko.littlepiggy.world;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.OrthographicCamera;

import com.badlogic.gdx.maps.MapLayer;
import com.badlogic.gdx.maps.MapObject;

import com.badlogic.gdx.maps.objects.EllipseMapObject;
import com.badlogic.gdx.maps.objects.PolygonMapObject;
import com.badlogic.gdx.maps.objects.PolylineMapObject;
import com.badlogic.gdx.maps.objects.RectangleMapObject;
import com.badlogic.gdx.utils.Array;

import com.niko.littlepiggy.item.Apple;
import com.niko.littlepiggy.assets.GameAssets;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TiledMapTile;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
import com.badlogic.gdx.maps.tiled.TmxMapLoader;

import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;

import com.badlogic.gdx.math.Ellipse;
import com.badlogic.gdx.math.Polygon;
import com.badlogic.gdx.math.Polyline;
import com.badlogic.gdx.math.Rectangle;

import com.badlogic.gdx.physics.box2d.*;

public class MapManager {

    private static final float PPM = 32f;

    private final TiledMap map;
    private final OrthogonalTiledMapRenderer mapRenderer;

    public MapManager(String mapName) {

        map = new TmxMapLoader().load(
                "maps/testmap/" + mapName + ".tmx");

        mapRenderer = new OrthogonalTiledMapRenderer(
                map,
                1f / PPM);
    }

    public void createCollisions(World world) {

        for (MapLayer layer : map.getLayers()) {
            if ("Apples".equals(layer.getName())) {
                continue;
            }
            if (!(layer instanceof TiledMapTileLayer)) {
                continue;
            }

            TiledMapTileLayer tileLayer = (TiledMapTileLayer) layer;

            float tileWidth = tileLayer.getTileWidth();
            float tileHeight = tileLayer.getTileHeight();

            for (int x = 0; x < tileLayer.getWidth(); x++) {

                for (int y = 0; y < tileLayer.getHeight(); y++) {

                    TiledMapTileLayer.Cell cell = tileLayer.getCell(x, y);

                    if (cell == null) {
                        continue;
                    }

                    TiledMapTile tile = cell.getTile();

                    if (tile == null) {
                        continue;
                    }

                    /*
                     * Pixelpositionen för denna tile
                     * i hela kartan.
                     */
                    float tileX = x * tileWidth;
                    float tileY = y * tileHeight;

                    for (MapObject object : tile.getObjects()) {

                        createCollisionForObject(
                                world,
                                object,
                                tileX,
                                tileY);
                    }
                }
            }
        }
    }

    private void createCollisionForObject(
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
        }

        else if (object instanceof PolygonMapObject) {

            createPolygonCollision(
                    world,
                    (PolygonMapObject) object,
                    tileX,
                    tileY);
        }

        else if (object instanceof PolylineMapObject) {

            createPolylineCollision(
                    world,
                    (PolylineMapObject) object,
                    tileX,
                    tileY);
        }

        else if (object instanceof EllipseMapObject) {

            createEllipseCollision(
                    world,
                    (EllipseMapObject) object,
                    tileX,
                    tileY);
        }
    }

    public Array<Apple> createApples(
            World world,
            GameAssets assets) {

        Array<Apple> apples = new Array<>();

        MapLayer layer = map.getLayers().get("Apples");

        if (!(layer instanceof TiledMapTileLayer)) {
            return apples;
        }

        TiledMapTileLayer appleLayer = (TiledMapTileLayer) layer;

        float tileWidth = appleLayer.getTileWidth();
        float tileHeight = appleLayer.getTileHeight();

        for (int x = 0; x < appleLayer.getWidth(); x++) {

            for (int y = 0; y < appleLayer.getHeight(); y++) {

                TiledMapTileLayer.Cell cell = appleLayer.getCell(x, y);

                if (cell == null) {
                    continue;
                }

                TiledMapTile tile = cell.getTile();

                if (tile == null) {
                    continue;
                }

                float worldX = (x * tileWidth + tileWidth / 2f) / PPM;

                float worldY = (y * tileHeight + tileHeight / 2f) / PPM;

                apples.add(
                        new Apple(
                                world,
                                tile.getTextureRegion(),
                                worldX,
                                worldY));
                appleLayer.setCell(x, y, null);
            }
        }

        return apples;
    }

    private void createRectangleCollision(
            World world,
            RectangleMapObject object,
            float tileX,
            float tileY) {

        Rectangle rect = object.getRectangle();

        BodyDef bodyDef = new BodyDef();
        bodyDef.type = BodyDef.BodyType.StaticBody;

        bodyDef.position.set(
                (tileX + rect.x + rect.width / 2f) / PPM,
                (tileY + rect.y + rect.height / 2f) / PPM);

        Body body = world.createBody(bodyDef);

        PolygonShape shape = new PolygonShape();

        shape.setAsBox(
                rect.width / 2f / PPM,
                rect.height / 2f / PPM);

        Fixture fixture = body.createFixture(shape, 0f);

        fixture.setUserData("ground");

        shape.dispose();
    }

    private void createPolygonCollision(
            World world,
            PolygonMapObject object,
            float tileX,
            float tileY) {

        Polygon polygon = object.getPolygon();

        float[] vertices = polygon.getVertices();

        int vertexCount = vertices.length / 2;

        /*
         * Box2D PolygonShape klarar max 8 vertices.
         */
        if (vertexCount < 3 || vertexCount > 8) {

            Gdx.app.error(
                    "MapManager",
                    "Hoppar över polygon med "
                            + vertexCount
                            + " hörn");

            return;
        }

        float[] box2dVertices = new float[vertices.length];

        for (int i = 0; i < vertices.length; i += 2) {

            box2dVertices[i] = (polygon.getX()
                    + vertices[i]) / PPM;

            box2dVertices[i + 1] = (polygon.getY()
                    + vertices[i + 1]) / PPM;
        }

        BodyDef bodyDef = new BodyDef();
        bodyDef.type = BodyDef.BodyType.StaticBody;

        /*
         * Bodyn placeras vid tile:ns position.
         * Polygonens coordinates är lokala.
         */
        bodyDef.position.set(
                tileX / PPM,
                tileY / PPM);

        Body body = world.createBody(bodyDef);

        PolygonShape shape = new PolygonShape();

        shape.set(box2dVertices);

        Fixture fixture = body.createFixture(shape, 0f);

        fixture.setUserData("ground");

        shape.dispose();
    }

    private void createPolylineCollision(
            World world,
            PolylineMapObject object,
            float tileX,
            float tileY) {

        Polyline polyline = object.getPolyline();

        float[] vertices = polyline.getVertices();

        if (vertices.length < 4) {
            return;
        }

        float[] box2dVertices = new float[vertices.length];

        for (int i = 0; i < vertices.length; i += 2) {

            box2dVertices[i] = (polyline.getX()
                    + vertices[i]) / PPM;

            box2dVertices[i + 1] = (polyline.getY()
                    + vertices[i + 1]) / PPM;
        }

        BodyDef bodyDef = new BodyDef();

        bodyDef.type = BodyDef.BodyType.StaticBody;

        bodyDef.position.set(
                tileX / PPM,
                tileY / PPM);

        Body body = world.createBody(bodyDef);

        ChainShape shape = new ChainShape();

        shape.createChain(box2dVertices);

        Fixture fixture = body.createFixture(shape, 0f);

        fixture.setUserData("ground");

        shape.dispose();
    }

    private void createEllipseCollision(
            World world,
            EllipseMapObject object,
            float tileX,
            float tileY) {

        Ellipse ellipse = object.getEllipse();

        float centerX = tileX
                + ellipse.x
                + ellipse.width / 2f;

        float centerY = tileY
                + ellipse.y
                + ellipse.height / 2f;

        BodyDef bodyDef = new BodyDef();

        bodyDef.type = BodyDef.BodyType.StaticBody;

        bodyDef.position.set(
                centerX / PPM,
                centerY / PPM);

        Body body = world.createBody(bodyDef);

        /*
         * Om den är ungefär cirkulär använder
         * vi riktig CircleShape.
         */
        if (Math.abs(
                ellipse.width
                        - ellipse.height) < 0.01f) {

            CircleShape shape = new CircleShape();

            shape.setRadius(
                    ellipse.width
                            / 2f
                            / PPM);

            Fixture fixture = body.createFixture(shape, 0f);

            fixture.setUserData("ground");

            shape.dispose();

            return;
        }

        /*
         * Box2D har ingen EllipseShape.
         * Approximerar därför ovalen med
         * en 8-hörnig polygon.
         */
        int points = 8;

        float[] vertices = new float[points * 2];

        float radiusX = ellipse.width
                / 2f
                / PPM;

        float radiusY = ellipse.height
                / 2f
                / PPM;

        for (int i = 0; i < points; i++) {

            double angle = Math.PI * 2
                    * i / points;

            vertices[i * 2] = (float) Math.cos(angle)
                    * radiusX;

            vertices[i * 2 + 1] = (float) Math.sin(angle)
                    * radiusY;
        }

        PolygonShape shape = new PolygonShape();

        shape.set(vertices);

        Fixture fixture = body.createFixture(shape, 0f);

        fixture.setUserData("ground");

        shape.dispose();
    }

    public void render(
            OrthographicCamera camera) {

        mapRenderer.setView(camera);
        mapRenderer.render();
    }

    public void dispose() {

        map.dispose();
        mapRenderer.dispose();
    }
}
