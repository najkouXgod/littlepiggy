package com.niko.littlepiggy.player;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.World;

import com.niko.littlepiggy.assets.GameAssets;

public class Player {

    private final PlayerStats playerStats;
    private final PlayerPhysics physics;
    private final PlayerController controller;
    private final PlayerAnimator animator;

    public Player(
            PlayerStats playerStats,
            World world,
            float startX,
            float startY,
            GameAssets assets) {

        this.playerStats = playerStats;

        physics = new PlayerPhysics(
                world,
                startX,
                startY);

        /*
         * Viktigt:
         * Box2D-body:n identifieras fortfarande som Player.
         *
         * Det gör att t.ex. Farmer range-sensorn fortfarande
         * kan använda:
         *
         * body.getUserData() instanceof Player
         */
        physics.setOwner(this);

        controller = new PlayerController(physics);

        animator = new PlayerAnimator(assets);
    }

    public void update(float delta) {

        controller.update(delta);

        animator.update(
                delta,
                physics.getX(),
                physics.getY(),
                controller.isMoving(),
                controller.isCharging(),
                controller.isFacingLeft());
    }

    public void render(SpriteBatch batch) {
        animator.render(batch);
    }

    public float getX() {
        return physics.getX();
    }

    public float getY() {
        return physics.getY();
    }

    public Vector2 getPosition() {
        return physics.getPosition();
    }

    public Vector2 getVelocity() {
        return physics.getVelocity();
    }

    public boolean isGrounded() {
        return physics.isGrounded();
    }

    public void beginGroundContact() {
        physics.beginGroundContact();
    }

    public void endGroundContact() {
        physics.endGroundContact();
    }

    public boolean isCharging() {
        return controller.isCharging();
    }

    public float getJumpCharge() {
        return controller.getJumpCharge();
    }

    public void applyKnockback(float x, float y) {
        physics.applyImpulse(x, y);
    }
}
