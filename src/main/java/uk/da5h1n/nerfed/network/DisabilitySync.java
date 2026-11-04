package uk.da5h1n.nerfed.network;

import net.fabricmc.fabric.api.networking.v1.PlayerLookup;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.server.level.ServerPlayer;
import uk.da5h1n.nerfed.util.DisabilityDataHelper;

public class DisabilitySync {

    public static void sendTo(ServerPlayer recipient, ServerPlayer subject) {
        DisabilityDataHelper.DisabilityData data = DisabilityDataHelper.getData(subject);
        ServerPlayNetworking.send(recipient, new DisabilitySyncPayload(
                subject.getId(),
                data.isLeftArmDisabled(),
                data.isRightArmDisabled(),
                data.isLeftLegDisabled(),
                data.isRightLegDisabled(),
                data.getColorblindMode(),
                data.isTunnelVision(),
                data.isMyopia(),
                data.isTremors()
        ));
    }

    public static void syncAll(ServerPlayer player) {
        sendTo(player, player);
        for (ServerPlayer tracker : PlayerLookup.tracking(player)) {
            sendTo(tracker, player);
        }
    }
}
