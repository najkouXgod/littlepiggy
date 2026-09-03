package com.niko.littlepiggy.player;

import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

import com.niko.littlepiggy.assets.GameAssets;

public class PlayerAnimator {

    private enum AnimationState {
        IDLE,
        RUNNING,
        CHARGING
    }

    private final Animation<TextureRegion> idleAnimation;
    private final Animation<TextureRegion> runningAnimation;
    private final Animation<TextureRegion> chargeAnimation;

    private final Sprite sprite;

    private AnimationState currentState = AnimationState.IDLE;

    private float stateTime;

    public PlayerAnimator(GameAssets assets) {

        TextureRegion[] idleFrames = assets.getRowFrames(
                GameAssets.PIG_SHEET,
                0,
                3,
                64,
                64);

        idleAnimation = new Animation<>(0.35f, idleFrames);

        idleAnimation.setPlayMode(
                Animation.PlayMode.LOOP);

        TextureRegion[] runningFrames = assets.getRowFrames(
                GameAssets.PIG_SHEET,
                1,
                5,
                64,
                64);

        runningAnimation = new Animation<>(0.07f, runningFrames);

        runningAnimation.setPlayMode(
                Animation.PlayMode.LOOP);

        TextureRegion[] chargeFrames = assets.getRowFrames(
                GameAssets.PIG_SHEET,
                2,
                3,
                64,
                64);

        chargeAnimation = new Animation<>(0.08f, chargeFrames);

        chargeAnimation.setPlayMode(
                Animation.PlayMode.NORMAL);

        sprite = new Sprite(idleFrames[0]);

        sprite.setSize(1f, 1f);
    }

    public void update(
            float delta,
            float x,
            float y,
            boolean moving,
            boolean charging,
            boolean facingLeft) {

        AnimationState newState;

        if (charging) {
            newState = AnimationState.CHARGING;

        } else if (moving) {
            newState = AnimationState.RUNNING;

        } else {
            newState = AnimationState.IDLE;
        }

        if (newState != currentState) {
            currentState = newState;
            stateTime = 0f;
        }

        stateTime += delta;

        TextureRegion frame;

        switch (currentState) {

            case RUNNING:
                frame = runningAnimation
                        .getKeyFrame(stateTime);
                break;

            case CHARGING:
                frame = chargeAnimation
                        .getKeyFrame(stateTime);
                break;

            default:
                frame = idleAnimation
                        .getKeyFrame(stateTime);
                break;
        }

        sprite.setRegion(frame);

        sprite.setFlip(
                facingLeft,
                false);

        sprite.setPosition(
                x - sprite.getWidth() / 2f,
                y - sprite.getHeight() / 2f);
    }

    public void render(SpriteBatch batch) {
        sprite.draw(batch);
    }
}
