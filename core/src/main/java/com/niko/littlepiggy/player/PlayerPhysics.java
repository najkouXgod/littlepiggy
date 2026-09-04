package com.niko.littlepiggy.player;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.*;

public class PlayerPhysics {

    /*
     * Kroppen byggs som en kapsel:
     *
     * _______
     * ( )
     * -------
     *
     * En box i mitten + en cirkel på varje sida.
     *
     * Justera dessa senare efter hur den nya grisen ser ut.
     */
    private static final float BODY_RADIUS = 0.20f;
    private static final float BODY_HALF_LENGTH = 0.22f;
    private static final float BODY_OFFSET_Y = 0f;

    /*
     * Två små foot sensors.
     *
     * gris
     * (=======)
     * • •
     */
    private static final float FOOT_X = 0.28f;
    private static final float FOOT_Y = -0.3f;
    private static final float FOOT_RADIUS = 0.055f;

    private final Body body;

    /*
     * Två foot sensors kan samtidigt röra flera fixtures,
     * därför använder vi en counter istället för boolean.
     */
    private int groundContacts;

    public PlayerPhysics(World world, float x, float y) {
        body = createBody(world, x, y);
    }

    private Body createBody(World world, float x, float y) {

        BodyDef bodyDef = new BodyDef();
        bodyDef.type = BodyDef.BodyType.DynamicBody;
        bodyDef.position.set(x, y);
        bodyDef.fixedRotation = true;

        Body body = world.createBody(bodyDef);

        createMainCollider(body);
        createFootSensors(body);

        return body;
    }

    private void createMainCollider(Body body) {

        /*
         * Mitten av kapseln.
         */
        PolygonShape centerShape = new PolygonShape();

        centerShape.setAsBox(
                BODY_HALF_LENGTH,
                BODY_RADIUS,
                new Vector2(0, BODY_OFFSET_Y),
                0);

        FixtureDef centerFixtureDef = new FixtureDef();
        centerFixtureDef.shape = centerShape;
        centerFixtureDef.density = 5f;
        centerFixtureDef.friction = 0f;

        body.createFixture(centerFixtureDef);

        centerShape.dispose();

        /*
         * Vänster rundning.
         */
        createBodyCircle(
                body,
                -BODY_HALF_LENGTH,
                BODY_OFFSET_Y);

        /*
         * Höger rundning.
         */
        createBodyCircle(
                body,
                BODY_HALF_LENGTH,
                BODY_OFFSET_Y);
    }

    private void createBodyCircle(
            Body body,
            float offsetX,
            float offsetY) {

        CircleShape shape = new CircleShape();

        shape.setRadius(BODY_RADIUS);

        shape.setPosition(
                new Vector2(offsetX, offsetY));

        FixtureDef fixtureDef = new FixtureDef();
        fixtureDef.shape = shape;

        /*
         * Mitten-fixturen står för massan.
         * Cirklarna används främst för collisionformen.
         */
        fixtureDef.density = 0f;
        fixtureDef.friction = 0f;

        body.createFixture(fixtureDef);

        shape.dispose();
    }

    private void createFootSensors(Body body) {

        createFootSensor(
                body,
                -FOOT_X,
                FOOT_Y);

        createFootSensor(
                body,
                FOOT_X,
                FOOT_Y);
    }

    private void createFootSensor(
            Body body,
            float offsetX,
            float offsetY) {

        CircleShape shape = new CircleShape();

        shape.setRadius(FOOT_RADIUS);

        shape.setPosition(
                new Vector2(offsetX, offsetY));

        FixtureDef fixtureDef = new FixtureDef();
        fixtureDef.shape = shape;
        fixtureDef.isSensor = true;

        /*
         * Vi behåller "foot" just nu så din nuvarande
         * GameContactListener fortsätter fungera.
         */
        body.createFixture(fixtureDef)
                .setUserData("foot");

        shape.dispose();
    }

    public void setOwner(Object owner) {
        body.setUserData(owner);
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

    public Vector2 getVelocity() {
        return body.getLinearVelocity().cpy();
    }

    public void setHorizontalVelocity(float velocity) {
        body.setLinearVelocity(
                velocity,
                body.getLinearVelocity().y);
    }

    public void jump(float xImpulse, float yImpulse) {

        body.applyLinearImpulse(
                new Vector2(xImpulse, yImpulse),
                body.getWorldCenter(),
                true);
    }

    public void applyImpulse(float x, float y) {

        body.applyLinearImpulse(
                new Vector2(x, y),
                body.getWorldCenter(),
                true);
    }

    public boolean isGrounded() {
        return groundContacts > 0;
    }

    public void beginGroundContact() {
        groundContacts++;
    }

    public void endGroundContact() {
        groundContacts = Math.max(0, groundContacts - 1);
    }
}
