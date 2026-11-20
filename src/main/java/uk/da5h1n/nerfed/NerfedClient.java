package uk.da5h1n.nerfed;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import uk.da5h1n.nerfed.client.*;
import uk.da5h1n.nerfed.item.WalkingStickItem;
import uk.da5h1n.nerfed.network.DisabilitySyncPayload;
import uk.da5h1n.nerfed.network.OpenConditionsScreenPayload;
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

                    data.applyFrom(payload.levels(), payload.colorblindMode(), payload.colorblindLevel());
                    player.setAttached(DisabilityDataHelper.DISABILITY_DATA, data);

                }
            });
        });

        ClientPlayNetworking.registerGlobalReceiver(OpenConditionsScreenPayload.TYPE, ((payload, context) ->
                context.client().execute(() ->
                        context.client().gui.setScreen(
                                new ConditionsScreen(payload.playerName(), payload.self(), payload.conditions())))));

        WalkingStickItem.clientTap = Echolocation::tap;
        ClientTickEvents.END_CLIENT_TICK.register(Echolocation::tick);
        ClientTickEvents.END_CLIENT_TICK.register(TremorsHandler::clientTick);
        ClientTickEvents.END_CLIENT_TICK.register(AdhdHandler::clientTick);
        ClientTickEvents.END_CLIENT_TICK.register(SchizophreniaHandler::clientTick);
    }
}