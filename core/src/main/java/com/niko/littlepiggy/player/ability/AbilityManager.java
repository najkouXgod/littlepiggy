package com.niko.littlepiggy.player.ability;

import com.badlogic.gdx.utils.Array;

import com.niko.littlepiggy.player.animation.PlayerAnimationState;
import com.niko.littlepiggy.player.event.PlayerEvent;

/**
 * Äger spelarens abilities och koordinerar dem, så att Player inte
 * behöver "if ground slam ... if backflip ... if dash ..." för varje ny
 * ability.
 *
 * Ordningen i listan är PRIORITETEN, och gäller både för
 * - vem som får försöka starta först under en frame, och
 * - vilken animation som vinner när flera abilities har en pose.
 *
 * Regler (generella, inga ability-specifika undantag):
 * - En ability kan inte startas medan någon ability blockerar movement.
 * - När en ability startar får alla andra veta det (onOtherAbilityStarted).
 *
 * Lägga till en ny ability (t.ex. Headbutt):
 * 1. Skriv HeadbuttAbility implements PlayerAbility.
 * 2. Skapa den och lägg till den i listan här.
 * Player, PlayerPhysics, PlayerCombat och PlayerAnimator påverkas inte
 * (animatorn behöver bara en sprite om abilityn har en ny pose).
 */
public class AbilityManager {

    private final Array<PlayerAbility> abilities = new Array<>(false, 4);

    private final GroundSlamAbility groundSlam;
    private final BackflipAbility backflip;
    private final DashAbility dash;

    public AbilityManager(AbilityContext ctx) {

        groundSlam = new GroundSlamAbility(ctx);
        backflip = new BackflipAbility(ctx);
        dash = new DashAbility(ctx);

        // Prioritetsordning: högst först.
        abilities.add(groundSlam);
        abilities.add(backflip);
        abilities.add(dash);
    }

    public void update(float delta) {

        // 1. Tick alla (cooldowns, state machines).
        for (int i = 0; i < abilities.size; i++) {
            abilities.get(i).update(delta);
        }

        // 2. Försök starta i prioritetsordning.
        for (int i = 0; i < abilities.size; i++) {
            tryStart(abilities.get(i));
        }
    }

    private void tryStart(PlayerAbility ability) {

        if (!ability.isTriggered() || !ability.canStart()) {
            return;
        }

        // Någon annan ability har redan tagit över kroppen.
        if (blocksMovement()) {
            return;
        }

        ability.start();

        for (int i = 0; i < abilities.size; i++) {

            PlayerAbility other = abilities.get(i);

            if (other != ability) {
                other.onOtherAbilityStarted(ability);
            }
        }
    }

    public void onPlayerEvent(PlayerEvent event) {

        for (int i = 0; i < abilities.size; i++) {
            abilities.get(i).onPlayerEvent(event);
        }
    }

    public boolean blocksMovement() {

        for (int i = 0; i < abilities.size; i++) {

            if (abilities.get(i).blocksMovement()) {
                return true;
            }
        }

        return false;
    }

    public boolean isImmuneToContactDamage() {

        for (int i = 0; i < abilities.size; i++) {

            if (abilities.get(i).isImmuneToContactDamage()) {
                return true;
            }
        }

        return false;
    }

    /**
     * Animationen från den högst prioriterade ability som har en pose,
     * eller null om ingen har det (då avgör PlayerMovement).
     */
    public PlayerAnimationState getAnimationState() {

        for (int i = 0; i < abilities.size; i++) {

            PlayerAnimationState state = abilities.get(i).getAnimationState();

            if (state != null) {
                return state;
            }
        }

        return null;
    }

    /*
     * ------------------------------------------------------------
     * Åtkomst för HUD via Player
     * ------------------------------------------------------------
     */

    public DashAbility dash() {
        return dash;
    }

    public BackflipAbility backflip() {
        return backflip;
    }

    public GroundSlamAbility groundSlam() {
        return groundSlam;
    }
}
