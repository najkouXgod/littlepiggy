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
        CHARGING,
        DASHING,
        DOUBLE_JUMP
    }

    private static final float DOUBLE_JUMP_FRAME_TIME = 0.055f;

    private final Animation<TextureRegion> idleAnimation;
    private final Animation<TextureRegion> runningAnimation;
    private final Animation<TextureRegion> chargeAnimation;
    private final Animation<TextureRegion> dashAnimation;
    private final Animation<TextureRegion> doubleJumpAnimation;

    private final Sprite sprite;

    private AnimationState currentState = AnimationState.IDLE;

    private float stateTime;
    private boolean doubleJumpAnimationActive;

    public PlayerAnimator(GameAssets assets) {

        TextureRegion[] idleFrames = assets.getRowFrames(
                GameAssets.PIG_SHEET,
                0,
                3,
                64,
                64);

        idleAnimation = new Animation<>(
                0.35f,
                idleFrames);

        idleAnimation.setPlayMode(
                Animation.PlayMode.LOOP);

        TextureRegion[] runningFrames = assets.getRowFrames(
                GameAssets.PIG_SHEET,
                1,
                5,
                64,
                64);

        runningAnimation = new Animation<>(
                0.07f,
                runningFrames);

        runningAnimation.setPlayMode(
                Animation.PlayMode.LOOP);

        /*
         * Row 2, columns 0-3
         */
        TextureRegion[] chargeFrames = assets.getRowFrames(
                GameAssets.PIG_SHEET,
                2,
                0,
                4,
                64,
                64);

        chargeAnimation = new Animation<>(
                0.10f,
                chargeFrames);

        /*
         * Spela charge-animationen en gång.
         * Om UP fortsätter hållas stannar den
         * på sista charge-framen.
         */
        chargeAnimation.setPlayMode(
                Animation.PlayMode.NORMAL);

        /*
         * Row 2, columns 4-11
         */
        TextureRegion[] dashFrames = assets.getRowFrames(
                GameAssets.PIG_SHEET,
                2,
                4,
                8,
                64,
                64);

        dashAnimation = new Animation<>(
                0.04f,
                dashFrames);

        dashAnimation.setPlayMode(
                Animation.PlayMode.NORMAL);

        /*
         * Fjärde raden i spritesheeten (row index 3).
         * 11 frames à 64x64: columns 0-10.
         */
        TextureRegion[] doubleJumpFrames = assets.getRowFrames(
                GameAssets.PIG_SHEET,
                3,
                0,
                11,
                64,
                64);

        doubleJumpAnimation = new Animation<>(
                DOUBLE_JUMP_FRAME_TIME,
                doubleJumpFrames);

        doubleJumpAnimation.setPlayMode(
                Animation.PlayMode.NORMAL);

        sprite = new Sprite(idleFrames[0]);

        sprite.setSize(
                1f,
                1f);
    }

    /**
     * Startar bakåtvolten från frame 0.
     * Det här är ett event, inte ett permanent movement-state.
     */
    public void startDoubleJump() {
        doubleJumpAnimationActive = true;
        currentState = AnimationState.DOUBLE_JUMP;
        stateTime = 0f;
    }

    public void update(
            float delta,
            float x,
            float y,
            boolean moving,
            boolean charging,
            boolean dashing,
            boolean grounded,
            boolean facingLeft) {

        /*
         * Om vi landar innan volten hunnit bli klar avbryts den,
         * så att grisen inte fortsätter snurra på marken.
         */
        if (doubleJumpAnimationActive && grounded) {
            doubleJumpAnimationActive = false;
        }

        if (doubleJumpAnimationActive
                && doubleJumpAnimation.isAnimationFinished(stateTime)) {

            doubleJumpAnimationActive = false;
        }

        AnimationState newState;

        if (doubleJumpAnimationActive) {

            newState = AnimationState.DOUBLE_JUMP;

        } else if (charging) {

            newState = AnimationState.CHARGING;

        } else if (dashing) {

            newState = AnimationState.DASHING;

        } else if (moving) {

            newState = AnimationState.RUNNING;

        } else {

            newState = AnimationState.IDLE;
        }

        /*
         * Ny animation börjar från frame 0.
         */
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

            case DASHING:

                frame = dashAnimation
                        .getKeyFrame(stateTime);

                break;

            case DOUBLE_JUMP:

                frame = doubleJumpAnimation
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
