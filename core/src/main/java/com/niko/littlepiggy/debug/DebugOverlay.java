package com.niko.littlepiggy.debug;

import java.util.Locale;

import com.niko.littlepiggy.player.Player;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector2;

public class DebugOverlay {

    private static final int MAX_LOG_LINES = 8;

    private final Player player;

    private final BitmapFont font;
    private final SpriteBatch batch;
    private final ShapeRenderer shapes;
    private final OrthographicCamera camera;

    private final String[] log = new String[MAX_LOG_LINES];

    private int logHead;
    private int logCount;

    private boolean visible;

    public DebugOverlay(Player player) {

        this.player = player;

        font = new BitmapFont();
        batch = new SpriteBatch();
        shapes = new ShapeRenderer();

        camera = new OrthographicCamera();

        resize(
                Gdx.graphics.getWidth(),
                Gdx.graphics.getHeight());
    }

    public void update(float delta) {

        if (Gdx.input.isKeyJustPressed(
                Input.Keys.F1)) {

            visible = !visible;
        }
    }

    public void render() {

        if (!visible) {
            return;
        }

        float screenHeight = Gdx.graphics.getHeight();

        float panelX = 12f;
        float panelY = screenHeight - 12f;

        float panelWidth = 340f;
        float panelHeight = 285f;

        drawBackground(
                panelX,
                panelY - panelHeight,
                panelWidth,
                panelHeight);

        batch.setProjectionMatrix(
                camera.combined);

        batch.begin();

        float x = panelX + 12f;
        float y = panelY - 12f;

        float lineHeight = 18f;

        /*
         * Header
         */
        font.setColor(Color.YELLOW);

        font.draw(
                batch,
                "DEBUG  [F1 hide]",
                x,
                y);

        y -= lineHeight * 1.5f;

        /*
         * Performance
         */
        font.setColor(Color.LIGHT_GRAY);

        drawLine(
                "FPS",
                Integer.toString(
                        Gdx.graphics.getFramesPerSecond()),
                x,
                y);

        y -= lineHeight;

        /*
         * Player physics
         */
        Vector2 position = player.getPosition();

        Vector2 velocity = player.getVelocity();

        drawLine(
                "Position",
                format(
                        "%.2f, %.2f",
                        position.x,
                        position.y),
                x,
                y);

        y -= lineHeight;

        drawLine(
                "Velocity",
                format(
                        "%.2f, %.2f",
                        velocity.x,
                        velocity.y),
                x,
                y);

        y -= lineHeight;

        drawLine(
                "Grounded",
                Boolean.toString(
                        player.isGrounded()),
                x,
                y);

        y -= lineHeight * 1.3f;

        /*
         * Health
         */
        font.setColor(Color.WHITE);

        drawLine(
                "Health",
                format(
                        "%.0f / %.0f",
                        player.getHealth(),
                        player.getMaxHealth()),
                x,
                y);

        y -= lineHeight * 1.3f;

        /*
         * Combat
         */
        font.setColor(Color.CYAN);

        drawLine(
                "Combat",
                player.getCombatState(),
                x,
                y);

        y -= lineHeight;

        drawLine(
                "Charging",
                Boolean.toString(
                        player.isCharging()),
                x,
                y);

        y -= lineHeight;

        drawLine(
                "Dashing",
                Boolean.toString(
                        player.isDashing()),
                x,
                y);

        y -= lineHeight;

        drawLine(
                "Dash charge",
                format(
                        "%.0f%%  (%.2fs)",
                        player.getDashCharge() * 100f,
                        player.getDashChargeTime()),
                x,
                y);

        batch.end();

        /*
         * Separat charge-bar.
         */
        drawChargeBar(
                panelX + 12f,
                panelY - 220f,
                panelWidth - 24f,
                8f,
                player.getDashCharge());

        /*
         * Event log.
         */
        drawLog(
                panelX + 12f,
                panelY - 245f);
    }

    private void drawBackground(
            float x,
            float y,
            float width,
            float height) {

        shapes.setProjectionMatrix(
                camera.combined);

        Gdx.gl.glEnable(
                GL20.GL_BLEND);

        Gdx.gl.glBlendFunc(
                GL20.GL_SRC_ALPHA,
                GL20.GL_ONE_MINUS_SRC_ALPHA);

        shapes.begin(
                ShapeRenderer.ShapeType.Filled);

        shapes.setColor(
                0f,
                0f,
                0f,
                0.75f);

        shapes.rect(
                x,
                y,
                width,
                height);

        shapes.end();

        Gdx.gl.glDisable(
                GL20.GL_BLEND);
    }

    private void drawChargeBar(
            float x,
            float y,
            float width,
            float height,
            float charge) {

        charge = Math.max(
                0f,
                Math.min(1f, charge));

        shapes.setProjectionMatrix(
                camera.combined);

        shapes.begin(
                ShapeRenderer.ShapeType.Filled);

        /*
         * Bakgrund
         */
        shapes.setColor(
                Color.DARK_GRAY);

        shapes.rect(
                x,
                y,
                width,
                height);

        /*
         * Charge
         */
        shapes.setColor(
                Color.ORANGE);

        shapes.rect(
                x,
                y,
                width * charge,
                height);

        shapes.end();
    }

    private void drawLog(
            float x,
            float y) {

        batch.setProjectionMatrix(
                camera.combined);

        batch.begin();

        font.setColor(Color.GRAY);

        font.draw(
                batch,
                "Events:",
                x,
                y);

        y -= 17f;

        /*
         * Visa senaste meddelandet först.
         */
        for (int i = 0; i < logCount; i++) {

            int index = (logHead - 1 - i
                    + MAX_LOG_LINES)
                    % MAX_LOG_LINES;

            font.draw(
                    batch,
                    log[index],
                    x,
                    y);

            y -= 15f;
        }

        batch.end();
    }

    private void drawLine(
            String label,
            String value,
            float x,
            float y) {

        font.draw(
                batch,
                label + ": " + value,
                x,
                y);
    }

    private String format(
            String format,
            Object... args) {

        return String.format(
                Locale.US,
                format,
                args);
    }

    public void log(String message) {

        log[logHead] = message;

        logHead = (logHead + 1)
                % MAX_LOG_LINES;

        logCount = Math.min(
                logCount + 1,
                MAX_LOG_LINES);

        Gdx.app.log(
                "DEBUG",
                message);
    }

    public void resize(
            int width,
            int height) {

        camera.setToOrtho(
                false,
                width,
                height);

        camera.update();
    }

    public void dispose() {

        font.dispose();
        batch.dispose();
        shapes.dispose();
    }
}
