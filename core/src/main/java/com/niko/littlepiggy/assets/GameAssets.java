package com.niko.littlepiggy.assets;

import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

public class GameAssets {

    private final AssetManager manager;

    public static final String WIN_SCREEN = "screens/winscreen.png";
    public static final String SKY = "background/bg.png";

    public static final String GAME_OVER = "screens/gameover.png";

    public static final String PIG_SHEET = "piggy/piggysheet.png";

    public static final String FARMER_IDLE = "farmer/farmer.png";

    public static final String FARMER_SHOOTING = "farmer/shooting.png";

    public static final String SFX_DASH_SWIPE = "sounds/clean_swipe.wav";
    public static final String SFX_DASH_HIT = "sounds/clean_punch.wav";
    public static final String SFX_BACKFLIP_HIT = "sounds/clean_punch_2.wav";
    public static final String SFX_FARMER_SHOT = "sounds/clean_shot_muffled.wav";

    /*
     * Volym per ljud. shot_muffled och punch_2 är taget från paket
     * som ofta är inspelade högre än swipe/punch, så vi trimmar dem
     * lite i koden istället för att göra om filerna.
     */
    private static final float VOLUME_DASH_SWIPE = 0.5f;
    private static final float VOLUME_DASH_HIT = 0.5f;
    private static final float VOLUME_BACKFLIP_HIT = 0.5f;
    private static final float VOLUME_FARMER_SHOT = 0.5f;

    public GameAssets() {
        manager = new AssetManager();
    }

    public void loadAll() {

        manager.load(PIG_SHEET, Texture.class);

        manager.load(FARMER_IDLE, Texture.class);
        manager.load(FARMER_SHOOTING, Texture.class);

        manager.load(SKY, Texture.class);
        manager.load(GAME_OVER, Texture.class);
        manager.load(WIN_SCREEN, Texture.class);

        manager.load(SFX_DASH_SWIPE, Sound.class);
        manager.load(SFX_DASH_HIT, Sound.class);
        manager.load(SFX_BACKFLIP_HIT, Sound.class);
        manager.load(SFX_FARMER_SHOT, Sound.class);
    }

    public void finishLoading() {
        manager.finishLoading();
    }

    public Texture getTexture(String path) {
        return manager.get(path, Texture.class);
    }

    public Sound getSound(String path) {
        return manager.get(path, Sound.class);
    }

    /**
     * Spelar ett ljud med en förvald volym (se VOLUME_-konstanterna).
     * Om ljudet inte har en förvald volym spelas det på full volym.
     */
    public void playSound(String path) {
        playSound(path, defaultVolumeFor(path));
    }

    public void playSound(String path, float volume) {
        getSound(path).play(volume);
    }

    private float defaultVolumeFor(String path) {

        if (SFX_DASH_SWIPE.equals(path)) {
            return VOLUME_DASH_SWIPE;
        }

        if (SFX_DASH_HIT.equals(path)) {
            return VOLUME_DASH_HIT;
        }

        if (SFX_BACKFLIP_HIT.equals(path)) {
            return VOLUME_BACKFLIP_HIT;
        }

        if (SFX_FARMER_SHOT.equals(path)) {
            return VOLUME_FARMER_SHOT;
        }

        return 1f;
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
