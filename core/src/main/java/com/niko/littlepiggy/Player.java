package com.niko.littlepiggy;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.*;

public class Player {

    private enum AnimationState {
        IDLE, RUNNING, CHARGING
    }

    private AnimationState currentState = AnimationState.IDLE;

    // JUMP
    private static final float JUMP_MAXPOWER = 4f;
    private static final float CHARGE_TIME = 5f;
    private static final float CHARGE_FRICTION = 1.8f;
    private static final float TAP_THRESHOLD = 0.15f;

    private final Animation<TextureRegion> idleAnimation;
    private final Animation<TextureRegion> runningAnimation;
    private final Animation<TextureRegion> chargeAnimation;
    private TextureRegion currentFrame;
    private final Sprite sprite;

    private final Body body;

    private float stateTime;
    private boolean spaceWasPressed = false;
    private boolean isMoving;
    private boolean facingLeft;
    private int groundContacts;
    private float jumpCharge;
    private boolean isCharging;
    private float spacePressedTime = 0f;

    public Player(World world, float startX, float startY, GameAssets assets) {

        TextureRegion[] idleFrames = assets.getFrames(GameAssets.PIG_IDLE, 2, 1);
        idleAnimation = new Animation<>(0.5f, idleFrames);
        idleAnimation.setPlayMode(Animation.PlayMode.LOOP);

        TextureRegion[] runFrames = assets.getFrames(GameAssets.PIG_RUNNING, 5, 1);
        runningAnimation = new Animation<>(0.06f, runFrames);
        runningAnimation.setPlayMode(Animation.PlayMode.LOOP);

        TextureRegion[] chargeFrames = assets.getFrames(GameAssets.PIG_CHARGING, 3, 1);
        chargeAnimation = new Animation<>(0.06f, chargeFrames);
        chargeAnimation.setPlayMode(Animation.PlayMode.NORMAL);

        sprite = new Sprite(idleFrames[0]);
        sprite.setSize(1f, 1f);

        // Player-BODY
        BodyDef bodyDef = new BodyDef();
        bodyDef.type = BodyDef.BodyType.DynamicBody;
        bodyDef.position.set(startX, startY);
        bodyDef.fixedRotation = true;
        body = world.createBody(bodyDef);
        // Player-COLLIDER
        PolygonShape bodyShape = new PolygonShape();
        bodyShape.setAsBox(sprite.getWidth() / 3f, sprite.getHeight() / 4, new Vector2(0, -0.2f), 0);

        FixtureDef fixtureDef = new FixtureDef();
        fixtureDef.shape = bodyShape;
        fixtureDef.density = 1f;
        fixtureDef.friction = 0f;
        body.createFixture(fixtureDef);
        bodyShape.dispose();

        // Fot-SENSOR
        PolygonShape footShape = new PolygonShape();
        footShape.setAsBox(
                sprite.getWidth() * 0.2f,
                0.05f,
                new Vector2(0, -sprite.getHeight() / 2f),
                0);
        FixtureDef footFixtureDef = new FixtureDef();
        footFixtureDef.shape = footShape;
        footFixtureDef.isSensor = true;
        body.createFixture(footFixtureDef).setUserData("foot");
        footShape.dispose();
    }

    public void update() {
        stateTime += Gdx.graphics.getDeltaTime();
        isMoving = false;

        handleJump();
        handleInput();
        updateAnimation();

        Vector2 pos = body.getPosition();
        sprite.setPosition(pos.x - sprite.getWidth() / 2f, pos.y - sprite.getHeight() / 2f);
    }

    private void updateAnimation() {
        AnimationState newState = isCharging ? AnimationState.CHARGING
                : isMoving ? AnimationState.RUNNING
                        : AnimationState.IDLE;
        if (newState != currentState) {
            stateTime = 0f;
            currentState = newState;
        }
        switch (currentState) {
            case RUNNING:
                currentFrame = runningAnimation.getKeyFrame(stateTime);
                break;
            case CHARGING:
                currentFrame = chargeAnimation.getKeyFrame(stateTime);
                break;
            default:
                currentFrame = idleAnimation.getKeyFrame(stateTime);
                break;
        }
        sprite.setRegion(currentFrame);
        sprite.setFlip(facingLeft, false);
    }

    private void handleInput() {
        if (isCharging)
            return;
        float vx = 0;
        if (Gdx.input.isKeyPressed(Input.Keys.RIGHT)) {
            vx = DebugConfig.SPEED;
            facingLeft = false;
            isMoving = true;
        }
        if (Gdx.input.isKeyPressed(Input.Keys.LEFT)) {
            vx = -DebugConfig.SPEED;
            facingLeft = true;
            isMoving = true;
        }
        body.setLinearVelocity(vx, body.getLinearVelocity().y);
    }

    private void handleJump() {
        boolean spacePressed = Gdx.input.isKeyPressed(Input.Keys.UP);
        float dt = Gdx.graphics.getDeltaTime();

        if (!isGrounded()) {
            if (!spacePressed) {
                isCharging = false;
                jumpCharge = 0f;
                spacePressedTime = 0f;
            }
            spaceWasPressed = spacePressed;
            return;
        }

        if (spacePressed) {
            spacePressedTime += dt;
            if (spacePressedTime >= DebugConfig.TAP_THRESHOLD) {
                Vector2 vel = body.getLinearVelocity();
                float newVx = vel.x * (1f - dt * DebugConfig.CHARGE_FRICTION);
                if (Math.abs(newVx) < 0.1f)
                    newVx = 0f;
                body.setLinearVelocity(newVx, vel.y);
                jumpCharge = Math.min(jumpCharge + dt / DebugConfig.CHARGE_TIME, 1f);
                isCharging = true;
            }
        }

        if (!spacePressed && spaceWasPressed) {
            if (spacePressedTime < DebugConfig.TAP_THRESHOLD) {
                body.applyLinearImpulse(new Vector2(0, DebugConfig.JUMP_MINPOWER), body.getWorldCenter(), true);
            } else if (isCharging) {
                float curvedCharge = (float) Math.pow(jumpCharge, 1f / DebugConfig.CHARGE_CURVE);
                float jumpStrength = DebugConfig.JUMP_MINPOWER
                        + (DebugConfig.JUMP_MAXPOWER - DebugConfig.JUMP_MINPOWER) * curvedCharge;
                float horizontalBoost = (facingLeft ? -1f : 1f) * 2.6f * curvedCharge;
                body.applyLinearImpulse(new Vector2(horizontalBoost, jumpStrength), body.getWorldCenter(), true);
            }
            spacePressedTime = 0f;
            jumpCharge = 0f;
            isCharging = false;
        }
        spaceWasPressed = spacePressed;
    }

    public void render(SpriteBatch batch) {
        sprite.draw(batch);
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

    public boolean isCharging() {
        return isCharging;
    }

    public float getJumpCharge() {
        return jumpCharge;
    }

    public float getX() {
        return body.getPosition().x;
    }

    public float getY() {
        return body.getPosition().y;
    }

    public Vector2 getVelocity() {
        return body.getLinearVelocity().cpy();
    }

    public void dispose() {
    }
}
