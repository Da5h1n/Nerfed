package uk.da5h1n.nerfed.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import uk.da5h1n.nerfed.util.DisabilityDataHelper;

import java.util.*;

public class ProsopagnosiaHandler {

    private static final long SWAP_INTERVAL_TICKS = 200L; // 10 seconds

    // Cache to maintain stable assignments during the same time bucket
    private static long cachedTimeBucket = -1;
    private static final Map<Integer, IdentityOverride> cachedOverrides = new HashMap<>();

    public static class IdentityOverride {
        public final Identifier skin;
        public final Component displayName;

        public IdentityOverride(Identifier skin, Component displayName) {
            this.skin = skin;
            this.displayName = displayName;
        }
    }

    public static IdentityOverride getOverrideForEntity(int entityId) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.level == null) return null;

        // Only scramble if local player has Prosopagnosia
        if (!DisabilityDataHelper.isProsopagnosia(mc.player)) return null;

        // Don't scramble the local player's own identity
        if (entityId == mc.player.getId()) return null;

        long timeBucket = mc.level.getGameTime() / SWAP_INTERVAL_TICKS;

        // Recompute the swap mapping if the 10-second time bucket has advanced
        if (timeBucket != cachedTimeBucket) {
            recomputeSwaps(mc, timeBucket);
        }

        return cachedOverrides.get(entityId);
    }

    private static void recomputeSwaps(Minecraft mc, long timeBucket) {
        cachedTimeBucket = timeBucket;
        cachedOverrides.clear();

        List<AbstractClientPlayer> players = new ArrayList<>();
        for (AbstractClientPlayer p : mc.level.players()) {
            // Exclude local player from the swap pool
            if (p == mc.player) continue;
            if (p.getSkin() != null && p.getSkin().body() != null) {
                players.add(p);
            }
        }

        int n = players.size();
        if (n < 2) {
            // Need at least 2 other players to perform a swap
            return;
        }

        // Sort by entity ID to ensure deterministic ordering across clients
        players.sort(Comparator.comparingInt(AbstractClientPlayer::getId));

        // Create a shuffled copy using the time bucket as a stable random seed
        List<AbstractClientPlayer> shuffled = new ArrayList<>(players);
        Collections.shuffle(shuffled, new Random(timeBucket));

        // Create a strict derangement by shifting by 1:
        // Player at index i takes the identity of shuffled player at (i + 1) % n.
        // This guarantees nobody ever gets assigned their own skin/name!
        for (int i = 0; i < n; i++) {
            AbstractClientPlayer source = players.get(i);
            AbstractClientPlayer target = shuffled.get((i + 1) % n);

            Identifier skin = target.getSkin().body().texturePath();
            Component name = target.getDisplayName();

            if (skin != null && name != null) {
                cachedOverrides.put(source.getId(), new IdentityOverride(skin, name));
            }
        }
    }
}