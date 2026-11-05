package uk.da5h1n.nerfed.client;

import net.minecraft.client.Minecraft;
import net.minecraft.util.RandomSource;
import uk.da5h1n.nerfed.util.DisabilityDataHelper;

public class AdhdHandler {

    private static final RandomSource RANDOM = RandomSource.create();
    private static int ticksUntilNextSwitch = 100;

    public static void clientTick(Minecraft client) {
        if (client.player == null || client.level == null) return;

        if (DisabilityDataHelper.isAdhd(client.player)) {
            ticksUntilNextSwitch--;
            if (ticksUntilNextSwitch <= 0) {
                ticksUntilNextSwitch = 100 + RANDOM.nextInt(200);

                int currentSlot = client.player.getInventory().getSelectedSlot();
                int newSlot;
                do {
                    newSlot = RANDOM.nextInt(9);
                } while (newSlot == currentSlot);

                client.player.getInventory().setSelectedSlot(newSlot);
            }
        }
    }
}
