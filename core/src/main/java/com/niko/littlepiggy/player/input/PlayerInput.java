package com.niko.littlepiggy.player.input;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;

/**
 * Enda platsen som läser tangentbordet för spelaren.
 *
 * Representerar bara spelarens INTENTIONER. Klassen ändrar ingen fysik,
 * har inga cooldowns och vet inget om animationer eller abilities.
 *
 * Anropa update() exakt en gång per Player.update(), först av allt.
 * "Pressed"-värden är true bara under den frame tangenten trycktes ned
 * (isKeyJustPressed), "Held"-värden är true så länge tangenten hålls.
 */
public class PlayerInput {

    private float moveAxis;

    private boolean jumpPressed;
    private boolean backflipPressed;
    private boolean groundSlamPressed;

    private boolean dashHeld;
    private boolean crouchHeld;

    public void update() {

        moveAxis = 0f;

        if (Gdx.input.isKeyPressed(Input.Keys.RIGHT)) {
            moveAxis = 1f;
        }

        // Vänster vinner om båda hålls, precis som tidigare.
        if (Gdx.input.isKeyPressed(Input.Keys.LEFT)) {
            moveAxis = -1f;
        }

        jumpPressed = Gdx.input.isKeyJustPressed(Input.Keys.SPACE);

        backflipPressed = Gdx.input.isKeyJustPressed(Input.Keys.CONTROL_LEFT)
                || Gdx.input.isKeyJustPressed(Input.Keys.CONTROL_RIGHT);

        groundSlamPressed = Gdx.input.isKeyJustPressed(Input.Keys.DOWN)
                || Gdx.input.isKeyJustPressed(Input.Keys.S);

        dashHeld = Gdx.input.isKeyPressed(Input.Keys.UP);

        /*
         * Crouch är inte implementerat än. Samma tangent som Ground Slam
         * (den fungerar bara i luften, crouch bara på marken) - byt här
         * om du vill ha en egen tangent.
         */
        crouchHeld = Gdx.input.isKeyPressed(Input.Keys.DOWN)
                || Gdx.input.isKeyPressed(Input.Keys.S);
    }

    /** -1 = vänster, 0 = ingen, 1 = höger. */
    public float getMoveAxis() {
        return moveAxis;
    }

    public boolean isJumpPressed() {
        return jumpPressed;
    }

    public boolean isBackflipPressed() {
        return backflipPressed;
    }

    public boolean isGroundSlamPressed() {
        return groundSlamPressed;
    }

    public boolean isDashHeld() {
        return dashHeld;
    }

    public boolean isCrouchHeld() {
        return crouchHeld;
    }
}
