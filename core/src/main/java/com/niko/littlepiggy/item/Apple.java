package com.niko.littlepiggy.item;

import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.*;

public class Apple {

    private final float BODY_RADIUS = 0.15f;
    private final Sprite sprite;
    private Body body;
    private boolean collected;

    public Apple(
            World world,
            TextureRegion texture,
            float x,
            float y) {

        sprite = new Sprite(texture);

        sprite.setSize(1f, 1f);

        sprite.setPosition(
                x - sprite.getWidth() / 2f,
                y - sprite.getHeight() / 2f);

        BodyDef bodyDef = new BodyDef();
        bodyDef.type = BodyDef.BodyType.StaticBody;
        bodyDef.position.set(x, y);

        body = world.createBody(bodyDef);

        CircleShape shape = new CircleShape();
        shape.setRadius(BODY_RADIUS);
        shape.setPosition(new Vector2(0, 0.20f));

        FixtureDef fixtureDef = new FixtureDef();
        fixtureDef.shape = shape;
        fixtureDef.isSensor = true;

        Fixture fixture = body.createFixture(fixtureDef);
        fixture.setUserData(this);

        shape.dispose();
    }

    public void collect() {
        collected = true;
    }

    public boolean isCollected() {
        return collected;
    }

    public void removeBody() {
        if (body != null) {
            body.getWorld().destroyBody(body);
            body = null;
        }
    }

    public void render(SpriteBatch batch) {
        if (!collected) {
            sprite.draw(batch);
        }
    }
}
