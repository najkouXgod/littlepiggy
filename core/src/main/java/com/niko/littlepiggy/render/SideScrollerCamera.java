package com.niko.littlepiggy.render;

/** Camera tracking in world units, independent of rendering and screen shake. */
public final class SideScrollerCamera {
    private float x;
    private float y;
    private float lookAhead;
    private boolean initialized;

    public void update(float playerX, float playerY, float velocityX, float delta,
                       float viewWidth, float viewHeight, float mapWidth, float mapHeight) {
        if (!initialized) {
            x = clampCenter(playerX, viewWidth, mapWidth);
            y = clampCenter(playerY + viewHeight * 0.25f, viewHeight, mapHeight);
            initialized = true;
        }
        float dt = Math.max(0f, Math.min(delta, 0.1f));
        // Capped anticipation: a dash must not throw the camera ahead of the player.
        float desiredLookAhead = Math.max(-1.5f, Math.min(1.5f, velocityX * 0.25f));
        lookAhead += (desiredLookAhead - lookAhead) * smoothing(5f, dt);
        float targetX = clampCenter(playerX + lookAhead, viewWidth, mapWidth);
        x += (targetX - x) * smoothing(9f, dt);

        // Normal jumps stay inside this band. Larger climbs/falls move the camera.
        float lower = y - viewHeight * 0.30f;
        float upper = y;
        float targetY = y;
        if (playerY < lower) targetY += playerY - lower;
        if (playerY > upper) targetY += playerY - upper;
        targetY = clampCenter(targetY, viewHeight, mapHeight);
        y += (targetY - y) * smoothing(7f, dt);

        // Safety band prevents fast movement from leaving the player off-screen.
        x = Math.max(playerX - viewWidth * 0.40f, Math.min(x, playerX + viewWidth * 0.40f));
        y = Math.max(playerY - viewHeight * 0.40f, Math.min(y, playerY + viewHeight * 0.40f));
        x = clampCenter(x, viewWidth, mapWidth);
        y = clampCenter(y, viewHeight, mapHeight);
    }

    private static float smoothing(float speed, float delta) {
        return 1f - (float) Math.exp(-speed * delta);
    }

    public static float clampCenter(float center, float viewSize, float mapSize) {
        if (mapSize <= viewSize) return mapSize * 0.5f;
        return Math.max(viewSize * 0.5f, Math.min(center, mapSize - viewSize * 0.5f));
    }

    public float getX() { return x; }
    public float getY() { return y; }
}
