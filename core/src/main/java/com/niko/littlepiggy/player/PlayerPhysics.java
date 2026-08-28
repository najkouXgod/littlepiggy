package com.niko.littlepiggy.player;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.*;

public class PlayerPhysics {

    private static final float BODY_HALF_WIDTH = 0.30f;
    private static final float BODY_HALF_HEIGHT = 0.45f;
    private static final float BODY_OFFSET_Y = -0.05f;

    private static final float FOOT_HALF_WIDTH = 0.20f;
    private static final float FOOT_HALF_HEIGHT = 0.05f;
    private static final float FOOT_OFFSET_Y = -0.52f;

    private final Body body;

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

        // Main collider
        PolygonShape bodyShape = new PolygonShape();
        bodyShape.setAsBox(
                BODY_HALF_WIDTH,
                BODY_HALF_HEIGHT,
                new Vector2(0, BODY_OFFSET_Y),
                0);

        FixtureDef bodyFixtureDef = new FixtureDef();
        bodyFixtureDef.shape = bodyShape;
        bodyFixtureDef.density = 1f;
        bodyFixtureDef.friction = 0f;

        body.createFixture(bodyFixtureDef);

        bodyShape.dispose();

        // Foot sensor
        PolygonShape footShape = new PolygonShape();
        footShape.setAsBox(
                FOOT_HALF_WIDTH,
                FOOT_HALF_HEIGHT,
                new Vector2(0, FOOT_OFFSET_Y),
                0);

        FixtureDef footFixtureDef = new FixtureDef();
        footFixtureDef.shape = footShape;
        footFixtureDef.isSensor = true;

        body.createFixture(footFixtureDef)
                .setUserData("foot");

        footShape.dispose();

        return body;
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
