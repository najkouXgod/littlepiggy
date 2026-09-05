package com.niko.littlepiggy.world;

import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.physics.box2d.*;

public class Goal {

    private final Sprite sprite;
    private final Body body;

    private boolean reached;

    public Goal(
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

        PolygonShape shape = new PolygonShape();

        shape.setAsBox(
                0.4f,
                0.5f);

        FixtureDef fixtureDef = new FixtureDef();
        fixtureDef.shape = shape;
        fixtureDef.isSensor = true;

        Fixture fixture = body.createFixture(fixtureDef);
        fixture.setUserData(this);

        shape.dispose();
    }

    public void reach() {
        reached = true;
    }

    public boolean isReached() {
        return reached;
    }

    public void render(SpriteBatch batch) {
        sprite.draw(batch);
    }
}
