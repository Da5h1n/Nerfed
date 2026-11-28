package uk.da5h1n.nerfed.client;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.command.v2.ClientCommands;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import uk.da5h1n.nerfed.Nerfed;
import uk.da5h1n.nerfed.network.RequestLocalConditionsScreenPayload;
import uk.da5h1n.nerfed.network.SetLocalColourblindPayload;
import uk.da5h1n.nerfed.network.SetLocalDisabilityPayload;
import uk.da5h1n.nerfed.util.Condition;
import uk.da5h1n.nerfed.util.DisabilityDataHelper.DisabilityData;

import java.util.List;

/**
 * Client-side copy of /nerfed, so the command works on servers without Nerfed.
 *
 * On a server that has Nerfed, actions are sent via custom C2S packets instead of
 * re-sending the command string through the client dispatcher (avoiding command loops).
 * Only literal nodes are used (no argument types).
 */
public final class LocalDisabilityCommand {
    private static final List<String> COLOURBLIND_MODES = List.of("protan", "deuteran", "tritan", "mono");

    private LocalDisabilityCommand() {}

    public static void register() {
        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> {
            LiteralArgumentBuilder<FabricClientCommandSource> disability = ClientCommands.literal("disability");

            // /nerfed disability colourblind <protan|deuteran|tritan|mono> <0-3>
            // /nerfed disability colourblind disable
            LiteralArgumentBuilder<FabricClientCommandSource> colourblind = ClientCommands.literal("colourblind");
            for (String mode : COLOURBLIND_MODES) {
                LiteralArgumentBuilder<FabricClientCommandSource> modeNode = ClientCommands.literal(mode);
                for (int i = 0; i <= Condition.COLOURBLIND_MAX_LEVEL; i++) {
                    final int level = i;
                    modeNode.then(ClientCommands.literal(String.valueOf(level))
                            .executes(ctx -> setColourblind(ctx, mode, level)));
                }
                colourblind.then(modeNode);
            }
            colourblind.then(ClientCommands.literal("disable").executes(LocalDisabilityCommand::clearColourblind));
            disability.then(colourblind);

            // /nerfed disability limbs <limb> <level>
            // /nerfed disability <condition> <level>
            LiteralArgumentBuilder<FabricClientCommandSource> limbs = ClientCommands.literal("limbs");
            for (Condition condition : Condition.values()) {
                LiteralArgumentBuilder<FabricClientCommandSource> node = ClientCommands.literal(condition.id());
                for (int i = 0; i <= condition.maxLevel(); i++) {
                    final int level = i;
                    node.then(ClientCommands.literal(String.valueOf(level))
                            .executes(ctx -> setLevel(ctx, condition, level)));
                }
                if (condition.category() == Condition.Category.LIMBS) limbs.then(node);
                else disability.then(node);
            }
            disability.then(limbs);

            // /nerfed show
            LiteralArgumentBuilder<FabricClientCommandSource> show = ClientCommands.literal("show")
                    .executes(LocalDisabilityCommand::show);

            dispatcher.register(ClientCommands.literal(Nerfed.MOD_ID).then(disability).then(show));
        });
    }

    /** Conditions the server has to enforce can't be faked locally on an unmodded server. */
    private static boolean needsServer(Condition condition) {
        return condition.category() == Condition.Category.LIMBS;
    }

    private static int setLevel(CommandContext<FabricClientCommandSource> ctx, Condition condition, int level) {
        if (LocalConditions.serverHasNerfed()) {
            ClientPlayNetworking.send(new SetLocalDisabilityPayload(condition.id(), level));
            return 1;
        }

        if (needsServer(condition)) {
            ctx.getSource().sendError(Component.literal("§c[Nerfed] §7" + condition.id()
                    + " is enforced by the server, so it needs Nerfed installed there."));
            return 0;
        }

        LocalConditions.data().setLevel(condition, level);
        LocalConditions.markChanged();
        ctx.getSource().sendFeedback(Component.literal("§c[Nerfed] §7" + condition.id() + (level == 0 ? " is now off" : " is now level " + level) + " §8(local, only you)"));

        if (condition == Condition.MUTE && level > 0) {
            ctx.getSource().sendFeedback(Component.literal(
                    "§8Only your voice chat is muffled. Text chat muting needs Nerfed on the server."));
        }
        
        return 1;


    }

    private static int setColourblind(CommandContext<FabricClientCommandSource> ctx, String mode, int level) {
        if (LocalConditions.serverHasNerfed()) {
            ClientPlayNetworking.send(new SetLocalColourblindPayload(mode, level));
            return 1;
        }

        DisabilityData data = LocalConditions.data();
        if (level > 0) {
            data.setColorblind(mode, level);
        } else if (mode.equalsIgnoreCase(data.getColorblindMode())) {
            data.setColorblind("none", 0);
        } else {
            ctx.getSource().sendFeedback(Component.literal("§e[Nerfed] Colourblind mode is '"
                    + data.getColorblindMode() + "', not '" + mode + "'. Nothing changed."));
            return 1;
        }

        LocalConditions.markChanged();
        ctx.getSource().sendFeedback(Component.literal("§c[Nerfed] §7Colourblindness: "
                + data.getColorblindMode() + " (level " + data.getColorblindLevel() + ") §8(local, only you)"));
        return 1;
    }

    private static int clearColourblind(CommandContext<FabricClientCommandSource> ctx) {
        if (LocalConditions.serverHasNerfed()) {
            ClientPlayNetworking.send(new SetLocalColourblindPayload("none", 0));
            return 1;
        }

        LocalConditions.data().setColorblind("none", 0);
        LocalConditions.markChanged();
        ctx.getSource().sendFeedback(Component.literal("§c[Nerfed] §7Colourblindness disabled. §8(local, only you)"));
        return 1;
    }

    private static int show(CommandContext<FabricClientCommandSource> ctx) {
        if (LocalConditions.serverHasNerfed()) {
            ClientPlayNetworking.send(new RequestLocalConditionsScreenPayload());
            return 1;
        }

        Minecraft client = Minecraft.getInstance();
        if (client.player == null) return 0;
        String name = client.player.getName().getString();

        // Opened next tick, because the chat screen would otherwise close it again straight away
        LocalConditions.runNextTick(() -> client.gui.setScreen(
                new ConditionsScreen(name, true, LocalConditions.data().activeLevels())));
        return 1;
    }
}