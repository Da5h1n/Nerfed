package uk.da5h1n.nerfed;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import uk.da5h1n.nerfed.client.AdhdHandler;
import uk.da5h1n.nerfed.client.SchizophreniaHandler;
import uk.da5h1n.nerfed.client.TremorsHandler;
import uk.da5h1n.nerfed.network.DisabilitySyncPayload;
import uk.da5h1n.nerfed.util.DisabilityDataHelper;

public class NerfedClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        ClientPlayNetworking.registerGlobalReceiver(DisabilitySyncPayload.TYPE, (payload, context) -> {
            context.client().execute(() -> {
                Player self = context.player();
                Entity entity = payload.entityId() == self.getId()
                        ? self
                        : self.level().getEntity(payload.entityId());

                if (entity instanceof Player player) {
                    DisabilityDataHelper.DisabilityData data = DisabilityDataHelper.getData(player);

                    data.setLeftArmDisabled(payload.leftArmDisabled());
                    data.setRightArmDisabled(payload.rightArmDisabled());
                    data.setLeftLegDisabled(payload.leftLegDisabled());
                    data.setRightLegDisabled(payload.rightLegDisabled());
                    data.setColorblindMode(payload.colorblindMode());
                    data.setTunnelVision(payload.tunnelVision());
                    data.setMyopia(payload.myopia());
                    data.setTremors(payload.tremors());
                    data.setAdhd(payload.adhd());
                    data.setVertigo(payload.vertigo());
                    data.setScoliosis(payload.scoliosis());
                    data.setProsopagnosia(payload.prosopagnosia());
                    data.setSchizophrenia(payload.schizophrenia());

                    player.setAttached(DisabilityDataHelper.DISABILITY_DATA, data);
                }
            });
        });


        ClientTickEvents.END_CLIENT_TICK.register(TremorsHandler::clientTick);
        ClientTickEvents.END_CLIENT_TICK.register(AdhdHandler::clientTick);
        ClientTickEvents.END_CLIENT_TICK.register(SchizophreniaHandler::clientTick);
    }
}