package uk.da5h1n.nerfed.util;

import net.minecraft.world.entity.player.Player;

public class Strength {

    private static final float[] BY_LEVEL = {0f, 0.4f, 0.7f, 1.0f};

    private Strength() {}

    public static float ofLevel(int level) {
        return BY_LEVEL[Math.max(0, Math.min(level, 3))];
    }

    public static float of(Player player, Condition condition) {
        return ofLevel(DisabilityDataHelper.getLevel(player, condition));
    }
}
