package com.niko.littlepiggy.projectile;

import com.niko.littlepiggy.projectile.Projectile;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.World;

public class Pellet extends Projectile {

    private static final float SPEED = 10f;
    private static final float DAMAGE = 5f;
    private static final float RADIUS = 0.04f;
    private static final float MAX_LIFETIME = 2f;
    private static final float KNOCKBACK = 2f;

    public Pellet(
            World world,
            Object owner,
            float x,
            float y,
            float speedMultiplier,
            Vector2 direction) {
        super(world, owner, x, y, direction, SPEED * speedMultiplier, DAMAGE, RADIUS, KNOCKBACK);
    }

    @Override
    protected float getMaxLifeTime() {
        return MAX_LIFETIME;
    }

}
