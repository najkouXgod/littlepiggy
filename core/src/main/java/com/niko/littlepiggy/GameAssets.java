package com.niko.littlepiggy;

import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

public class GameAssets extends AssetManager{

    private final AssetManager manager;

    public static final String PIG_IDLE = "piggy/idle.png";
    public static final String PIG_RUNNING = "piggy/running.png";
    public static final String PIG_CHARGING = "piggy/charging.png";

    public GameAssets() {
        manager = new AssetManager();
    }

    public void loadAll() {
        manager.load(PIG_IDLE, Texture.class);
        manager.load(PIG_RUNNING, Texture.class);
        manager.load(PIG_CHARGING, Texture.class);
    }

    public void finishLoading() {
        manager.finishLoading();
    }

    public Texture getTexture( String path ) {
        return manager.get(path, Texture.class);
    }

    public TextureRegion[] getFrames(String path, int frameCols, int frameRows) {
        Texture texture = getTexture(path);
        TextureRegion[][] tmp = TextureRegion.split(texture, texture.getWidth() / frameCols, texture.getHeight() / frameRows);
        TextureRegion[] frames = new TextureRegion[frameCols * frameRows];
        int index = 0;
        for (int i = 0; i < frameRows; i++) {
            for (int j = 0; j < frameCols; j++) {
                frames[index++] = tmp[i][j];
            }
        }
        return frames;
    }

    public void dispose() {
        manager.dispose();
    }
}
