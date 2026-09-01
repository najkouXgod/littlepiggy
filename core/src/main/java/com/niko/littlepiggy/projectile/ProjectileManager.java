package com.niko.littlepiggy.projectile;

import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.physics.box2d.World;

public class ProjectileManager {

    private final Array<Projectile> projectiles = new Array<>();
    private final World world;

    public ProjectileManager(World world) {
        this.world = world;
    }

    public void add(Projectile projectile) {
        if (projectile != null) {
            projectiles.add(projectile);
        }
    }

    public void addAll(Array<? extends Projectile> newProjectiles) {
        if (newProjectiles != null) {
            projectiles.addAll(newProjectiles);
        }
    }

    public void update(float delta) {
        for (int i = projectiles.size - 1; i >= 0; i--) {

            Projectile projectile = projectiles.get(i);

            projectile.update(delta);

            if (projectile.shouldRemove()) {
                world.destroyBody(projectile.getBody());
                projectiles.removeIndex(i);
            }
        }
    }

    public Array<Projectile> getProjectiles() {
        return projectiles;
    }

}
