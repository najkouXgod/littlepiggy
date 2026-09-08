package com.niko.littlepiggy.enemy.farmer;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.*;

public class FarmerPhysics {

    private static final float BODY_HALF_WIDTH = 0.45f;
    private static final float BODY_HALF_HEIGHT = 0.75f;

    private static final float SHOOT_RANGE = 3.5f;

    /*
     * Farmer ska kännas nästan "fast" när Player bara springer in i den.
     * En betydligt högre density gör att vanliga kroppskollisioner bara
     * flyttar Farmer lite och långsamt. Attack-knockback kompenseras
     * separat i applyImpulse() så dash/backflip behåller sin gamla fart.
     */
    private static final float BODY_DENSITY = 7f;

    /*
     * Dashen ger fortfarande exakt samma initiala impulse som tidigare,
     * men efter träffen bromsas bara X-led mycket snabbare.
     */
    private static final float DASH_KNOCKBACK_BRAKE = 10f;
    private static final float DASH_BRAKE_STOP_SPEED = 0.08f;

    private final Body body;
    private final Fixture bodyFixture;
    private final Fixture rangeFixture;

    private boolean dashKnockbackBrakeActive;

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
        bodyFixtureDef.density = BODY_DENSITY;
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

    public void update(float delta) {

        if (!dashKnockbackBrakeActive) {
            return;
        }

        Vector2 velocity = body.getLinearVelocity();

        /*
         * Bromsa endast horisontellt. Y-hastigheten lämnas orörd så
         * backflip/andra vertikala krafter fortfarande känns naturliga.
         */
        float brakeFactor = Math.max(
                0f,
                1f - DASH_KNOCKBACK_BRAKE * delta);

        float newVelocityX = velocity.x * brakeFactor;

        if (Math.abs(newVelocityX) <= DASH_BRAKE_STOP_SPEED) {
            newVelocityX = 0f;
            dashKnockbackBrakeActive = false;
        }

        body.setLinearVelocity(
                newVelocityX,
                velocity.y);
    }

    public void startDashKnockbackBrake() {
        dashKnockbackBrakeActive = true;
    }

    public void applyImpulse(
            float x,
            float y) {

        /*
         * x/y har hittills i praktiken motsvarat Farmerns hastighetsändring
         * eftersom kroppen hade ungefär massan 1. När vi gör kroppen tyngre
         * för att Player inte ska kunna putta runt den, skalar vi avsiktlig
         * attack-knockback med massan. Resultatet blir samma initiala fart
         * från dash/backflip som före BODY_DENSITY-ändringen.
         */
        body.applyLinearImpulse(
                new Vector2(
                        x * body.getMass(),
                        y * body.getMass()),
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
