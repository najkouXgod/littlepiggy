package com.niko.littlepiggy.enemy.farmer;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.*;

public class FarmerPhysics {

    private static final float BODY_HALF_WIDTH = 0.45f;
    private static final float BODY_HALF_HEIGHT = 0.75f;

    private static final float FOOT_HALF_WIDTH = 0.34f;
    private static final float FOOT_HALF_HEIGHT = 0.06f;
    private static final float FOOT_OFFSET_Y = -BODY_HALF_HEIGHT - 0.02f;

    private static final float SHOOT_RANGE = 3.5f;

    private static final float BODY_DENSITY = 7f;

    private static final float DASH_KNOCKBACK_BRAKE = 10f;
    private static final float DASH_BRAKE_STOP_SPEED = 0.08f;

    private final Body body;

    private final Fixture bodyFixture;
    private final Fixture footFixture;
    private final Fixture rangeFixture;

    private boolean dashKnockbackBrakeActive;

    /*
     * Ground/fall tracking.
     */
    private int groundContacts;

    private boolean wasGrounded;
    private boolean landedThisFrame;

    /*
     * Högsta Y-positionen Farmer nådde under
     * nuvarande luftsekvens.
     */
    private float highestAirY;

    /*
     * Sparas när Farmer landar.
     */
    private float lastFallDistance;

    public FarmerPhysics(
            World world,
            float x,
            float y) {

        BodyDef bodyDef = new BodyDef();

        bodyDef.type = BodyDef.BodyType.DynamicBody;

        bodyDef.position.set(x, y);

        bodyDef.fixedRotation = true;

        body = world.createBody(bodyDef);

        /*
         * Vanlig collider.
         */
        PolygonShape bodyShape = new PolygonShape();

        bodyShape.setAsBox(
                BODY_HALF_WIDTH,
                BODY_HALF_HEIGHT);

        FixtureDef bodyFixtureDef = new FixtureDef();

        bodyFixtureDef.shape = bodyShape;

        bodyFixtureDef.density = BODY_DENSITY;

        bodyFixtureDef.friction = 0.5f;

        bodyFixture = body.createFixture(
                bodyFixtureDef);

        bodyShape.dispose();

        /*
         * Foot sensor.
         *
         * Använd en egen tag så vi inte blandar ihop
         * Farmerns foot sensor med Playerns "foot".
         */
        PolygonShape footShape = new PolygonShape();

        footShape.setAsBox(
                FOOT_HALF_WIDTH,
                FOOT_HALF_HEIGHT,
                new Vector2(
                        0f,
                        FOOT_OFFSET_Y),
                0f);

        FixtureDef footFixtureDef = new FixtureDef();

        footFixtureDef.shape = footShape;

        footFixtureDef.isSensor = true;

        footFixture = body.createFixture(
                footFixtureDef);

        footFixture.setUserData(
                "farmerFoot");

        footShape.dispose();

        /*
         * Sensor för att upptäcka Player.
         */
        CircleShape rangeShape = new CircleShape();

        rangeShape.setRadius(
                SHOOT_RANGE);

        FixtureDef rangeFixtureDef = new FixtureDef();

        rangeFixtureDef.shape = rangeShape;

        rangeFixtureDef.isSensor = true;

        rangeFixture = body.createFixture(
                rangeFixtureDef);

        rangeShape.dispose();

        /*
         * Om Farmer spawnar i luften ska fallet
         * räknas från spawn-positionen.
         */
        highestAirY = y;
    }

    public void setOwner(Farmer farmer) {

        body.setUserData(farmer);

        bodyFixture.setUserData(farmer);

        /*
         * Range-sensorn använder Farmer som userData
         * eftersom GameContactListener redan identifierar
         * den på det sättet.
         */
        rangeFixture.setUserData(farmer);

        /*
         * footFixture behåller "farmerFoot".
         * Farmer hittar vi via fixture.getBody().getUserData().
         */
    }

    public void update(float delta) {

        updateFallTracking();

        updateDashKnockback(delta);
    }

    private void updateFallTracking() {

        landedThisFrame = false;

        boolean grounded = isGrounded();

        float currentY = body.getPosition().y;

        /*
         * Farmer är i luften.
         */
        if (!grounded) {

            /*
             * Precis lämnat marken.
             */
            if (wasGrounded) {
                highestAirY = currentY;
            }

            /*
             * Om Farmer knockas upp räknar vi fallet
             * från den högsta punkten.
             */
            highestAirY = Math.max(
                    highestAirY,
                    currentY);
        }

        /*
         * Farmer har precis landat.
         */
        else if (!wasGrounded) {

            lastFallDistance = Math.max(
                    0f,
                    highestAirY
                            - currentY);

            landedThisFrame = true;

            highestAirY = currentY;
        }

        /*
         * När Farmer står på mark håller vi
         * referenspunkten vid nuvarande höjd.
         */
        else {
            highestAirY = currentY;
        }

        wasGrounded = grounded;
    }

    private void updateDashKnockback(
            float delta) {

        if (!dashKnockbackBrakeActive) {
            return;
        }

        Vector2 velocity = body.getLinearVelocity();

        float brakeFactor = Math.max(
                0f,
                1f
                        - DASH_KNOCKBACK_BRAKE
                                * delta);

        float newVelocityX = velocity.x
                * brakeFactor;

        if (Math.abs(newVelocityX) <= DASH_BRAKE_STOP_SPEED) {

            newVelocityX = 0f;

            dashKnockbackBrakeActive = false;
        }

        body.setLinearVelocity(
                newVelocityX,
                velocity.y);
    }

    public void beginGroundContact() {
        groundContacts++;
    }

    public void endGroundContact() {

        groundContacts = Math.max(
                0,
                groundContacts - 1);
    }

    public boolean isGrounded() {
        return groundContacts > 0;
    }

    public boolean didJustLand() {
        return landedThisFrame;
    }

    public float getLastFallDistance() {
        return lastFallDistance;
    }

    public void startDashKnockbackBrake() {
        dashKnockbackBrakeActive = true;
    }

    public void applyImpulse(
            float x,
            float y) {

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
                (fixture,
                        point,
                        normal,
                        fraction) -> {

                    /*
                     * Ignorera Farmers egna fixtures.
                     */
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
