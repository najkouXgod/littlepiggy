package com.niko.littlepiggy.player;

import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

import com.niko.littlepiggy.assets.GameAssets;
import com.niko.littlepiggy.fx.HitFlash;

public class PlayerAnimator {

    private enum AnimationState {
        IDLE,
        RUNNING,
        CHARGING,
        DASHING,
        BACKFLIP,
        GROUND_SLAM_AIR,
        GROUND_SLAM_LAND
    }

    private static final float BACKFLIP_FRAME_TIME = 0.055f;
    private static final float GROUND_SLAM_FRAME_TIME = 0.06f;

    private final Animation<TextureRegion> groundSlamAirAnimation;
    private final Animation<TextureRegion> groundSlamLandAnimation;

    private boolean groundSlamAirActive;
    private boolean groundSlamLandingActive;

    private final Animation<TextureRegion> idleAnimation;
    private final Animation<TextureRegion> runningAnimation;
    private final Animation<TextureRegion> chargeAnimation;
    private final Animation<TextureRegion> dashAnimation;
    private final Animation<TextureRegion> backflipAnimation;

    private final Sprite sprite;
    private final HitFlash hitFlash = new HitFlash();

    private AnimationState currentState = AnimationState.IDLE;

    private float stateTime;

    private boolean backflipAnimationActive;
    private boolean backflipWasAirborne;

    public PlayerAnimator(GameAssets assets) {

        /*
         * Sista raden i piggysheet:
         *
         * row 4
         *
         * frames 0-3 = start/windup
         * frames 4-6 = mark-impact
         */
        TextureRegion[] groundSlamAirFrames = assets.getRowFrames(
                GameAssets.PIG_SHEET,
                4,
                0,
                4,
                64,
                64);

        groundSlamAirAnimation = new Animation<>(
                GROUND_SLAM_FRAME_TIME,
                groundSlamAirFrames);

        groundSlamAirAnimation.setPlayMode(
                Animation.PlayMode.NORMAL);

        TextureRegion[] groundSlamLandFrames = assets.getRowFrames(
                GameAssets.PIG_SHEET,
                4,
                4,
                3,
                64,
                64);

        groundSlamLandAnimation = new Animation<>(
                GROUND_SLAM_FRAME_TIME,
                groundSlamLandFrames);

        groundSlamLandAnimation.setPlayMode(
                Animation.PlayMode.NORMAL);

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

        chargeAnimation.setPlayMode(
                Animation.PlayMode.NORMAL);

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

        TextureRegion[] backflipFrames = assets.getRowFrames(
                GameAssets.PIG_SHEET,
                3,
                0,
                11,
                64,
                64);

        backflipAnimation = new Animation<>(
                BACKFLIP_FRAME_TIME,
                backflipFrames);

        backflipAnimation.setPlayMode(
                Animation.PlayMode.NORMAL);

        sprite = new Sprite(idleFrames[0]);

        sprite.setSize(
                1f,
                1f);
    }

    public void startBackflip() {

        backflipAnimationActive = true;
        backflipWasAirborne = false;

        currentState = AnimationState.BACKFLIP;
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

        hitFlash.update(delta);
        if (groundSlamLandingActive
                && groundSlamLandAnimation
                        .isAnimationFinished(stateTime)) {

            groundSlamLandingActive = false;
            groundSlamAirActive = false;
        }
        if (backflipAnimationActive && !grounded) {
            backflipWasAirborne = true;
        }

        /*
         * Avbryt först när spelaren faktiskt varit i luften
         * och sedan landar igen.
         *
         * Det gör att CTRL från marken inte stoppar
         * animationen direkt på första framen.
         */
        if (backflipAnimationActive
                && backflipWasAirborne
                && grounded) {

            backflipAnimationActive = false;
        }

        if (backflipAnimationActive
                && backflipAnimation.isAnimationFinished(stateTime)) {

            backflipAnimationActive = false;
        }
        if (groundSlamLandingActive
                && groundSlamLandAnimation
                        .isAnimationFinished(stateTime)) {

            groundSlamLandingActive = false;
        }
        AnimationState newState;

        if (groundSlamLandingActive) {

            newState = AnimationState.GROUND_SLAM_LAND;

        } else if (groundSlamAirActive) {

            newState = AnimationState.GROUND_SLAM_AIR;

        } else if (backflipAnimationActive) {

            newState = AnimationState.BACKFLIP;

        } else if (charging) {

            newState = AnimationState.CHARGING;

        } else if (dashing) {

            newState = AnimationState.DASHING;

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
            case GROUND_SLAM_AIR:

                /*
                 * När de första fyra framesen är slut
                 * hålls automatiskt sista framen medan
                 * grisen faller.
                 */
                frame = groundSlamAirAnimation
                        .getKeyFrame(stateTime);

                break;

            case GROUND_SLAM_LAND:

                frame = groundSlamLandAnimation
                        .getKeyFrame(stateTime);

                break;

            case RUNNING:

                frame = runningAnimation.getKeyFrame(stateTime);
                break;

            case CHARGING:

                frame = chargeAnimation.getKeyFrame(stateTime);
                break;

            case DASHING:

                frame = dashAnimation.getKeyFrame(stateTime);
                break;

            case BACKFLIP:

                frame = backflipAnimation.getKeyFrame(stateTime);
                break;

            default:

                frame = idleAnimation.getKeyFrame(stateTime);
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

    public void startGroundSlam() {

        /*
         * Ground slam avbryter eventuell
         * backflip-animation.
         */
        backflipAnimationActive = false;

        groundSlamLandingActive = false;
        groundSlamAirActive = true;

        currentState = AnimationState.GROUND_SLAM_AIR;

        stateTime = 0f;
    }

    public void landGroundSlam() {

        groundSlamAirActive = false;
        groundSlamLandingActive = true;

        currentState = AnimationState.GROUND_SLAM_LAND;

        stateTime = 0f;
    }

    public void triggerFlash() {
        hitFlash.trigger();
    }

    public void render(SpriteBatch batch) {

        hitFlash.begin(batch);
        sprite.draw(batch);
        hitFlash.end(batch);
    }
}
