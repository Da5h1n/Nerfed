package uk.da5h1n.nerfed.client;

import net.minecraft.client.Minecraft;
import net.minecraft.util.RandomSource;
import uk.da5h1n.nerfed.util.DisabilityDataHelper;

public class TremorsHandler {

    private static final RandomSource RANDOM = RandomSource.create();

    public static void clientTick(Minecraft client) {
        if (client.player == null || client.level == null) return;

        if (DisabilityDataHelper.isTremors(client.player)) {
            float intensity = 0.15f;
            float yawJitter = (RANDOM.nextFloat() - 0.5f) * intensity;
            float pitchJitter = (RANDOM.nextFloat() - 0.5f) * intensity;

            client.player.setYRot(client.player.getYRot() + yawJitter);
            client.player.setXRot(client.player.getXRot() + pitchJitter);
        }
    }
}
