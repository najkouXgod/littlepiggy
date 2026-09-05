package com.niko.littlepiggy.physics;

import com.niko.littlepiggy.combat.Damageable;
import com.niko.littlepiggy.level.Goal;
import com.niko.littlepiggy.projectile.Projectile;
import com.niko.littlepiggy.player.Player;
import com.niko.littlepiggy.enemy.Farmer;
import com.niko.littlepiggy.item.Apple;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.*;

public class GameContactListener implements ContactListener {

    private final Player player;

    public GameContactListener(Player player) {
        this.player = player;
    }

    @Override
    public void beginContact(Contact contact) {
        Fixture a = contact.getFixtureA();
        Fixture b = contact.getFixtureB();

        checkProjectileContact(a, b);

        String aName = a.getUserData() != null ? a.getUserData().toString() : "NULL";
        String bName = b.getUserData() != null ? b.getUserData().toString() : "NULL";

        checkGroundContact(contact, a, b, true);
        checkFarmerRange(a, b, true);
        checkAppleContact(a, b);
        checkGoalContact(a, b);
    }

    @Override
    public void endContact(Contact contact) {
        Fixture a = contact.getFixtureA();
        Fixture b = contact.getFixtureB();

        String aName = a.getUserData() != null ? a.getUserData().toString() : "NULL";
        String bName = b.getUserData() != null ? b.getUserData().toString() : "NULL";

        checkGroundContact(contact, a, b, false);
        checkFarmerRange(a, b, false);
    }

    private void checkGroundContact(Contact contact, Fixture a, Fixture b, boolean begin) {
        String aName = a.getUserData() != null ? a.getUserData().toString() : "";
        String bName = b.getUserData() != null ? b.getUserData().toString() : "";

        boolean footOnGround = (aName.equals("foot") && bName.equals("ground")) ||
                (bName.equals("foot") && aName.equals("ground"));

        if (!footOnGround) {
            return;
        }

        if (begin) {
            player.beginGroundContact();
        } else {
            player.endGroundContact();
        }
    }

    private void checkGoalContact(
            Fixture a,
            Fixture b) {

        if (a.getUserData() instanceof Goal
                && b.getBody().getUserData() instanceof Player) {

            Goal goal = (Goal) a.getUserData();

            if (!goal.isReached()) {
                goal.reach();
                System.out.println("GOAL REACHED!");
            }
        }

        if (b.getUserData() instanceof Goal
                && a.getBody().getUserData() instanceof Player) {

            Goal goal = (Goal) b.getUserData();

            if (!goal.isReached()) {
                goal.reach();
            }
        }
    }

    private void checkAppleContact(Fixture a, Fixture b) {

        if (a.getUserData() instanceof Apple
                && b.getBody().getUserData() instanceof Player) {

            collectApple((Apple) a.getUserData());
        }

        if (b.getUserData() instanceof Apple
                && a.getBody().getUserData() instanceof Player) {

            collectApple((Apple) b.getUserData());
        }
    }

    private void collectApple(Apple apple) {

        if (apple.isCollected()) {
            return;
        }

        apple.collect();
        player.heal(10f);
    }

    private void checkProjectileContact(
            Fixture a,
            Fixture b) {

        Projectile projectile = null;
        Fixture other = null;

        if (a.getUserData() instanceof Projectile) {

            projectile = (Projectile) a.getUserData();

            other = b;

        } else if (b.getUserData() instanceof Projectile) {

            projectile = (Projectile) b.getUserData();

            other = a;
        }

        if (projectile == null) {
            return;
        }

        /*
         * Projektilen har redan träffat något
         * denna physics-step.
         */
        if (projectile.shouldRemove()) {
            return;
        }

        // Terrain
        if ("ground".equals(
                other.getUserData())) {

            projectile.markForRemoval();
            return;
        }

        // Något som kan ta damage
        Object target = other.getBody().getUserData();

        if (target instanceof Damageable) {
            Damageable damageable = (Damageable) target;

            damageable.takeDamage(
                    projectile.getDamage());

            Vector2 knockback = projectile.getKnockbackImpulse();

            damageable.applyKnockback(
                    knockback.x,
                    knockback.y);

            projectile.markForRemoval();
        }
    }

    private void checkFarmerRange(
            Fixture a,
            Fixture b,
            boolean entered) {

        Farmer farmer = null;

        // A är Farmer range-sensor, B tillhör Player
        if (a.isSensor()
                && a.getUserData() instanceof Farmer
                && b.getBody().getUserData() instanceof Player) {

            farmer = (Farmer) a.getUserData();
        }

        // B är Farmer range-sensor, A tillhör Player
        else if (b.isSensor()
                && b.getUserData() instanceof Farmer
                && a.getBody().getUserData() instanceof Player) {

            farmer = (Farmer) b.getUserData();
        }

        if (farmer == null) {
            return;
        }

        if (entered) {
            farmer.playerEnteredRange();
        } else {
            farmer.playerExitedRange();
        }
    }

    @Override
    public void preSolve(Contact contact, Manifold oldManifold) {
    }

    @Override
    public void postSolve(Contact contact, ContactImpulse impulse) {
    }
}
