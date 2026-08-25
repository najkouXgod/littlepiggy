package com.niko.littlepiggy.enemy;

import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.physics.box2d.*;

public class Farmer {

    private static final float SHOOT_RANGE = 4f;
    private static final float SHOOT_INTERVAL = 2f;

    private boolean playerInRange;
    private float shootCooldown;

    private final Sprite sprite;
    private final Body body;

    public Farmer(World world, GameAssets assets, float x, float y) {

        sprite = new Sprite(
                assets.getTexture(GameAssets.FARMER_IDLE));

        sprite.setSize(1.5f, 1.5f);

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

        CircleShape rangeShape = new CircleShape();
        rangeShape.setRadius(SHOOT_RANGE);

        FixtureDef rangeFixtureDef = new FixtureDef();
        rangeFixtureDef.shape = rangeShape;
        rangeFixtureDef.isSensor = true;

        Fixture rangeFixture = body.createFixture(rangeFixtureDef);
        rangeFixture.setUserData("farmerRange");

        rangeShape.dispose();

        shape.dispose();
    }

    public void render(SpriteBatch batch) {
        sprite.draw(batch);
    }

    public void update(float delta) {

        sprite.setPosition(
                body.getPosition().x - sprite.getWidth() / 2f,
                body.getPosition().y - sprite.getHeight() / 2f);

        if (shootCooldown > 0) {
            shootCooldown -= delta;
        }

        if (playerInRange && shootCooldown <= 0) {
            shoot();
            shootCooldown = SHOOT_INTERVAL;
        }
    }

    public void playerEnteredRange() {
        playerInRange = true;
    }

    public void playerExitedRange() {
        playerInRange = false;
    }

    private void shoot() {
        System.out.println("PANG!");
    }
}
