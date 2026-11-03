package uk.da5h1n.nerfed;

import net.fabricmc.api.ModInitializer;

import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.resources.Identifier;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.da5h1n.nerfed.command.DisableLimbCommand;
import uk.da5h1n.nerfed.event.LimbInteractionHandler;
import uk.da5h1n.nerfed.network.LimbSyncPayload;

public class Nerfed implements ModInitializer {
	public static final String MOD_ID = "nerfed";

	// This logger is used to write text to the console and the log file.
	// It is considered best practice to use your mod id as the logger's name.
	// That way, it's clear which mod wrote info, warnings, and errors.
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	@Override
	public void onInitialize() {
		LOGGER.info("Initialising Nerfed");

		PayloadTypeRegistry.clientboundPlay().register(LimbSyncPayload.TYPE, LimbSyncPayload.STREAM_CODEC);

		LimbInteractionHandler.register();

		CommandRegistrationCallback.EVENT.register(((dispatcher, buildContext, selection) -> {
			DisableLimbCommand.register(dispatcher);
		}));
	}

	public static Identifier id(String path) {
		return Identifier.fromNamespaceAndPath(MOD_ID, path);
	}
}
