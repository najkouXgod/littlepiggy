package com.niko.littlepiggy.world;

import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.maps.MapLayer;
import com.badlogic.gdx.maps.MapObject;
import com.badlogic.gdx.maps.objects.RectangleMapObject;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TmxMapLoader;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.physics.box2d.*;

public class MapManager {
    private final TiledMap map;
    private final OrthogonalTiledMapRenderer mapRenderer;

    public MapManager( String mapName ) {
        map = new TmxMapLoader().load("maps/" + mapName + ".tmx");
        mapRenderer = new OrthogonalTiledMapRenderer(map, 1/64f);
    }

    public void createCollisions( World world ) {
        MapLayer collisionLayer = map.getLayers().get("collisions");
        if ( collisionLayer == null ) return;

        for (MapObject obj : collisionLayer.getObjects()) {
            if ( obj instanceof RectangleMapObject) {
                RectangleMapObject rectangleObject = (RectangleMapObject) obj;
                Rectangle rect = rectangleObject.getRectangle();

                BodyDef bodyDef = new BodyDef();
                bodyDef.type = BodyDef.BodyType.StaticBody;
                bodyDef.position.set( ( rect.x + rect.width / 2 ) / 64f,
                    ( rect.y + rect.height / 2 ) / 64f );

                Body body = world.createBody(bodyDef);

                PolygonShape shape = new PolygonShape();
                shape.setAsBox(rect.width/2/64f,
                               rect.height/2/64f);

                body.setUserData(this);
                Fixture fixture = body.createFixture(shape, 0.0f);
                fixture.setUserData("ground");
                shape.dispose();
            }
        }
    }

    public void render( OrthographicCamera camera ) {
        mapRenderer.setView(camera);
        mapRenderer.render();
    }
    public void dispose() {
        map.dispose();
        mapRenderer.dispose();
    }
}
