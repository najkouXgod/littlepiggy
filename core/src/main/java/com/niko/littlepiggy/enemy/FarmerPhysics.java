package com.niko.littlepiggy.enemy;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.*;

public class FarmerPhysics {

    private static final float BODY_HALF_WIDTH = 0.45f;
    private static final float BODY_HALF_HEIGHT = 0.75f;

    private static final float SHOOT_RANGE = 3.5f;

    private final Body body;
    private final Fixture bodyFixture;
    private final Fixture rangeFixture;

    public FarmerPhysics(
            World world,
            float x,
            float y) {

        BodyDef bodyDef = new BodyDef();
        bodyDef.type = BodyDef.BodyType.DynamicBody;
        bodyDef.position.set(x, y);
        bodyDef.fixedRotation = true;

        body = world.createBody(bodyDef);

        // Vanlig collider
        PolygonShape bodyShape = new PolygonShape();

        bodyShape.setAsBox(
                BODY_HALF_WIDTH,
                BODY_HALF_HEIGHT);

        FixtureDef bodyFixtureDef = new FixtureDef();
        bodyFixtureDef.shape = bodyShape;
        bodyFixtureDef.friction = 0.5f;

        bodyFixture = body.createFixture(bodyFixtureDef);

        bodyShape.dispose();

        // Sensor för att upptäcka Player
        CircleShape rangeShape = new CircleShape();
        rangeShape.setRadius(SHOOT_RANGE);

        FixtureDef rangeFixtureDef = new FixtureDef();
        rangeFixtureDef.shape = rangeShape;
        rangeFixtureDef.isSensor = true;

        rangeFixture = body.createFixture(rangeFixtureDef);

        rangeShape.dispose();
    }

    public void setOwner(Farmer farmer) {

        body.setUserData(farmer);

        bodyFixture.setUserData(farmer);

        /*
         * Viktigt eftersom GameContactListener
         * identifierar Farmer range-sensorn
         * via fixture.getUserData().
         */
        rangeFixture.setUserData(farmer);
    }

    public void applyImpulse(
            float x,
            float y) {

        body.applyLinearImpulse(
                new Vector2(x, y),
                body.getWorldCenter(),
                true);
    }

    public boolean hasLineOfSight(
            Vector2 targetPosition) {

        final boolean[] blocked = { false };

        body.getWorld().rayCast(
                (fixture, point, normal, fraction) -> {

                    // Ignorera Farmers egna fixtures.
                    if (fixture.getBody() == body) {
                        return 1f;
                    }

                    if ("ground".equals(
                            fixture.getUserData())) {

                        blocked[0] = true;

                        return 0f;
                    }

                    return 1f;
                },
                body.getPosition(),
                targetPosition);

        return !blocked[0];
    }

    public float getX() {
        return body.getPosition().x;
    }

    public float getY() {
        return body.getPosition().y;
    }

    public Vector2 getPosition() {
        return body.getPosition().cpy();
    }

    public World getWorld() {
        return body.getWorld();
    }

    public void destroy() {
        body.getWorld().destroyBody(body);
    }
}
