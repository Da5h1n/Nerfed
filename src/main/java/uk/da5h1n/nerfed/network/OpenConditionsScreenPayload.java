package uk.da5h1n.nerfed.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import uk.da5h1n.nerfed.Nerfed;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public record OpenConditionsScreenPayload(String playerName, boolean self, Map<String, Integer> conditions) implements CustomPacketPayload {

    public static final CustomPacketPayload.Type<OpenConditionsScreenPayload> TYPE = new CustomPacketPayload.Type<>(Identifier.fromNamespaceAndPath(Nerfed.MOD_ID, "open_conditions_screen"));

    public static final StreamCodec<FriendlyByteBuf, OpenConditionsScreenPayload> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.STRING_UTF8, OpenConditionsScreenPayload::playerName,
            ByteBufCodecs.BOOL, OpenConditionsScreenPayload::self,
            ByteBufCodecs.<FriendlyByteBuf, String, Integer, Map<String, Integer>>map(
                    LinkedHashMap::new, ByteBufCodecs.STRING_UTF8, ByteBufCodecs.VAR_INT), OpenConditionsScreenPayload::conditions,
            OpenConditionsScreenPayload::new
    );

    @Override
    public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
