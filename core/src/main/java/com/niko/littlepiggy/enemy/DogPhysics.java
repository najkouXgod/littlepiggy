package com.niko.littlepiggy.enemy;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.*;

public class DogPhysics {

    private static final float BODY_HALF_WIDTH = 0.4f;
    private static final float BODY_HALF_HEIGHT = 0.35f;

    private static final float AGGRO_RANGE = 5f;

    /*
     * Till skillnad från Farmer (som ska stå still och kännas "fast")
     * behöver Dog kunna röra sig fritt, så vanlig densitet räcker.
     * Ingen speciell knockback-broms behövs heller av samma anledning.
     */
    private static final float BODY_DENSITY = 1f;

    private final Body body;
    private final Fixture bodyFixture;
    private final Fixture rangeFixture;

    public DogPhysics(
            World world,
            float x,
            float y) {

        BodyDef bodyDef = new BodyDef();
        bodyDef.type = BodyDef.BodyType.DynamicBody;
        bodyDef.position.set(x, y);
        bodyDef.fixedRotation = true;

        body = world.createBody(bodyDef);

        PolygonShape bodyShape = new PolygonShape();

        bodyShape.setAsBox(
                BODY_HALF_WIDTH,
                BODY_HALF_HEIGHT);

        FixtureDef bodyFixtureDef = new FixtureDef();
        bodyFixtureDef.shape = bodyShape;
        bodyFixtureDef.density = BODY_DENSITY;
        bodyFixtureDef.friction = 0.3f;

        bodyFixture = body.createFixture(bodyFixtureDef);

        bodyShape.dispose();

        CircleShape rangeShape = new CircleShape();
        rangeShape.setRadius(AGGRO_RANGE);

        FixtureDef rangeFixtureDef = new FixtureDef();
        rangeFixtureDef.shape = rangeShape;
        rangeFixtureDef.isSensor = true;

        rangeFixture = body.createFixture(rangeFixtureDef);

        rangeShape.dispose();
    }

    public void setOwner(Dog dog) {

        body.setUserData(dog);
        bodyFixture.setUserData(dog);

        /*
         * Viktigt: GameContactListener identifierar
         * Dogs range-sensor via fixture.getUserData().
         */
        rangeFixture.setUserData(dog);
    }

    /** Vanlig gångfart mot ett mål, styr bara X. */
    public void moveTowards(float directionX, float speed) {

        Vector2 velocity = body.getLinearVelocity();

        body.setLinearVelocity(
                Math.signum(directionX) * speed,
                velocity.y);
    }

    public void stopHorizontal() {

        Vector2 velocity = body.getLinearVelocity();

        body.setLinearVelocity(0f, velocity.y);
    }

    /** Kraftig, momentan rörelse i en låst riktning. */
    public void lunge(float directionX, float speed) {

        Vector2 velocity = body.getLinearVelocity();

        body.setLinearVelocity(
                Math.signum(directionX) * speed,
                velocity.y);
    }

    public void applyImpulse(float x, float y) {

        body.applyLinearImpulse(
                new Vector2(
                        x * body.getMass(),
                        y * body.getMass()),
                body.getWorldCenter(),
                true);
    }

    public boolean hasLineOfSight(Vector2 targetPosition) {

        final boolean[] blocked = { false };

        body.getWorld().rayCast(
                (fixture, point, normal, fraction) -> {

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
