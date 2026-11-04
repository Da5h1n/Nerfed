package uk.da5h1n.nerfed;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import uk.da5h1n.nerfed.client.ColourblindRenderHandler;
import uk.da5h1n.nerfed.network.DisabilitySyncPayload;
import uk.da5h1n.nerfed.util.DisabilityDataHelper;

public class NerfedClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        ClientPlayNetworking.registerGlobalReceiver(DisabilitySyncPayload.TYPE, (payload, context) -> {
            context.client().execute(() -> {
                Entity entity = context.player().level().getEntity(payload.entityId());
                if (entity instanceof Player player) {
                    DisabilityDataHelper.DisabilityData data = player.getAttachedOrCreate(
                            DisabilityDataHelper.DISABILITY_DATA,
                            () -> new DisabilityDataHelper.DisabilityData(false, false, false, false, "none", false, false, false)
                    );

                    // Update arms
                    data.setLeftArmDisabled(payload.leftArmDisabled());
                    data.setRightArmDisabled(payload.rightArmDisabled());

                    // Update legs
                    data.setLeftLegDisabled(payload.leftLegDisabled());
                    data.setRightLegDisabled(payload.rightLegDisabled());

                    // Update sensory & neurological conditions
                    data.setColorblindMode(payload.colorblindMode());
                    data.setTunnelVision(payload.tunnelVision());
                    data.setMyopia(payload.myopia());
                    data.setTremors(payload.tremors());

                    player.setAttached(DisabilityDataHelper.DISABILITY_DATA, data);
                }
            });
        });
    }
}