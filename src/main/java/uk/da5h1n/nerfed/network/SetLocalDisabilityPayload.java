package uk.da5h1n.nerfed.network;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import uk.da5h1n.nerfed.Nerfed;

public record SetLocalDisabilityPayload(String conditionId, int level) implements CustomPacketPayload {
    public static final Type<SetLocalDisabilityPayload> TYPE =
            new Type<>(Nerfed.id("set_local_disability"));

    public static final StreamCodec<RegistryFriendlyByteBuf, SetLocalDisabilityPayload> STREAM_CODEC =
            StreamCodec.composite(
                    ByteBufCodecs.STRING_UTF8, SetLocalDisabilityPayload::conditionId,
                    ByteBufCodecs.VAR_INT, SetLocalDisabilityPayload::level,
                    SetLocalDisabilityPayload::new
            );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}