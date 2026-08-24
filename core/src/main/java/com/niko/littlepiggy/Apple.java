package com.niko.littlepiggy;

import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.physics.box2d.*;

public class Apple {

    private final Sprite sprite;
    private Body body;
    private boolean collected = false;

    public Apple(World world, GameAssets assets, float x, float y) {

        sprite = new Sprite(assets.getTexture(GameAssets.APPLE));
        sprite.setSize(0.25f, 0.25f);
        sprite.setPosition(
                x - sprite.getWidth() / 2f,
                y - sprite.getHeight() / 2f);

        BodyDef bodyDef = new BodyDef();
        bodyDef.type = BodyDef.BodyType.StaticBody;
        bodyDef.position.set(x, y);

        body = world.createBody(bodyDef);

        CircleShape shape = new CircleShape();
        shape.setRadius(0.125f);

        FixtureDef fixtureDef = new FixtureDef();
        fixtureDef.shape = shape;
        fixtureDef.isSensor = true;

        Fixture fixture = body.createFixture(fixtureDef);
        fixture.setUserData(this);

        shape.dispose();
    }

    public void render(SpriteBatch batch) {
        if (!collected) {
            sprite.draw(batch);
        }
    }

    public void collect() {
        collected = true;
    }

    public void removeBody() {
        if (body != null) {
            body.getWorld().destroyBody(body);
            body = null;
        }
    }

    public boolean isCollected() {
        return collected;
    }

    public void dispose() {
        body.getWorld().destroyBody(body);
    }
}
