package uk.da5h1n.nerfed.event;

import net.fabricmc.fabric.api.message.v1.ServerMessageEvents;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import uk.da5h1n.nerfed.util.Condition;
import uk.da5h1n.nerfed.util.DisabilityDataHelper;

public class MuteHandler {

    public static void register() {

        ServerMessageEvents.ALLOW_CHAT_MESSAGE.register((message, sender, params) -> allow(sender));

        ServerMessageEvents.ALLOW_COMMAND_MESSAGE.register((message, source, params) -> allow(source.getPlayer()));
    }

    private static boolean allow(ServerPlayer player) {
        if (player == null) return true;
        if (!DisabilityDataHelper.getData(player).has(Condition.MUTE)) return true;

        player.sendSystemMessage(Component.literal("§c[Nerfed] §7You can't speak."));
        return false;
    }
}
