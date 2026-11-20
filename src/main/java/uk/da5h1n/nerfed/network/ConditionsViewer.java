package uk.da5h1n.nerfed.network;

import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.server.level.ServerPlayer;
import uk.da5h1n.nerfed.util.DisabilityDataHelper;

public class ConditionsViewer {

    public static void open(ServerPlayer viewer, ServerPlayer target) {
        DisabilityDataHelper.DisabilityData data = DisabilityDataHelper.getData(target);
        ServerPlayNetworking.send(viewer, new OpenConditionsScreenPayload(
                target.getName().getString(),
                viewer.getUUID().equals(target.getUUID()),
                data.activeLevels()
        ));
    }
}
