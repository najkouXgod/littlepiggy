package com.niko.littlepiggy.assets;

import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

public class GameAssets {

    private final AssetManager manager;

    public static final String SKY = "background/bg.png";

    public static final String GAME_OVER = "screens/gameover.png";

    public static final String PIG_SHEET = "piggy/piggysheet.png";

    public static final String FARMER_IDLE = "farmer/farmer.png";

    public static final String FARMER_SHOOTING = "farmer/shooting.png";

    public GameAssets() {
        manager = new AssetManager();
    }

    public void loadAll() {

        manager.load(PIG_SHEET, Texture.class);

        manager.load(FARMER_IDLE, Texture.class);
        manager.load(FARMER_SHOOTING, Texture.class);

        manager.load(SKY, Texture.class);
        manager.load(GAME_OVER, Texture.class);
    }

    public void finishLoading() {
        manager.finishLoading();
    }

    public Texture getTexture(String path) {
        return manager.get(path, Texture.class);
    }

    /*
     * Hämtar ett visst antal frames från
     * en specifik rad i ett spritesheet.
     *
     * row är 0-indexerad:
     *
     * row 0 = idle
     * row 1 = running
     * row 2 = charging
     * row 3 = jab
     */
    public TextureRegion[] getRowFrames(
            String path,
            int row,
            int frameCount,
            int frameWidth,
            int frameHeight) {

        return getRowFrames(
                path,
                row,
                0,
                frameCount,
                frameWidth,
                frameHeight);
    }

    public TextureRegion[] getRowFrames(
            String path,
            int row,
            int startColumn,
            int frameCount,
            int frameWidth,
            int frameHeight) {

        Texture texture = getTexture(path);

        TextureRegion[][] sheet = TextureRegion.split(
                texture,
                frameWidth,
                frameHeight);

        TextureRegion[] frames = new TextureRegion[frameCount];

        for (int i = 0; i < frameCount; i++) {
            frames[i] = sheet[row][startColumn + i];
        }

        return frames;
    }

    public void dispose() {
        manager.dispose();
    }
}
