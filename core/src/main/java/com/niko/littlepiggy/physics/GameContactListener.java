package com.niko.littlepiggy.physics;

import com.niko.littlepiggy.player.PlayerAttackHitbox;
import com.niko.littlepiggy.combat.Damageable;
import com.niko.littlepiggy.level.Goal;
import com.niko.littlepiggy.projectile.Projectile;
import com.niko.littlepiggy.player.Player;
import com.niko.littlepiggy.enemy.Farmer;
import com.niko.littlepiggy.enemy.Dog;
import com.niko.littlepiggy.item.Apple;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.*;

public class GameContactListener implements ContactListener {

    private static final float DOG_CONTACT_DAMAGE = 15f;
    private static final float DOG_CONTACT_KNOCKBACK_X = 4f;
    private static final float DOG_CONTACT_KNOCKBACK_Y = 2f;

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

        checkAttackContact(a, b);
        checkGroundContact(contact, a, b, true);
        checkFarmerRange(a, b, true);
        checkDogRange(a, b, true);
        checkDogContact(a, b);
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
        checkDogRange(a, b, false);
    }

    private void checkAttackContact(
            Fixture a,
            Fixture b) {

        if (a.getUserData() instanceof PlayerAttackHitbox) {
            applyAttackHit(
                    (PlayerAttackHitbox) a.getUserData(),
                    b);
        }

        if (b.getUserData() instanceof PlayerAttackHitbox) {
            applyAttackHit(
                    (PlayerAttackHitbox) b.getUserData(),
                    a);
        }
    }

    private void applyAttackHit(
            PlayerAttackHitbox attackHitbox,
            Fixture targetFixture) {

        /*
         * Farmer har en stor range-sensor. Vi vill träffa
         * Farmers kropp, inte range-sensorn.
         */
        if (targetFixture.isSensor()) {
            return;
        }

        Object target = targetFixture
                .getBody()
                .getUserData();

        if (target instanceof Damageable) {
            Damageable damageable = (Damageable) target;

            attackHitbox.getCombat().hit(
                    attackHitbox.getType(),
                    damageable);
        }
    }

    private void checkGroundContact(Contact contact, Fixture a, Fixture b, boolean begin) {
        boolean aIsFoot = "foot".equals(a.getUserData());
        boolean bIsFoot = "foot".equals(b.getUserData());

        boolean aIsGroundSurface = isGroundSurface(a);
        boolean bIsGroundSurface = isGroundSurface(b);

        boolean footOnGround = (aIsFoot && bIsGroundSurface)
                || (bIsFoot && aIsGroundSurface);

        if (!footOnGround) {
            return;
        }

        if (begin) {
            player.beginGroundContact();
        } else {
            player.endGroundContact();
        }
    }

    /**
     * Player får hoppa både från terräng och från ovansidan av en Farmer.
     * Farmerns stora range-sensor räknas däremot aldrig som mark.
     */
    private boolean isGroundSurface(Fixture fixture) {

        if ("ground".equals(fixture.getUserData())) {
            return true;
        }

        if (fixture.isSensor()
                || !(fixture.getBody().getUserData() instanceof Farmer)) {
            return false;
        }

        /*
         * Foot-sensorn kan även nudda sidan av en Farmer. Det ska inte
         * ge "grounded". Farmer måste faktiskt ligga tydligt under Player.
         */
        return fixture.getBody().getPosition().y < player.getY() - 0.25f;
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

        if (target == projectile.getOwner()) {
            return;
        }

        /*
         * Farmers hagel ska aldrig skada andra Farmers. Projektilen får
         * fortsätta genom dem så en Farmer framför skytten inte fungerar
         * som en osynlig skottsköld för Player.
         */
        if (projectile.getOwner() instanceof Farmer
                && target instanceof Farmer) {
            return;
        }

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

    private void checkDogRange(
            Fixture a,
            Fixture b,
            boolean entered) {

        Dog dog = null;

        if (a.isSensor()
                && a.getUserData() instanceof Dog
                && b.getBody().getUserData() instanceof Player) {

            dog = (Dog) a.getUserData();

        } else if (b.isSensor()
                && b.getUserData() instanceof Dog
                && a.getBody().getUserData() instanceof Player) {

            dog = (Dog) b.getUserData();
        }

        if (dog == null) {
            return;
        }

        if (entered) {
            dog.playerEnteredRange();
        } else {
            dog.playerExitedRange();
        }
    }

    /**
     * Dog skadar Player direkt via kroppskontakt, men bara under
     * själva lunge-anfallet - annars skulle det räcka att gå emot
     * en Dog av misstag för att ta skada.
     */
    private void checkDogContact(Fixture a, Fixture b) {

        Dog dog = null;
        Fixture playerFixture = null;

        if (!a.isSensor()
                && a.getBody().getUserData() instanceof Dog
                && !b.isSensor()
                && b.getBody().getUserData() instanceof Player) {

            dog = (Dog) a.getBody().getUserData();
            playerFixture = b;

        } else if (!b.isSensor()
                && b.getBody().getUserData() instanceof Dog
                && !a.isSensor()
                && a.getBody().getUserData() instanceof Player) {

            dog = (Dog) b.getBody().getUserData();
            playerFixture = a;
        }

        if (dog == null || !dog.isLungeHitAvailable()) {
            return;
        }

        dog.consumeLungeHit();

        player.takeDamage(DOG_CONTACT_DAMAGE);

        float knockbackDirection = Math.signum(
                player.getX() - dog.getPosition().x);

        player.applyKnockback(
                knockbackDirection * DOG_CONTACT_KNOCKBACK_X,
                DOG_CONTACT_KNOCKBACK_Y);
    }

    @Override
    public void preSolve(Contact contact, Manifold oldManifold) {
    }

    @Override
    public void postSolve(Contact contact, ContactImpulse impulse) {
    }
}
