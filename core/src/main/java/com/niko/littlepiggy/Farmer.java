package com.niko.littlepiggy;

import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.physics.box2d.*;

public class Farmer {

    private final Sprite sprite;
    private final Body body;

    public Farmer(World world, GameAssets assets, float x, float y) {

        sprite = new Sprite(
                assets.getTexture(GameAssets.FARMER_IDLE));

        sprite.setSize(1f, 1f);

        sprite.setPosition(
                x - sprite.getWidth() / 2f,
                y - sprite.getHeight() / 2f);

        // Farmer body
        BodyDef bodyDef = new BodyDef();
        bodyDef.type = BodyDef.BodyType.DynamicBody;
        bodyDef.position.set(x, y);
        bodyDef.fixedRotation = true;

        body = world.createBody(bodyDef);

        // Farmer collider
        PolygonShape shape = new PolygonShape();
        shape.setAsBox(
                sprite.getWidth() * 0.3f,
                sprite.getHeight() * 0.5f);

        FixtureDef fixtureDef = new FixtureDef();
        fixtureDef.shape = shape;
        fixtureDef.friction = 0.5f;

        Fixture fixture = body.createFixture(fixtureDef);
        fixture.setUserData(this);

        shape.dispose();
    }

    public void render(SpriteBatch batch) {
        sprite.draw(batch);
    }

    public void update() {
        sprite.setPosition(
                body.getPosition().x - sprite.getWidth() / 2f,
                body.getPosition().y - sprite.getHeight() / 2f);
    }
}
