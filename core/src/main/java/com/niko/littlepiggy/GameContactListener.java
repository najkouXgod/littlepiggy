package com.niko.littlepiggy;

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

        String aName = a.getUserData() != null ? a.getUserData().toString() : "NULL";
        String bName = b.getUserData() != null ? b.getUserData().toString() : "NULL";

        System.out.println("beginContact: [" + aName + "] <-> [" + bName + "]");

        checkGroundContact(contact, a, b, true);
        checkAppleContact(a, b);
    }

    @Override
    public void endContact(Contact contact) {
        Fixture a = contact.getFixtureA();
        Fixture b = contact.getFixtureB();

        String aName = a.getUserData() != null ? a.getUserData().toString() : "NULL";
        String bName = b.getUserData() != null ? b.getUserData().toString() : "NULL";

        System.out.println("endContact: [" + aName + "] <-> [" + bName + "]");

        checkGroundContact(contact, a, b, false);
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

    private void checkAppleContact(Fixture a, Fixture b) {

        if (a.getUserData() instanceof Apple) {
            Apple apple = (Apple) a.getUserData();
            apple.collect();
        }

        if (b.getUserData() instanceof Apple) {
            Apple apple = (Apple) b.getUserData();
            apple.collect();
        }
    }

    @Override
    public void preSolve(Contact contact, Manifold oldManifold) {
    }

    @Override
    public void postSolve(Contact contact, ContactImpulse impulse) {
    }
}
