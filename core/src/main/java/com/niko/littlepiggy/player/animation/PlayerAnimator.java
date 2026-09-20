package com.niko.littlepiggy.player.animation;

import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

import com.niko.littlepiggy.assets.GameAssets;
import com.niko.littlepiggy.fx.HitFlash;

/**
 * Visualiserar ett PlayerAnimationState. Inget mer.
 *
 * Animatorn avgör aldrig om en ability är "klar", om spelaren är i luften
 * eller vilken animation som har prioritet - det bestämmer gameplay-
 * systemen och skickar in färdigt state. Här finns bara:
 * - stateTime (nollställs när state byts)
 * - val av TextureRegion
 * - HitFlash och flipping
 */
public class PlayerAnimator {

    private static final float BACKFLIP_FRAME_TIME = 0.055f;
    private static final float GROUND_SLAM_FRAME_TIME = 0.06f;

    private final Animation<TextureRegion> idleAnimation;
    private final Animation<TextureRegion> runningAnimation;
    private final Animation<TextureRegion> chargeAnimation;
    private final Animation<TextureRegion> dashAnimation;
    private final Animation<TextureRegion> backflipAnimation;

    private final Animation<TextureRegion> groundSlamWindupAnimation;
    private final Animation<TextureRegion> groundSlamImpactAnimation;

    /** Sista windup-framen; hålls medan spelaren faller. */
    private final TextureRegion groundSlamFallFrame;

    private final Sprite sprite;
    private final HitFlash hitFlash = new HitFlash();

    private PlayerAnimationState currentState = PlayerAnimationState.IDLE;

    private float stateTime;

    public PlayerAnimator(GameAssets assets) {

        /*
         * Sista raden i piggysheet:
         *
         * row 4
         *
         * frames 0-3 = start/windup
         * frames 4-6 = mark-impact
         */
        TextureRegion[] groundSlamWindupFrames = assets.getRowFrames(
                GameAssets.PIG_SHEET,
                4,
                0,
                4,
                64,
                64);

        groundSlamWindupAnimation = new Animation<>(
                GROUND_SLAM_FRAME_TIME,
                groundSlamWindupFrames);

        groundSlamWindupAnimation.setPlayMode(
                Animation.PlayMode.NORMAL);

        groundSlamFallFrame = groundSlamWindupFrames[groundSlamWindupFrames.length - 1];

        TextureRegion[] groundSlamImpactFrames = assets.getRowFrames(
                GameAssets.PIG_SHEET,
                4,
                4,
                3,
                64,
                64);

        groundSlamImpactAnimation = new Animation<>(
                GROUND_SLAM_FRAME_TIME,
                groundSlamImpactFrames);

        groundSlamImpactAnimation.setPlayMode(
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

    public void update(
            float delta,
            PlayerAnimationState state,
            float x,
            float y,
            boolean facingLeft) {

        hitFlash.update(delta);

        if (state != currentState) {

            currentState = state;
            stateTime = 0f;
        }

        stateTime += delta;

        sprite.setRegion(getFrame());

        sprite.setFlip(
                facingLeft,
                false);

        sprite.setPosition(
                x - sprite.getWidth() / 2f,
                y - sprite.getHeight() / 2f);
    }

    private TextureRegion getFrame() {

        switch (currentState) {

            case GROUND_SLAM_WINDUP:
                return groundSlamWindupAnimation.getKeyFrame(stateTime);

            case GROUND_SLAM_FALL:
                return groundSlamFallFrame;

            case GROUND_SLAM_IMPACT:
                return groundSlamImpactAnimation.getKeyFrame(stateTime);

            case BACKFLIP:
                return backflipAnimation.getKeyFrame(stateTime);

            case DASH_CHARGE:
                return chargeAnimation.getKeyFrame(stateTime);

            case DASH:
                return dashAnimation.getKeyFrame(stateTime);

            case RUNNING:
                return runningAnimation.getKeyFrame(stateTime);

            case CROUCHING:
            case SWIMMING:
                // Ingen grafik än - visa idle tills sprites finns.
            case IDLE:
            default:
                return idleAnimation.getKeyFrame(stateTime);
        }
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
