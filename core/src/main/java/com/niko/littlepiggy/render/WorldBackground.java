package com.niko.littlepiggy.render;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.maps.MapProperties;
import com.badlogic.gdx.utils.Disposable;

/** World-anchored scenery. Scale and ground alignment are level data, never camera data. */
public final class WorldBackground implements Disposable {
    private final TextureRegion normal;
    private final TextureRegion mirrored;
    private final TextureRegion skyEdge;
    private final TextureRegion mirroredSkyEdge;
    private final Texture pixel;
    private final Color skyColor;
    private final float width;
    private final float height;
    private final float bottom;
    private final float groundY;

    public WorldBackground(Texture texture, MapProperties properties) {
        width = properties.get("backgroundWorldWidth", 20f, Float.class);
        groundY = properties.get("backgroundGroundY", 1f, Float.class);
        int sourceGround = properties.get("backgroundGroundPixelY", 730, Integer.class);
        int sourceBottom = properties.get("backgroundCropBottom", 774, Integer.class);
        if (width <= 0f || sourceGround < 0 || sourceBottom <= sourceGround
                || sourceBottom > texture.getHeight()) {
            throw new IllegalArgumentException("Invalid background scale/crop in map properties");
        }
        float scale = width / texture.getWidth();
        height = sourceBottom * scale;
        bottom = groundY - (sourceBottom - sourceGround) * scale;
        normal = new TextureRegion(texture, 0, 0, texture.getWidth(), sourceBottom);
        mirrored = new TextureRegion(normal);
        mirrored.flip(true, false);
        skyEdge = new TextureRegion(texture, 0, 0, texture.getWidth(), 1);
        mirroredSkyEdge = new TextureRegion(skyEdge);
        mirroredSkyEdge.flip(true, false);
        // The existing painting isn't seamless: alternate mirrored panels so both
        // sides of every join sample the same edge. Never repeat the blue padding.
        texture.setWrap(Texture.TextureWrap.ClampToEdge, Texture.TextureWrap.ClampToEdge);
        skyColor = Color.valueOf(properties.get("backgroundSkyColor", "70AEC7", String.class));
        Pixmap white = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        white.setColor(Color.WHITE);
        white.fill();
        pixel = new Texture(white);
        white.dispose();
    }

    /** Caller owns begin/end and uses the same world projection as terrain/entities. */
    public void render(SpriteBatch batch, OrthographicCamera camera) {
        float viewWidth = camera.viewportWidth * camera.zoom;
        float viewHeight = camera.viewportHeight * camera.zoom;
        float left = camera.position.x - viewWidth / 2f;
        float viewBottom = camera.position.y - viewHeight / 2f;
        batch.setColor(skyColor);
        batch.draw(pixel, left, viewBottom, viewWidth, viewHeight);
        // Explicit underground color instead of stretching the picture's last row.
        float earthTop = Math.min(groundY, viewBottom + viewHeight);
        if (earthTop > viewBottom) {
            batch.setColor(0.16f, 0.13f, 0.11f, 1f);
            batch.draw(pixel, left, viewBottom, viewWidth, earthTop - viewBottom);
        }
        batch.setColor(Color.WHITE);
        int first = (int) Math.floor(left / width);
        int last = (int) Math.ceil((left + viewWidth) / width);
        for (int tile = first; tile < last; tile++) {
            boolean flipped = (tile & 1) != 0;
            batch.draw(flipped ? mirrored : normal, tile * width, bottom, width, height);
            // This painting has a clear blue top row: continue that sky above
            // the finite artwork without a hard color seam on high jumps.
            float top = bottom + height;
            if (viewBottom + viewHeight > top) {
                batch.draw(flipped ? mirroredSkyEdge : skyEdge, tile * width, top,
                        width, viewBottom + viewHeight - top);
            }
        }
    }

    @Override
    public void dispose() {
        pixel.dispose(); // The scenery texture belongs to GameAssets.
    }
}
