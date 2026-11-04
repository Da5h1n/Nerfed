package uk.da5h1n.nerfed;

import net.fabricmc.api.ModInitializer;

import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.entity.event.v1.ServerEntityLevelChangeEvents;
import net.fabricmc.fabric.api.entity.event.v1.ServerPlayerEvents;
import net.fabricmc.fabric.api.networking.v1.EntityTrackingEvents;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.resources.Identifier;

import net.minecraft.server.level.ServerPlayer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.da5h1n.nerfed.command.DisabilityCommand;
import uk.da5h1n.nerfed.event.LimbInteractionHandler;
import uk.da5h1n.nerfed.network.DisabilitySync;
import uk.da5h1n.nerfed.network.DisabilitySyncPayload;

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

		LimbInteractionHandler.register();

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
