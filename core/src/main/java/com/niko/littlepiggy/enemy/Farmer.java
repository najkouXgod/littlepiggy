package com.niko.littlepiggy.enemy;

import com.niko.littlepiggy.assets.GameAssets;
import com.niko.littlepiggy.projectile.Pellet;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.*;

public class Farmer {

    private static final int PELLET_COUNT = 5;
    private static final float SPREAD_DEGREES = 8f;
    private static final float SHOOT_RANGE = 6f;
    private static final float SHOOT_INTERVAL = 2f;

    private boolean facingLeft;
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

    public Array<Pellet> update(float delta, Vector2 playerPosition) {

        facingLeft = playerPosition.x < body.getPosition().x;

        sprite.setFlip(!facingLeft, false);

        sprite.setPosition(
                body.getPosition().x - sprite.getWidth() / 2f,
                body.getPosition().y - sprite.getHeight() / 2f);

        if (shootCooldown > 0) {
            shootCooldown -= delta;
        }

        if (playerInRange && hasLineOfSight(playerPosition) && shootCooldown <= 0) {
            shootCooldown = SHOOT_INTERVAL;
            return createPellets(playerPosition);
        }

        return null;
    }

    public void playerEnteredRange() {
        playerInRange = true;
    }

    public void playerExitedRange() {
        playerInRange = false;
    }

    private boolean hasLineOfSight(Vector2 playerPosition) {

        final boolean[] blocked = { false };

        body.getWorld().rayCast(
                (fixture, point, normal, fraction) -> {

                    // Ignorera Farmers egna fixtures
                    if (fixture.getBody() == body) {
                        return 1f;
                    }

                    // Vägg/mark ligger mellan Farmer och Player
                    if ("ground".equals(fixture.getUserData())) {
                        blocked[0] = true;

                        // Vi behöver inte fortsätta raycasten
                        return 0f;
                    }

                    // Ignorera exempelvis sensors och annat
                    return 1f;
                },
                body.getPosition(),
                playerPosition);

        return !blocked[0];
    }

    private Array<Pellet> createPellets(Vector2 playerPosition) {

        Array<Pellet> pellets = new Array<>();

        Vector2 baseDirection = new Vector2(playerPosition)
                .sub(body.getPosition())
                .nor();

        for (int i = 0; i < PELLET_COUNT; i++) {

            float angle = (MathUtils.random(-SPREAD_DEGREES / 2f, SPREAD_DEGREES / 2f)
                    + MathUtils.random(-SPREAD_DEGREES / 2f, SPREAD_DEGREES / 2f))
                    / 2f;

            Vector2 direction = new Vector2(baseDirection).rotateDeg(angle);

            float speedMultiplier = MathUtils.random(0.8f, 1.2f);

            pellets.add(new Pellet(
                    body.getWorld(),
                    body.getPosition().x,
                    body.getPosition().y,
                    speedMultiplier,
                    direction));
        }

        return pellets;
    }
}
