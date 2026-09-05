package com.niko.littlepiggy.enemy;

import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

import com.niko.littlepiggy.assets.GameAssets;

public class FarmerAnimator {

    private final Sprite sprite;

    public FarmerAnimator(GameAssets assets) {

        sprite = new Sprite(
                assets.getTexture(
                        GameAssets.FARMER_IDLE));

        sprite.setSize(1.5f, 1.5f);
    }

    public void update(
            float delta,
            float x,
            float y,
            boolean facingLeft) {

        /*
         * Behåller beteendet från din nuvarande
         * Farmer.java.
         *
         * Om farmer.png i grunden är ritad åt andra
         * hållet kan denna senare bytas till facingLeft.
         */
        sprite.setFlip(
                !facingLeft,
                false);

        sprite.setPosition(
                x - sprite.getWidth() / 2f,
                y - sprite.getHeight() / 2f);
    }

    public void render(SpriteBatch batch) {
        sprite.draw(batch);
    }
}
