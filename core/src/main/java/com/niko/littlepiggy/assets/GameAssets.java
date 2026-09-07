package com.niko.littlepiggy.assets;

import com.badlogic.gdx.Gdx;
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

    /*
     * PLACEHOLDER: pekar just nu på samma fil som FARMER_IDLE
     * eftersom det inte finns en egen dog-sprite än. Byt till en
     * riktig fil (och lägg till en manager.load(...)-rad i loadAll()
     * om det blir en annan fil) när konsten finns på plats.
     */
    public static final String DOG_IDLE = FARMER_IDLE;

    public static final String SFX_DASH_SWIPE = "sounds/swipe.wav";
    public static final String SFX_DASH_HIT = "sounds/punch.wav";
    public static final String SFX_BACKFLIP_HIT = "sounds/punch_2.wav";
    public static final String SFX_FARMER_SHOT = "sounds/shot_muffled.wav";

    /*
     * Volym per ljud. shot_muffled och punch_2 är taget från paket
     * som ofta är inspelade högre än swipe/punch, så vi trimmar dem
     * lite i koden istället för att göra om filerna.
     */
    private static final float VOLUME_DASH_SWIPE = 0.8f;
    private static final float VOLUME_DASH_HIT = 1f;
    private static final float VOLUME_BACKFLIP_HIT = 1f;
    private static final float VOLUME_FARMER_SHOT = 0.6f;

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
