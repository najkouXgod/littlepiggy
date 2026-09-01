package com.niko.littlepiggy.projectile;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.*;

public abstract class Projectile {

    protected final Body body;

    protected final float damage;
    protected final float knockback;

    protected float lifeTime;
    protected boolean remove;

    protected Projectile(
            World world,
            float x,
            float y,
            Vector2 direction,
            float speed,
            float damage,
            float radius,
            float knockback) {

        this.damage = damage;
        this.knockback = knockback;

        BodyDef bodyDef = new BodyDef();
        bodyDef.type = BodyDef.BodyType.DynamicBody;
        bodyDef.position.set(x, y);
        bodyDef.bullet = true;

        body = world.createBody(bodyDef);
        body.setGravityScale(0);

        CircleShape shape = new CircleShape();
        shape.setRadius(radius);

        FixtureDef fixtureDef = new FixtureDef();
        fixtureDef.shape = shape;
        fixtureDef.isSensor = true;

        Fixture fixture = body.createFixture(fixtureDef);
        fixture.setUserData(this);

        shape.dispose();

        body.setLinearVelocity(
                new Vector2(direction).nor().scl(speed));

    }

    public void update(float delta) {
        lifeTime += delta;

        if (lifeTime >= getMaxLifeTime()) {
            remove = true;
        }
    }

    public boolean shouldRemove() {
        return remove;
    }

    public void markForRemoval() {
        remove = true;
    }

    public float getDamage() {
        return damage;
    }

    public Body getBody() {
        return body;
    }

    public Vector2 getKnockbackImpulse() {

        return body.getLinearVelocity()
                .cpy()
                .nor()
                .scl(knockback);
    }

    protected abstract float getMaxLifeTime();

}
