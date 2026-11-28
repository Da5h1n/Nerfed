package uk.da5h1n.nerfed;

import net.fabricmc.api.ModInitializer;

import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.entity.event.v1.ServerEntityLevelChangeEvents;
import net.fabricmc.fabric.api.entity.event.v1.ServerPlayerEvents;
import net.fabricmc.fabric.api.networking.v1.EntityTrackingEvents;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.resources.Identifier;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.chat.Component;
import uk.da5h1n.nerfed.network.ConditionsViewer;
import uk.da5h1n.nerfed.network.DisabilitySync;
import uk.da5h1n.nerfed.network.RequestLocalConditionsScreenPayload;
import uk.da5h1n.nerfed.network.SetLocalColourblindPayload;
import uk.da5h1n.nerfed.network.SetLocalDisabilityPayload;
import uk.da5h1n.nerfed.util.Condition;
import uk.da5h1n.nerfed.util.DisabilityDataHelper;

import net.minecraft.server.level.ServerPlayer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.da5h1n.nerfed.command.DisabilityCommand;
import uk.da5h1n.nerfed.event.LimbInteractionHandler;
import uk.da5h1n.nerfed.event.MuteHandler;
import uk.da5h1n.nerfed.item.ModItems;
import uk.da5h1n.nerfed.network.DisabilitySyncPayload;
import uk.da5h1n.nerfed.network.OpenConditionsScreenPayload;

public class Nerfed implements ModInitializer {
	public static final String MOD_ID = "nerfed";

	// This logger is used to write text to the console and the log file.
	// It is considered best practice to use your mod id as the logger's name.
	// That way, it's clear which mod wrote info, warnings, and errors.
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	@Override
	public void onInitialize() {
		LOGGER.info("Initialising Nerfed");

		PayloadTypeRegistry.clientboundPlay().register(DisabilitySyncPayload.TYPE, DisabilitySyncPayload.STREAM_CODEC);
		PayloadTypeRegistry.clientboundPlay().register(OpenConditionsScreenPayload.TYPE, OpenConditionsScreenPayload.STREAM_CODEC);

		PayloadTypeRegistry.serverboundPlay().register(SetLocalDisabilityPayload.TYPE, SetLocalDisabilityPayload.STREAM_CODEC);
		PayloadTypeRegistry.serverboundPlay().register(SetLocalColourblindPayload.TYPE, SetLocalColourblindPayload.STREAM_CODEC);
		PayloadTypeRegistry.serverboundPlay().register(RequestLocalConditionsScreenPayload.TYPE, RequestLocalConditionsScreenPayload.STREAM_CODEC);

		ServerPlayNetworking.registerGlobalReceiver(SetLocalDisabilityPayload.TYPE, (payload, context) -> {
			context.server().execute(() -> {
				ServerPlayer player = context.player();
				Condition condition = Condition.byId(payload.conditionId());
				if (condition == null) return;

				int clamped = Math.clamp(payload.level(), 0, condition.maxLevel());
				DisabilityDataHelper.DisabilityData data = DisabilityDataHelper.getData(player);
				data.setLevel(condition, clamped);
				player.setAttached(DisabilityDataHelper.DISABILITY_DATA, data);
				DisabilitySync.syncAll(player);
				player.sendSystemMessage(Component.literal("§c[Nerfed] §7" + condition.id()
						+ (clamped == 0 ? " is now off" : " is now level " + clamped)));
			});
		});

		ServerPlayNetworking.registerGlobalReceiver(SetLocalColourblindPayload.TYPE, (payload, context) -> {
			context.server().execute(() -> {
				ServerPlayer player = context.player();
				String mode = payload.mode().toLowerCase(java.util.Locale.ROOT);
				boolean off = mode.equals("none");
				if (!off && !java.util.Set.of("protan", "deuteran", "tritan", "mono").contains(mode)) return;

				int level = Math.clamp(payload.level(), 0, Condition.COLOURBLIND_MAX_LEVEL);
				DisabilityDataHelper.DisabilityData data = DisabilityDataHelper.getData(player);

				if (off || (level == 0 && mode.equals(data.getColorblindMode()))) {
					data.setColorblind("none", 0);
				} else if (level > 0) {
					data.setColorblind(mode, level);
				} else {
					player.sendSystemMessage(Component.literal("§e[Nerfed] Colourblind mode is '"
							+ data.getColorblindMode() + "', not '" + mode + "'. Nothing changed."));
					return;
				}

				player.setAttached(DisabilityDataHelper.DISABILITY_DATA, data);
				DisabilitySync.syncAll(player);
				player.sendSystemMessage(Component.literal("§c[Nerfed] §7Colourblindness: "
						+ data.getColorblindMode() + " (level " + data.getColorblindLevel() + ")"));
			});
		});

		ServerPlayNetworking.registerGlobalReceiver(RequestLocalConditionsScreenPayload.TYPE, (payload, context) -> {
			context.server().execute(() -> ConditionsViewer.open(context.player(), context.player()));
		});

		ModItems.init();

		LimbInteractionHandler.register();
		MuteHandler.register();

		// In your ModInitializer / Command registration event:
		CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
			DisabilityCommand.register(dispatcher);
		});


		ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
			DisabilitySync.syncAll(handler.getPlayer());
		});

		ServerPlayerEvents.AFTER_RESPAWN.register((oldPlayer, newPlayer, alive) -> {
			DisabilitySync.syncAll(newPlayer);
		});

		ServerEntityLevelChangeEvents.AFTER_PLAYER_CHANGE_LEVEL.register((player, origin, destination) -> {
			DisabilitySync.syncAll(player);
		});

		EntityTrackingEvents.START_TRACKING.register((tracked, viewer) -> {
			if (tracked instanceof ServerPlayer other) DisabilitySync.sendTo(viewer, other);
		});
	}

	public static Identifier id(String path) {
		return Identifier.fromNamespaceAndPath(MOD_ID, path);
	}
}



/*


 TODO:

 ADD WAYS TO MITAGATE / remove NERFS

 ADD MORE NERFS

 FEEL IT NEEDS MORE "INTERACTION"



Foundations

[ ] Add flags to each Condition (photosensitivity risk, motion risk)
[ ] Client settings model and a JSON config file
[ ] Per-effect render mode: Full / Gentle / Off

Settings menu

[ ] NerfedSettingsScreen
[ ] Keybind to open it
[ ] Pause menu / Options button
[ ] Mod Menu entry (optional)
[ ] First-run photosensitivity prompt

Accessibility

[ ] Photosensitivity safe-mode master switch
[ ] “Don’t give me this effect” list, synced to the server

Random system

[ ] Timed effects (store expiry times, migrate saved data)
[ ] Random effect picker with a timer
[ ] Max effects at once (global + per-player)
[ ] Commands: enable/disable, interval, max, duration, roll

New effects

[ ] Dyslexia (jumbled words)
[ ] Vision variants (central blind spot, half-field loss, dark blotches)
[ ] Extras to consider: tinnitus, tremor, dizziness

Other

[ ] Update the Modrinth disclosures if new effects flash or flicker
[ ] Update the README and description as features land

 */