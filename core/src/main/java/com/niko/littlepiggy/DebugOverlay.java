package com.niko.littlepiggy;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

/**
 * Tryck F1 för att visa/dölja.
 * Navigera med W/S, ändra värde med A/D.
 * Håll SHIFT för snabbare ändring (x5).
 */
public class DebugOverlay {

    private static class Param {
        String label;
        float  value, min, max, step;
        Param(String label, float value, float min, float max, float step) {
            this.label = label; this.value = value;
            this.min   = min;   this.max   = max;   this.step = step;
        }
        void increase(boolean fast) { value = Math.min(max, value + step * (fast ? 5 : 1)); apply(); }
        void decrease(boolean fast) { value = Math.max(min, value - step * (fast ? 5 : 1)); apply(); }
        void apply() {
            switch (label) {
                case "Speed":           DebugConfig.SPEED           = value; break;
                case "Jump min":        DebugConfig.JUMP_MINPOWER   = value; break;
                case "Jump max":        DebugConfig.JUMP_MAXPOWER   = value; break;
                case "Charge time":     DebugConfig.CHARGE_TIME     = value; break;
                case "Charge curve":    DebugConfig.CHARGE_CURVE    = value; break;
                case "Charge friction": DebugConfig.CHARGE_FRICTION = value; break;
                case "Tap threshold":   DebugConfig.TAP_THRESHOLD   = value; break;
            }
        }
    }

    private final Param[] params = {
        new Param("Speed",           DebugConfig.SPEED,           0.5f, 12f,   0.5f),
        new Param("Jump min",        DebugConfig.JUMP_MINPOWER,   0.5f, 5f,    0.25f),
        new Param("Jump max",        DebugConfig.JUMP_MAXPOWER,   1f,   8f,    0.25f),
        new Param("Charge time",     DebugConfig.CHARGE_TIME,     0.1f, 2f,    0.1f),
        new Param("Charge curve",    DebugConfig.CHARGE_CURVE,    0.5f, 5f,    0.25f),
        new Param("Charge friction", DebugConfig.CHARGE_FRICTION, 1f,   20f,   1f),
        new Param("Tap threshold",   DebugConfig.TAP_THRESHOLD,   0.01f, 0.2f, 0.01f),
    };

    private int     selected = 0;
    private boolean visible  = false;

    private final BitmapFont  font;
    private final SpriteBatch hudBatch;
    private final OrthographicCamera hudCam;
    private final Player player;

    private final String[] log    = new String[8];
    private int             logHead = 0;

    public DebugOverlay(Player player) {
        this.player = player;
        font        = new BitmapFont();
        hudBatch    = new SpriteBatch();
        hudCam      = new OrthographicCamera();
        hudCam.setToOrtho(false, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
    }

    public void update(float delta) {
        if (Gdx.input.isKeyJustPressed(Input.Keys.NUM_5)) visible = !visible;
        if (!visible) return;
        boolean shift = Gdx.input.isKeyPressed(Input.Keys.SHIFT_LEFT);

        if (Gdx.input.isKeyJustPressed(Input.Keys.W))
            selected = (selected - 1 + params.length) % params.length;
        if (Gdx.input.isKeyJustPressed(Input.Keys.S))
            selected = (selected + 1) % params.length;
        if (Gdx.input.isKeyJustPressed(Input.Keys.D))
            params[selected].increase(shift);
        if (Gdx.input.isKeyJustPressed(Input.Keys.A))
            params[selected].decrease(shift);
    }

    public void render() {
        if (!visible) return;

        hudBatch.setProjectionMatrix(hudCam.combined);
        hudBatch.begin();

        float x  = 14f;
        float y  = Gdx.graphics.getHeight() - 12f;
        float lh = 16f;

        font.setColor(Color.YELLOW);
        font.draw(hudBatch, "F1:dölj  W/S:välj  A/D:ändra  SHIFT:x5", x, y);
        y -= lh * 1.5f;

        for (int i = 0; i < params.length; i++) {
            Param p = params[i];
            font.setColor(i == selected ? Color.CYAN : Color.WHITE);
            font.draw(hudBatch,
                String.format("%s%-18s %.2f", i == selected ? "> " : "  ", p.label, p.value),
                x, y);
            y -= lh;
        }

        y -= lh * 0.5f;
        font.setColor(Color.LIGHT_GRAY);
        font.draw(hudBatch, String.format(
            "pos(%.1f, %.1f)  vel(%.1f, %.1f)  gnd:%b  chg:%b  pwr:%.2f",
            player.getX(), player.getY(),
            player.getVelocity().x, player.getVelocity().y,
            player.isGrounded(), player.isCharging(), player.getJumpCharge()
        ), x, y);
        y -= lh * 1.5f;

        font.setColor(Color.GRAY);
        for (int i = 0; i < log.length; i++) {
            int idx = (logHead + i) % log.length;
            if (log[idx] != null) { font.draw(hudBatch, log[idx], x, y); y -= lh; }
        }

        hudBatch.end();
    }

    /** Anropa utifrån vid viktiga händelser, t.ex. landning eller hopp. */
    public void log(String msg) {
        log[logHead] = msg;
        logHead = (logHead + 1) % log.length;
        Gdx.app.log("DBG", msg);
    }

    public void resize(int w, int h) {
        hudCam.setToOrtho(false, w, h);
    }

    public void dispose() {
        font.dispose();
        hudBatch.dispose();
    }
}
