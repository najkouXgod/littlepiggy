package com.niko.littlepiggy.enemy.farmer;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

import com.niko.littlepiggy.assets.GameAssets;
import com.niko.littlepiggy.fx.HitFlash;

public class FarmerAnimator {

    private static final float AIM_FRAME_TIME = 0.2f;
    private static final float SHOOT_TIME = 0.18f;

    private final Texture idleTexture;
    private final Texture shootingTexture;

    private final Animation<TextureRegion> aimingAnimation;

    private final Sprite sprite;

    private final HitFlash hitFlash = new HitFlash();

    private float aimStateTime;

    private float shootingTimer;

    private boolean wasAiming;

    public FarmerAnimator(GameAssets assets) {

        idleTexture = assets.getTexture(
                GameAssets.FARMER_IDLE);

        shootingTexture = assets.getTexture(
                GameAssets.FARMER_SHOOTING);

        TextureRegion[] aimingFrames = assets.getRowFrames(
                GameAssets.FARMER_AIMING,
                0,
                3,
                64,
                64);

        aimingAnimation = new Animation<>(
                AIM_FRAME_TIME,
                aimingFrames);

        aimingAnimation.setPlayMode(
                Animation.PlayMode.NORMAL);

        sprite = new Sprite(idleTexture);

        sprite.setSize(
                1.5f,
                1.5f);
    }

    public void startShooting() {

        shootingTimer = SHOOT_TIME;
    }

    public void update(
            float delta,
            float x,
            float y,
            boolean facingLeft,
            boolean aiming) {

        hitFlash.update(delta);

        /*
         * Börjar aiming från frame 0.
         */
        if (aiming && !wasAiming) {
            aimStateTime = 0f;
        }

        if (aiming) {
            aimStateTime += delta;
        }

        /*
         * Shooting har högsta prioritet.
         */
        if (shootingTimer > 0f) {

            shootingTimer -= delta;

            sprite.setRegion(
                    shootingTexture);

        } else if (aiming) {

            sprite.setRegion(
                    aimingAnimation.getKeyFrame(
                            aimStateTime));

        } else {

            sprite.setRegion(
                    idleTexture);
        }

        wasAiming = aiming;

        sprite.setFlip(
                !facingLeft,
                false);

        sprite.setPosition(
                x - sprite.getWidth() / 2f,
                y - sprite.getHeight() / 2f);
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
