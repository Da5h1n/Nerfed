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
    private static final List<String> OTHER = List.of(
            "tunnel_vision", "myopia", "tremors",
            "adhd", "vertigo", "scoliosis", "prosopagnosia", "schizophrenia"
    );

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        LiteralArgumentBuilder<CommandSourceStack> disability = Commands.literal("disability");

        // /nerfed disability colourblind <protan|deuteran|tritan|mono> <apply|remove>
        // /nerfed disability colourblind disable
        LiteralArgumentBuilder<CommandSourceStack> colourblind = Commands.literal("colourblind");
        for (String mode : COLOURBLIND_MODES) {
            colourblind.then(Commands.literal(mode)
                    .then(Commands.literal("apply").executes(ctx -> setColourblind(ctx, mode, true)))
                    .then(Commands.literal("remove").executes(ctx -> setColourblind(ctx, mode, false))));
        }
        colourblind.then(Commands.literal("disable").executes(ctx -> clearColourblind(ctx)));
        disability.then(colourblind);

        // /nerfed disability limbs <left_arm|right_arm|left_leg|right_leg> <apply|remove>
        LiteralArgumentBuilder<CommandSourceStack> limbs = Commands.literal("limbs");
        for (String limb : LIMBS) {
            limbs.then(Commands.literal(limb)
                    .then(Commands.literal("apply").executes(ctx -> setBoolean(ctx, limb, true)))
                    .then(Commands.literal("remove").executes(ctx -> setBoolean(ctx, limb, false))));
        }
        disability.then(limbs);

        // Direct format: /nerfed disability <tunnel_vision|myopia|tremors|adhd|vertigo|scoliosis|prosopagnosia> <apply|remove>
        for (String other : OTHER) {
            disability.then(Commands.literal(other)
                    .then(Commands.literal("apply").executes(ctx -> setBoolean(ctx, other, true)))
                    .then(Commands.literal("remove").executes(ctx -> setBoolean(ctx, other, false))));
        }
        dispatcher.register(Commands.literal(Nerfed.MOD_ID).then(disability));
    }

    private static int setBoolean(CommandContext<CommandSourceStack> ctx, String target, boolean apply) throws CommandSyntaxException {
        ServerPlayer player = ctx.getSource().getPlayerOrException();

        DisabilityDataHelper.setFlag(player, target, apply);
        DisabilityDataHelper.DisabilityData data = DisabilityDataHelper.getData(player);

        saveAndSync(player, data);

        String statusMsg = apply ? "§aapplied" : "§cremoved";
        player.sendSystemMessage(Component.literal("§c[Nerfed] §7Successfully " + statusMsg + " §7condition: §e" + target));
        return 1;
    }

    private static int setColourblind(CommandContext<CommandSourceStack> ctx, String mode, boolean apply) throws CommandSyntaxException {
        ServerPlayer player = ctx.getSource().getPlayerOrException();
        DisabilityDataHelper.DisabilityData data = DisabilityDataHelper.getData(player);

        if (apply) {
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
                data.isTremors(),
                data.isAdhd(),
                data.isVertigo(),
                data.isScoliosis(),
                data.isProsopagnosia(),
                data.isSchizophrenia()
        );

        for (ServerPlayer trackingPlayer : PlayerLookup.tracking(player)) {
            ServerPlayNetworking.send(trackingPlayer, payload);
        }
        ServerPlayNetworking.send(player, payload);
    }
}