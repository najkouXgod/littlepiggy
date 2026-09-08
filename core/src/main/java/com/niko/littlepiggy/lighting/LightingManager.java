package com.niko.littlepiggy.lighting;

import box2dLight.PointLight;
import box2dLight.RayHandler;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.physics.box2d.Filter;
import com.badlogic.gdx.physics.box2d.World;

/**
 * Wrapper runt Box2DLights RayHandler. All tweaking av hur ljuset
 * ser ut/känns görs via konstanterna här, se kommentarerna vid varje.
 */
public class LightingManager {

    /*
     * VIKTIGT att förstå: Box2DLights skjuter osynliga rays genom din
     * Box2D-värld för att räkna ut skuggor. Det gör INGEN skillnad på
     * vanliga fixtures och sensorer - din Farmers/Dogs stora aggro-sensorer
     * (radie 3.5-5 world units!) och spelarens foot-sensor skulle annars
     * blockera ljus precis som en vägg, vilket ser trasigt ut.
     *
     * Lösningen: en egen Box2D "filter category" som BARA sätts på
     * ground-fixtures (se TerrainCollisionFactory), och vi säger till
     * Box2DLights att bara den kategorin ska räknas som ljusblockerare.
     * Detta är helt separat från den vanliga fysik-kollisionen och
     * påverkar inte hur saker krockar med varandra.
     */
    public static final short CATEGORY_LIGHT_BLOCKER = 0x0002;

    /*
     * Antal rays per ljuskälla. Fler = mjukare/rundare skuggkanter
     * men dyrare att rendera. 64 är ett bra default för punktljus
     * i den här storleken av spel. Sänk till ~32 om FPS blir ett
     * problem på Pi:n med många lampor samtidigt.
     */
    private static final int RAYS_PER_LIGHT = 64;

    /*
     * Antal blur-pass på ljuskartan. Högre = mjukare men dyrare.
     * 0 = helt skarpa/hackiga ljuskanter (pixligt, kan vara en stil-val).
     */
    private static final int BLUR_PASSES = 0;

    private final RayHandler rayHandler;
    private final Filter lightFilter;

    public LightingManager(World world) {

        rayHandler = new RayHandler(world);
        rayHandler.setBlurNum(BLUR_PASSES);

        lightFilter = new Filter();
        lightFilter.categoryBits = CATEGORY_LIGHT_BLOCKER;
        lightFilter.maskBits = CATEGORY_LIGHT_BLOCKER;

        setAmbientLight(0f);
    }

    /**
     * Styr hur mörkt/ljust hela banan är i grunden, INNAN några
     * lampor läggs till ovanpå.
     *
     * 0.0 = becksvart utanför ljuskällornas räckvidd.
     * 1.0 = fullt upplyst överallt, som om ljussystemet inte fanns.
     *
     * Ett vanligt "mysigt mörker"-värde ligger runt 0.25-0.45.
     */
    public void setAmbientLight(float amount) {
        rayHandler.setAmbientLight(amount, amount, amount, 1f);
    }

    /**
     * Skapar en punktljuskälla, t.ex. en lampa eller fackla.
     *
     * @param radius hur långt ljuset når, i world units (samma
     *               enhet som resten av spelet, dvs tiles).
     * @param color  ljusets färg. Alpha-kanalen styr intensiteten
     *               (1f = fullt intensivt, lägre = svagare/genomskinligare).
     */
    public PointLight createLamp(
            float x,
            float y,
            float radius,
            Color color) {

        PointLight light = new PointLight(
                rayHandler,
                RAYS_PER_LIGHT,
                color,
                radius,
                x,
                y);

        /*
         * Detta är vad som faktiskt gör att range-sensorer på
         * Farmer/Dog/Player INTE blockerar ljus - bara "ground"-
         * fixtures (se TerrainCollisionFactory) matchar filtret.
         */
        light.setContactFilter(lightFilter);

        /*
         * setSoft(true) ger mjuka, gradienta skuggkanter istället för
         * hårda streck. Nästan alltid vad man vill ha.
         */
        light.setSoft(true);

        return light;
    }

    /**
     * Anropas EN gång per frame, efter att du ritat klart hela
     * spelvärlden (terräng, spelare, fiender) men INNAN UI
     * (health bar, debug-overlay). Ritar ljus-lagret ovanpå scenen.
     */
    public void update(OrthographicCamera camera) {
        rayHandler.setCombinedMatrix(camera);
        rayHandler.updateAndRender();
    }

    public void dispose() {
        rayHandler.dispose();
    }
}
