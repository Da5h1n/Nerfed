package uk.da5h1n.nerfed.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import uk.da5h1n.nerfed.Nerfed;

import java.util.HashMap;
import java.util.Map;

public record DisabilitySyncPayload(
        int entityId,
        Map<String, Integer> levels,
        String colorblindMode,
        int colorblindLevel
) implements CustomPacketPayload {

    public static final CustomPacketPayload.Type<DisabilitySyncPayload> TYPE =
            new CustomPacketPayload.Type<>(Identifier.fromNamespaceAndPath(Nerfed.MOD_ID, "disability_sync"));

    public static final StreamCodec<FriendlyByteBuf, DisabilitySyncPayload> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT, DisabilitySyncPayload::entityId,
            ByteBufCodecs.<FriendlyByteBuf, String, Integer, Map<String, Integer>>map(
                    HashMap::new, ByteBufCodecs.STRING_UTF8, ByteBufCodecs.VAR_INT), DisabilitySyncPayload::levels,
            ByteBufCodecs.STRING_UTF8, DisabilitySyncPayload::colorblindMode,
            ByteBufCodecs.VAR_INT, DisabilitySyncPayload::colorblindLevel,
            DisabilitySyncPayload::new
    );

    @Override
    public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
