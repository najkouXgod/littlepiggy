package com.niko.littlepiggy.enemy.dog;

import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

import com.niko.littlepiggy.assets.GameAssets;
import com.niko.littlepiggy.fx.HitFlash;

public class DogAnimator {

    private enum State {
        IDLE,
        RUNNING,
        ATTACKING
    }

    private static final float WIDTH = 1.1f;
    private static final float HEIGHT = 0.9f;

    private static final float IDLE_FRAME_TIME = 0.16f;
    private static final float RUN_FRAME_TIME = 0.08f;

    /*
     * AI:
     *
     * windup = 0.35
     * lunge = 0.20
     *
     * Totalt ungefär 0.55 sek.
     *
     * 5 frames * 0.11 = 0.55 sek.
     */
    private static final float ATTACK_FRAME_TIME = 0.11f;

    private final Animation<TextureRegion> idleAnimation;
    private final Animation<TextureRegion> runAnimation;
    private final Animation<TextureRegion> attackAnimation;

    private final Sprite sprite;

    private final HitFlash hitFlash = new HitFlash();

    private State currentState = State.IDLE;

    private float stateTime;

    private boolean attackActive;

    public DogAnimator(GameAssets assets) {

        /*
         * Row 0:
         * idle
         */
        TextureRegion[] idleFrames = assets.getRowFrames(
                GameAssets.DOG_SHEET,
                0,
                5,
                64,
                64);

        idleAnimation = new Animation<>(
                IDLE_FRAME_TIME,
                idleFrames);

        idleAnimation.setPlayMode(
                Animation.PlayMode.LOOP);

        /*
         * Row 1:
         * running / chasing
         */
        TextureRegion[] runFrames = assets.getRowFrames(
                GameAssets.DOG_SHEET,
                1,
                5,
                64,
                64);

        runAnimation = new Animation<>(
                RUN_FRAME_TIME,
                runFrames);

        runAnimation.setPlayMode(
                Animation.PlayMode.LOOP);

        /*
         * Row 2:
         * windup + lunge + recovery
         */
        TextureRegion[] attackFrames = assets.getRowFrames(
                GameAssets.DOG_SHEET,
                2,
                5,
                64,
                64);

        attackAnimation = new Animation<>(
                ATTACK_FRAME_TIME,
                attackFrames);

        attackAnimation.setPlayMode(
                Animation.PlayMode.NORMAL);

        sprite = new Sprite(idleFrames[0]);

        sprite.setSize(
                WIDTH,
                HEIGHT);
    }

    public void startAttack() {

        attackActive = true;

        currentState = State.ATTACKING;

        stateTime = 0f;
    }

    public void update(
            float delta,
            float x,
            float y,
            boolean facingLeft,
            boolean chasing) {

        hitFlash.update(delta);

        /*
         * Attackanimationen får spela klart.
         */
        if (attackActive
                && attackAnimation
                        .isAnimationFinished(stateTime)) {

            attackActive = false;
        }

        State newState;

        if (attackActive) {

            newState = State.ATTACKING;

        } else if (chasing) {

            newState = State.RUNNING;

        } else {

            newState = State.IDLE;
        }

        /*
         * Nollställ animationstid när state byts.
         */
        if (newState != currentState) {

            currentState = newState;

            stateTime = 0f;
        }

        stateTime += delta;

        TextureRegion frame;

        switch (currentState) {

            case RUNNING:

                frame = runAnimation.getKeyFrame(
                        stateTime);

                break;

            case ATTACKING:

                frame = attackAnimation.getKeyFrame(
                        stateTime);

                break;

            case IDLE:
            default:

                frame = idleAnimation.getKeyFrame(
                        stateTime);

                break;
        }

        sprite.setRegion(frame);

        /*
         * Sheetet är ritat mot höger.
         */
        sprite.setFlip(
                facingLeft,
                false);

        sprite.setSize(
                WIDTH,
                HEIGHT);

        sprite.setPosition(
                x - WIDTH / 2f,
                y - HEIGHT / 2f);
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
