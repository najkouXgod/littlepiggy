package com.niko.littlepiggy.projectile;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;

public class ProjectileRenderer {

    private static final float VISUAL_RADIUS = 0.04f;

    private final ShapeRenderer shapeRenderer;

    public ProjectileRenderer() {
        shapeRenderer = new ShapeRenderer();
    }

    public void render(
            OrthographicCamera camera,
            ProjectileManager projectileManager) {

        shapeRenderer.setProjectionMatrix(camera.combined);

        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.setColor(Color.BLACK);

        for (Projectile projectile : projectileManager.getProjectiles()) {

            float x = projectile.getBody().getPosition().x;
            float y = projectile.getBody().getPosition().y;

            shapeRenderer.circle(
                    x,
                    y,
                    VISUAL_RADIUS,
                    8);
        }

        shapeRenderer.end();
    }

    public void dispose() {
        shapeRenderer.dispose();
    }
}
