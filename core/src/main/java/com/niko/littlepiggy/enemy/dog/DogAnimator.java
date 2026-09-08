package com.niko.littlepiggy.enemy.dog;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

import com.niko.littlepiggy.assets.GameAssets;
import com.niko.littlepiggy.fx.HitFlash;

public class DogAnimator {

    private static final float BASE_WIDTH = 1.1f;
    private static final float BASE_HEIGHT = 0.9f;

    /*
     * Windup: sprite krymper ihop lite (som att den samlar kraft).
     * Lunge: sprite sträcks ut i rörelseriktningen (fartkänsla).
     * Rent visuellt lapptäcke tills det finns en riktig dog-animation -
     * byt gärna ut mot faktiska frames senare, samma mönster som
     * PlayerAnimator/FarmerAnimator använder med assets.getRowFrames(...).
     */
    private static final float WINDUP_SCALE = 0.85f;
    private static final float LUNGE_STRETCH_X = 1.3f;
    private static final float LUNGE_SQUASH_Y = 0.8f;

    private final Sprite sprite;
    private final HitFlash hitFlash = new HitFlash();

    public DogAnimator(GameAssets assets) {

        /*
         * PLACEHOLDER: återanvänder farmer-texturen tonad brun
         * tills det finns en egen dog-sprite. Byt GameAssets.DOG_IDLE
         * till din egna fil när den är på plats, och ta bort
         * sprite.setColor-raden nedan.
         */
        sprite = new Sprite(
                assets.getTexture(
                        GameAssets.DOG_IDLE));

        sprite.setColor(
                new Color(0.55f, 0.35f, 0.22f, 1f));

        sprite.setSize(BASE_WIDTH, BASE_HEIGHT);
    }

    public void update(
            float delta,
            float x,
            float y,
            boolean facingLeft,
            boolean windingUp,
            boolean lunging) {

        sprite.setFlip(!facingLeft, false);

        float width = BASE_WIDTH;
        float height = BASE_HEIGHT;

        if (windingUp) {

            width *= WINDUP_SCALE;
            height *= WINDUP_SCALE;

        } else if (lunging) {

            width *= LUNGE_STRETCH_X;
            height *= LUNGE_SQUASH_Y;
        }

        sprite.setSize(width, height);

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
