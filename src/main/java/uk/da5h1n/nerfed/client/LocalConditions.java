package uk.da5h1n.nerfed.client;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Player;
import uk.da5h1n.nerfed.util.DisabilityDataHelper;

/**
 * Conditions chosen with the client-side command, for servers that don't have Nerfed installed.
 *
 * Kept in memory only: they survive server changes, dimension changes and respawns,
 * and are gone the next time the game is launched.
 *
 * If the server has Nerfed, the server's data wins and these are paused (not deleted).
 * Everything here runs on the client thread.
 */
public final class LocalConditions {

    /** Ticks to wait after joining before applying local data, so a Nerfed server can identify itself first. */
    private static final int JOIN_GRACE_TICKS = 20;

    private static final DisabilityDataHelper.DisabilityData DATA = new DisabilityDataHelper.DisabilityData();

    private static boolean serverHasNerfed = false;
    private static int ticksSinceJoin = 0;

    private static int version = 0;
    private static int appliedVersion = -1;
    private static Player appliedPlayer = null;

    private static Runnable pendingAction = null;

    private LocalConditions() {}

    public static void register() {
        ClientPlayConnectionEvents.JOIN.register((handler, sender, client) -> reset());
        ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> reset());
        ClientTickEvents.END_CLIENT_TICK.register(LocalConditions::tick);
    }

    private static void reset() {
        serverHasNerfed = false;
        ticksSinceJoin = 0;
        appliedPlayer = null;
    }

    /** Called when a Nerfed server sends us anything, which proves it has the mod. */
    public static void onServerSync() {
        serverHasNerfed = true;
    }

    public static boolean serverHasNerfed() {
        return serverHasNerfed;
    }

    public static DisabilityDataHelper.DisabilityData data() {
        return DATA;
    }

    /** Call after changing {@link #data()} so it gets re-applied to the player. */
    public static void markChanged() {
        version++;
    }

    /**
     * Runs something on the next client tick. Opening a screen straight from a command would be
     * closed again by the chat screen that is still finishing, so screens are opened this way.
     */
    public static void runNextTick(Runnable action) {
        pendingAction = action;
    }

    private static void tick(Minecraft client) {
        if (pendingAction != null) {
            Runnable action = pendingAction;
            pendingAction = null;
            action.run();
        }

        Player player = client.player;
        if (player == null) return;

        ticksSinceJoin++;
        if (serverHasNerfed || ticksSinceJoin < JOIN_GRACE_TICKS) return;

        // Re-apply when the data changed or the player entity is new (join, respawn, dimension change)
        if (player != appliedPlayer || version != appliedVersion) {
            DisabilityDataHelper.DisabilityData target = DisabilityDataHelper.getData(player);
            target.applyFrom(DATA.levelsById(), DATA.getColorblindMode(), DATA.getColorblindLevel());
            player.setAttached(DisabilityDataHelper.DISABILITY_DATA, target);
            appliedPlayer = player;
            appliedVersion = version;
        }
    }
}