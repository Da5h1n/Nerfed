package uk.da5h1n.nerfed.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.fabricmc.fabric.api.networking.v1.PlayerLookup;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import uk.da5h1n.nerfed.Nerfed;
import uk.da5h1n.nerfed.network.DisabilitySyncPayload;
import uk.da5h1n.nerfed.util.DisabilityDataHelper;

import java.util.List;

public class DisabilityCommand {
    private static final List<String> COLOURBLIND_MODES = List.of("protan", "deuteran", "tritan", "mono");
    private static final List<String> LIMBS = List.of("left_arm", "right_arm", "left_leg", "right_leg");
    private static final List<String> OTHER = List.of("tunnel_vision", "myopia", "tremors");

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        LiteralArgumentBuilder<CommandSourceStack> disability = Commands.literal("disability");

        // /nerfed disability colourblind <protan|deuteran|tritan|mono> <true|false>
        // /nerfed disability colourblind disable
        LiteralArgumentBuilder<CommandSourceStack> colourblind = Commands.literal("colourblind");
        for (String mode : COLOURBLIND_MODES) {
            colourblind.then(Commands.literal(mode)
                    .then(Commands.argument("value", BoolArgumentType.bool())
                            .executes(ctx -> setColourblind(ctx, mode))));
        }
        colourblind.then(Commands.literal("disable")
                .executes(ctx -> clearColourblind(ctx)));
        disability.then(colourblind);

        // /nerfed disability limbs <left_arm|right_arm|left_leg|right_leg> <true|false>
        LiteralArgumentBuilder<CommandSourceStack> limbs = Commands.literal("limbs");
        for (String limb : LIMBS) {
            limbs.then(Commands.literal(limb)
                    .then(Commands.argument("value", BoolArgumentType.bool())
                            .executes(ctx -> setBoolean(ctx, limb))));
        }
        disability.then(limbs);

        // /nerfed disability <tunnel_vision|myopia|tremors> <true|false>
        for (String other : OTHER) {
            disability.then(Commands.literal(other)
                    .then(Commands.argument("value", BoolArgumentType.bool())
                            .executes(ctx -> setBoolean(ctx, other))));
        }

        dispatcher.register(Commands.literal(Nerfed.MOD_ID).then(disability));
    }

    private static int setBoolean(CommandContext<CommandSourceStack> ctx, String target) throws CommandSyntaxException {
        ServerPlayer player = ctx.getSource().getPlayerOrException();
        boolean value = BoolArgumentType.getBool(ctx, "value");
        DisabilityDataHelper.DisabilityData data = DisabilityDataHelper.getData(player);

        switch (target) {
            case "left_arm" -> data.setLeftArmDisabled(value);
            case "right_arm" -> data.setRightArmDisabled(value);
            case "left_leg" -> data.setLeftLegDisabled(value);
            case "right_leg" -> data.setRightLegDisabled(value);
            case "tunnel_vision" -> data.setTunnelVision(value);
            case "myopia" -> data.setMyopia(value);
            case "tremors" -> data.setTremors(value);
        }

        saveAndSync(player, data);
        player.sendSystemMessage(Component.literal("§c[Nerfed] §7Set " + target + " to: " + value));
        return 1;
    }

    private static int setColourblind(CommandContext<CommandSourceStack> ctx, String mode) throws CommandSyntaxException {
        ServerPlayer player = ctx.getSource().getPlayerOrException();
        boolean value = BoolArgumentType.getBool(ctx, "value");
        DisabilityDataHelper.DisabilityData data = DisabilityDataHelper.getData(player);

        if (value) {
            data.setColorblindMode(mode);
        } else if (mode.equalsIgnoreCase(data.getColorblindMode())) {
            data.setColorblindMode("none");
        } else {
            player.sendSystemMessage(Component.literal(
                    "§e[Nerfed] Colourblind mode is '" + data.getColorblindMode() + "', not '" + mode + "'. Nothing changed."));
            return 1;
        }

        saveAndSync(player, data);
        player.sendSystemMessage(Component.literal(
                "§c[Nerfed] §7Colourblind mode is now: " + data.getColorblindMode()));
        return 1;
    }

    private static int clearColourblind(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        ServerPlayer player = ctx.getSource().getPlayerOrException();
        DisabilityDataHelper.DisabilityData data = DisabilityDataHelper.getData(player);

        data.setColorblindMode("none");

        saveAndSync(player, data);
        player.sendSystemMessage(Component.literal("§c[Nerfed] §7Colourblind mode disabled."));
        return 1;
    }

    private static void saveAndSync(ServerPlayer player, DisabilityDataHelper.DisabilityData data) {
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
    }
}