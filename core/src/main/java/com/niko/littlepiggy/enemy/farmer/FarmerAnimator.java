package com.niko.littlepiggy.enemy.farmer;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

import com.niko.littlepiggy.assets.GameAssets;
import com.niko.littlepiggy.fx.HitFlash;

public class FarmerAnimator {

    private static final float SHOOTING_TIME = 0.25f;

    private final Texture idleTexture;
    private final Texture shootingTexture;

    private final Sprite sprite;

    private final HitFlash hitFlash = new HitFlash();

    private float shootingTimer;

    public FarmerAnimator(GameAssets assets) {

        idleTexture = assets.getTexture(
                GameAssets.FARMER_IDLE);

        shootingTexture = assets.getTexture(
                GameAssets.FARMER_SHOOTING);

        sprite = new Sprite(idleTexture);

        sprite.setSize(
                1.5f,
                1.5f);
    }

    public void startShooting() {

        shootingTimer = SHOOTING_TIME;

        sprite.setRegion(shootingTexture);
    }

    public void update(
            float delta,
            float x,
            float y,
            boolean facingLeft) {

        if (shootingTimer > 0f) {

            shootingTimer -= delta;

            sprite.setRegion(shootingTexture);

        } else {

            sprite.setRegion(idleTexture);
        }

        sprite.setFlip(
                !facingLeft,
                false);

        sprite.setPosition(
                x - sprite.getWidth() / 2f,
                y - sprite.getHeight() / 2f);

        hitFlash.update(delta);
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
