package com.niko.littlepiggy.player;

/**
 * UserData för spelarens attack-sensorer.
 * ContactListenern kan därmed skilja dash från bakåtvolt utan
 * att själva Box2D-fixturen behöver känna till attacklogiken.
 */
public class PlayerAttackHitbox {

    public enum Type {
        DASH,
        BACKFLIP
    }

    private final PlayerCombat combat;
    private final Type type;

    public PlayerAttackHitbox(
            PlayerCombat combat,
            Type type) {

        this.combat = combat;
        this.type = type;
    }

    public PlayerCombat getCombat() {
        return combat;
    }

    public Type getType() {
        return type;
    }
}
