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
import uk.da5h1n.nerfed.network.DisabilitySyncPayload;
import uk.da5h1n.nerfed.util.DisabilityDataHelper;

import java.util.List;

public class DisabilityCommand {
    private static final List<String> TARGET_SUGGESTIONS = List.of(
            "left_arm", "right_arm", "left_leg", "right_leg",
            "colorblind", "tunnel_vision", "myopia", "tremors"
    );

    private static final List<String> STATE_SUGGESTIONS = List.of("disabled", "normal", "none", "protan", "deuteran", "tritan", "mono", "true", "false");

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal(Nerfed.MOD_ID)
                .then(Commands.literal("disability")
                        .then(Commands.argument("target", StringArgumentType.word())
                                .suggests((context, builder) -> SharedSuggestionProvider.suggest(TARGET_SUGGESTIONS, builder))
                                .then(Commands.argument("value", StringArgumentType.word())
                                        .suggests((context, builder) -> SharedSuggestionProvider.suggest(STATE_SUGGESTIONS, builder))
                                        .executes(context -> {
                                            ServerPlayer player = context.getSource().getPlayerOrException();
                                            String target = StringArgumentType.getString(context, "target");
                                            String val = StringArgumentType.getString(context, "value");

                                            DisabilityDataHelper.DisabilityData data = DisabilityDataHelper.getData(player);

                                            switch (target) {
                                                case "left_arm" -> data.setLeftArmDisabled(val.equalsIgnoreCase("disabled") || val.equalsIgnoreCase("true"));
                                                case "right_arm" -> data.setRightArmDisabled(val.equalsIgnoreCase("disabled") || val.equalsIgnoreCase("true"));
                                                case "left_leg" -> data.setLeftLegDisabled(val.equalsIgnoreCase("disabled") || val.equalsIgnoreCase("true"));
                                                case "right_leg" -> data.setRightLegDisabled(val.equalsIgnoreCase("disabled") || val.equalsIgnoreCase("true"));
                                                case "colorblind" -> data.setColorblindMode(val.toLowerCase());
                                                case "tunnel_vision" -> data.setTunnelVision(val.equalsIgnoreCase("disabled") || val.equalsIgnoreCase("true"));
                                                case "myopia" -> data.setMyopia(val.equalsIgnoreCase("disabled") || val.equalsIgnoreCase("true"));
                                                case "tremors" -> data.setTremors(val.equalsIgnoreCase("disabled") || val.equalsIgnoreCase("true"));
                                                default -> {
                                                    player.sendSystemMessage(Component.literal("§e[Nerfed] Unknown target: " + target));
                                                    return 1;
                                                }
                                            }

                                            player.setAttached(DisabilityDataHelper.DISABILITY_DATA, data);

                                            DisabilitySyncPayload payload = new DisabilitySyncPayload(
                                                    player.getId(),
                                                    data.isLeftArmDisabled(),
                                                    data.isRightArmDisabled(),
                                                    data.isLeftLegDisabled(),
                                                    data.isRightLegDisabled(),
                                                    data.getColorblindMode(),
                                                    data.isTunnelVision(),
                                                    data.isMyopia(),
                                                    data.isTremors()
                                            );

                                            for (ServerPlayer trackingPlayer : PlayerLookup.tracking(player)) {
                                                ServerPlayNetworking.send(trackingPlayer, payload);
                                            }
                                            ServerPlayNetworking.send(player, payload);

                                            player.sendSystemMessage(Component.literal("§c[Nerfed] §7Set " + target + " to: " + val));
                                            return 1;
                                        })
                                )
                        )
                )
        );
    }
}