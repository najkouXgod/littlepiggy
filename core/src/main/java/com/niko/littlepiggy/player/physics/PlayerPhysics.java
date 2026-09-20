package com.niko.littlepiggy.player.physics;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.*;

/**
 * Spelarens Box2D-body: collider, foot sensors, velocity, gravity och
 * ground contacts.
 *
 * Klassen är helt generisk. Den vet inte vad Dash, Backflip eller
 * Ground Slam är - abilities och movement använder bara de generella
 * operationerna här (setVelocity, setGravityScale, applyImpulse ...).
 *
 * Body:n ägs bara här. Andra klasser får skapa/ta bort sensor-fixtures
 * via createSensor/destroyFixture men når aldrig Body direkt.
 */
public class PlayerPhysics {

    private static final float BODY_RADIUS = 0.20f;
    private static final float BODY_HALF_LENGTH = 0.22f;
    private static final float BODY_OFFSET_Y = 0f;

    private static final float FOOT_X = 0.28f;
    private static final float FOOT_Y = -0.3f;
    private static final float FOOT_RADIUS = 0.055f;

    private final Body body;

    private int groundContacts;

    /*
     * Gravity som gäller "normalt". Abilities som tillfälligt vill ändra
     * gravity använder setGravityScale() och återställer med
     * resetGravityScale(). PlayerMovement kan senare ändra basvärdet
     * (t.ex. lägre gravity i vatten) utan att abilities behöver veta.
     */
    private float baseGravityScale = 1f;

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

        createBodyCircle(
                body,
                -BODY_HALF_LENGTH,
                BODY_OFFSET_Y);

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

        body.createFixture(fixtureDef)
                .setUserData("foot");

        shape.dispose();
    }

    /* ------------------------------------------------------------
     * Generiska sensorer (används av AttackHitboxManager)
     * ------------------------------------------------------------ */

    /**
     * Skapar en rektangulär sensor på spelarens body.
     * Får inte anropas medan world.step() pågår.
     */
    public Fixture createSensor(
            float width,
            float height,
            float offsetX,
            float offsetY,
            Object userData) {

        PolygonShape shape = new PolygonShape();

        shape.setAsBox(
                width / 2f,
                height / 2f,
                new Vector2(offsetX, offsetY),
                0f);

        FixtureDef fixtureDef = new FixtureDef();
        fixtureDef.shape = shape;
        fixtureDef.isSensor = true;

        Fixture fixture = body.createFixture(fixtureDef);

        fixture.setUserData(userData);

        shape.dispose();

        return fixture;
    }

    public void destroyFixture(Fixture fixture) {

        if (fixture != null) {
            body.destroyFixture(fixture);
        }
    }

    public void setOwner(Object owner) {
        body.setUserData(owner);
    }

    /* ------------------------------------------------------------
     * Position / velocity
     * ------------------------------------------------------------ */

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

    public void setVelocity(float x, float y) {
        body.setLinearVelocity(x, y);
    }

    public void setHorizontalVelocity(float velocity) {

        body.setLinearVelocity(
                velocity,
                body.getLinearVelocity().y);
    }

    public void setVerticalVelocity(float velocity) {

        body.setLinearVelocity(
                body.getLinearVelocity().x,
                velocity);
    }

    public void applyImpulse(float x, float y) {

        body.applyLinearImpulse(
                new Vector2(x, y),
                body.getWorldCenter(),
                true);
    }

    /* ------------------------------------------------------------
     * Gravity
     * ------------------------------------------------------------ */

    /** Tillfällig override, t.ex. 0 medan en ability hänger i luften. */
    public void setGravityScale(float scale) {
        body.setGravityScale(scale);
    }

    /** Går tillbaka till gravity som movement-läget bestämt. */
    public void resetGravityScale() {
        body.setGravityScale(baseGravityScale);
    }

    /** Ändrar "normal" gravity (för framtida movement modes). */
    public void setBaseGravityScale(float scale) {
        baseGravityScale = scale;
        body.setGravityScale(scale);
    }

    /* ------------------------------------------------------------
     * Ground contacts (foot sensors)
     * ------------------------------------------------------------ */

    public boolean isGrounded() {
        return groundContacts > 0;
    }

    public void beginGroundContact() {
        groundContacts++;
    }

    public void endGroundContact() {
        groundContacts = Math.max(
                0,
                groundContacts - 1);
    }
}
