package com.niko.littlepiggy.physics;

import com.niko.littlepiggy.player.Player;
import com.niko.littlepiggy.enemy.Farmer;

import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Box2DDebugRenderer;
import com.badlogic.gdx.physics.box2d.World;

public class PhysicsManager {

    private final World world;
    private final Box2DDebugRenderer debugRen;

    public PhysicsManager() {
        world = new World(new Vector2(0, -14f), true);
        debugRen = new Box2DDebugRenderer();
    }

    public void setContactListener(Player player) {
        world.setContactListener(new GameContactListener(player));
    }

    public void step(float delta) {
        world.step(delta, 6, 2);
    }

    public void renderDebug(OrthographicCamera camera) {
        debugRen.render(world, camera.combined);
    }

    public World getWorld() {
        return world;
    }

    public void dispose() {
        world.dispose();
        debugRen.dispose();
    }
}
