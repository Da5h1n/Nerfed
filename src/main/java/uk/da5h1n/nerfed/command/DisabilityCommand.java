package uk.da5h1n.nerfed.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.fabricmc.fabric.api.networking.v1.PlayerLookup;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import uk.da5h1n.nerfed.Nerfed;
import uk.da5h1n.nerfed.network.ConditionsViewer;
import uk.da5h1n.nerfed.network.DisabilitySync;
import uk.da5h1n.nerfed.network.DisabilitySyncPayload;
import uk.da5h1n.nerfed.util.Condition;
import uk.da5h1n.nerfed.util.DisabilityDataHelper;

import java.util.ArrayList;
import java.util.List;

public class DisabilityCommand {
    private static final List<String> COLOURBLIND_MODES = List.of("protan", "deuteran", "tritan", "mono");

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        LiteralArgumentBuilder<CommandSourceStack> disability = Commands.literal("disability");

        // /nerfed disability colourblind <protan|deuteran|tritan|mono> <0-3>
        // /nerfed disability colourblind disable
        LiteralArgumentBuilder<CommandSourceStack> colourblind = Commands.literal("colourblind");
        for (String mode : COLOURBLIND_MODES) {
            colourblind.then(Commands.literal(mode)
                    .then(levelArgument(Condition.COLOURBLIND_MAX_LEVEL)
                            .executes(ctx -> setColourblind(ctx, mode))));
        }
        colourblind.then(Commands.literal("disable").executes(DisabilityCommand::clearColourblind));
        disability.then(colourblind);

        // /nerfed disability limbs <left_arm|...> <level>
        // /nerfed disability <tunnel_vision|myopia|...> <level>
        LiteralArgumentBuilder<CommandSourceStack> limbs = Commands.literal("limbs");
        for (Condition condition : Condition.values()) {
            LiteralArgumentBuilder<CommandSourceStack> node = Commands.literal(condition.id())
                    .then(levelArgument(condition.maxLevel())
                            .executes(ctx -> setLevel(ctx, condition)));
            if (condition.category() == Condition.Category.LIMBS) limbs.then(node);
            else disability.then(node);
        }
        disability.then(limbs);

        // /nerfed show [player]
        LiteralArgumentBuilder<CommandSourceStack> show = Commands.literal("show")
                .executes(ctx -> {
                    ServerPlayer viewer = ctx.getSource().getPlayerOrException();
                    ConditionsViewer.open(viewer, viewer);
                    return 1;
                })
                .then(Commands.argument("player", EntityArgument.player())
                        .executes(ctx -> {
                            ServerPlayer viewer = ctx.getSource().getPlayerOrException();
                            ServerPlayer target = EntityArgument.getPlayer(ctx, "player");
                            ConditionsViewer.open(viewer, target);
                            return 1;
                        }));

        dispatcher.register(Commands.literal(Nerfed.MOD_ID).then(disability).then(show));
    }

    private static RequiredArgumentBuilder<CommandSourceStack, Integer> levelArgument(int max) {
        return Commands.argument("level", IntegerArgumentType.integer(0, max))
                .suggests((ctx, builder) -> {
                    List<String> options = new ArrayList<>();
                    for ( int i = 0; i <= max; i++) options.add(String.valueOf(i));
                    return SharedSuggestionProvider.suggest(options, builder);
                });
    }

    private static int setLevel(CommandContext<CommandSourceStack> ctx, Condition condition) throws CommandSyntaxException {
        ServerPlayer player = ctx.getSource().getPlayerOrException();
        int level = IntegerArgumentType.getInteger(ctx, "level");
        DisabilityDataHelper.DisabilityData data = DisabilityDataHelper.getData(player);

        data.setLevel(condition, level);

        saveAndSync(player, data);
        player.sendSystemMessage(Component.literal("§c[Nerfed] §7" + condition.id()
                + (level == 0 ? " is now off" : " is now level " + level)));
        return 1;
    }

    private static int setColourblind(CommandContext<CommandSourceStack> ctx, String mode) throws CommandSyntaxException {
        ServerPlayer player = ctx.getSource().getPlayerOrException();
        int level = IntegerArgumentType.getInteger(ctx, "level");
        DisabilityDataHelper.DisabilityData data = DisabilityDataHelper.getData(player);

        if (level > 0) {
            data.setColorblind(mode, level);
        } else if (mode.equalsIgnoreCase(data.getColorblindMode())) {
            data.setColorblind("none", 0);
        } else {
            player.sendSystemMessage(Component.literal("§e[Nerfed] Colourblind mode is '"
                    + data.getColorblindMode() + "', not '" + mode + "'. Nothing changed."));
            return 1;
        }

        saveAndSync(player, data);
        player.sendSystemMessage(Component.literal("§c[Nerfed] §7Colourblindness: "
                + data.getColorblindMode() + " (level " + data.getColorblindLevel() + ")"));
        return 1;
    }

    private static int clearColourblind(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        ServerPlayer player = ctx.getSource().getPlayerOrException();
        DisabilityDataHelper.DisabilityData data = DisabilityDataHelper.getData(player);

        data.setColorblind("none", 0);

        saveAndSync(player, data);
        player.sendSystemMessage(Component.literal("§c[Nerfed] §7Colourblindness disabled."));
        return 1;
    }

    private static void saveAndSync(ServerPlayer player, DisabilityDataHelper.DisabilityData data) {
        player.setAttached(DisabilityDataHelper.DISABILITY_DATA, data);
        DisabilitySync.syncAll(player);
    }
}