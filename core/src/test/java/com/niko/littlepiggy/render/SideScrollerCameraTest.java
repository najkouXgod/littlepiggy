package com.niko.littlepiggy.render;

/** Run with core:test; no graphics context needed. */
public final class SideScrollerCameraTest {
    @org.junit.Test
    public void cameraRegression() {
        SideScrollerCamera camera = new SideScrollerCamera();
        camera.update(1f, 1.5f, 0f, 0f, 16f, 9f, 60f, 25f);
        near(camera.getX(), 8f, "spawn clamps to left edge");
        near(camera.getY(), 4.5f, "spawn clamps to floor");
        for (int i = 0; i < 120; i++) {
            float jumpY = 1.5f + 2f * (float) Math.sin(Math.PI * i / 120f);
            camera.update(1f, jumpY, 0f, 1f / 60f, 16f, 9f, 60f, 25f);
            near(camera.getY(), 4.5f, "normal jump keeps camera steady");
        }
        camera.update(59f, 24f, 100f, 1f / 60f, 16f, 9f, 60f, 25f);
        bounded(camera.getX(), 8f, 52f, "horizontal map limits");
        bounded(camera.getY(), 4.5f, 20.5f, "vertical map limits");
        bounded(59f - camera.getX(), -8f, 8f, "fast movement stays visible");
        near(SideScrollerCamera.clampCenter(-100f, 16f, 8f), 4f, "small map centered");
        near(SideScrollerCamera.clampCenter(100f, 9f, 3f), 1.5f, "short map centered");
        near(trackAtFps(30), trackAtFps(120), "stationary target convergence independent of FPS");
        System.out.println("Camera regression checks passed");
    }
    private static float trackAtFps(int fps) {
        SideScrollerCamera c = new SideScrollerCamera();
        c.update(20f, 10f, 0f, 0f, 16f, 9f, 60f, 25f);
        for (int i = 0; i < fps; i++) c.update(23f, 10f, 0f, 1f / fps, 16f, 9f, 60f, 25f);
        return c.getX();
    }
    private static void near(float actual, float expected, String message) {
        if (Math.abs(actual - expected) > 0.002f) throw new AssertionError(message + ": " + actual);
    }
    private static void bounded(float actual, float min, float max, String message) {
        if (actual < min || actual > max) throw new AssertionError(message + ": " + actual);
    }
}
