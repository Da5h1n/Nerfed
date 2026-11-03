package uk.da5h1n.nerfed;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import uk.da5h1n.nerfed.network.LimbSyncPayload;
import uk.da5h1n.nerfed.util.LimbDataHelper;

public class NerfedClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        ClientPlayNetworking.registerGlobalReceiver(LimbSyncPayload.TYPE, (payload, context) -> {
            context.client().execute(() -> {
                Entity entity = context.player().level().getEntity(payload.entityId());
                if (entity instanceof Player player) {
                    LimbDataHelper.LimbData data = player.getAttachedOrCreate(LimbDataHelper.LIMB_DATA, () -> new LimbDataHelper.LimbData(false, false));
                    data.setLeftDisabled(payload.leftDisabled());
                    data.setRightDisabled(payload.rightDisabled());
                    player.setAttached(LimbDataHelper.LIMB_DATA, data);
                }
            });
        });
    }
}