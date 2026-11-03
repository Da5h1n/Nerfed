package uk.da5h1n.nerfed.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import net.fabricmc.fabric.api.networking.v1.PlayerLookup;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import uk.da5h1n.nerfed.Nerfed;
import uk.da5h1n.nerfed.network.LimbSyncPayload;
import uk.da5h1n.nerfed.util.LimbDataHelper;

import java.util.List;

public class DisableLimbCommand {
    private static final List<String> LIMB_SUGGESTIONS = List.of("left_arm", "right_arm", "left_leg", "right_leg", "head");
    private static final List<String> STATE_SUGGESTIONS = List.of("disabled", "normal");

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal(Nerfed.MOD_ID)
                .then(Commands.literal("limb")
                        .then(Commands.argument("part", StringArgumentType.word())
                                .suggests((context, builder) -> SharedSuggestionProvider.suggest(LIMB_SUGGESTIONS, builder))
                                .then(Commands.argument("state", StringArgumentType.word())
                                        .suggests((context, builder) -> SharedSuggestionProvider.suggest(STATE_SUGGESTIONS, builder))
                                        .executes(context -> {
                                            ServerPlayer player = context.getSource().getPlayerOrException();
                                            String part = StringArgumentType.getString(context, "part");
                                            String stateStr = StringArgumentType.getString(context, "state");

                                            boolean disabled = stateStr.equalsIgnoreCase("disabled");

                                            if (part.equals("left_arm")) {
                                                LimbDataHelper.setArmDisabled(player, "left", disabled);
                                            } else if (part.equals("right_arm")) {
                                                LimbDataHelper.setArmDisabled(player, "right", disabled);
                                            } else {
                                                player.sendSystemMessage(Component.literal("§e[Nerfed] §7" + part + " set to " + stateStr + " (Logic pending)"));
                                                return 1;
                                            }

                                            // Sync packet to self and tracking players in multiplayer
                                            LimbSyncPayload payload = new LimbSyncPayload(
                                                    player.getId(),
                                                    LimbDataHelper.isLeftArmDisabled(player),
                                                    LimbDataHelper.isRightArmDisabled(player)
                                            );

                                            for (ServerPlayer trackingPlayer : PlayerLookup.tracking(player)) {
                                                ServerPlayNetworking.send(trackingPlayer, payload);
                                            }
                                            ServerPlayNetworking.send(player, payload);

                                            player.sendSystemMessage(Component.literal("§c[Nerfed] §7Set " + part + " state to: " + stateStr));
                                            return 1;
                                        })
                                )
                        )
                )
        );
    }
}