package uk.da5h1n.nerfed.network;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import uk.da5h1n.nerfed.Nerfed;

public record SetLocalColourblindPayload(String mode, int level) implements CustomPacketPayload {
    public static final Type<SetLocalColourblindPayload> TYPE =
            new Type<>(Nerfed.id("set_local_colourblind"));

    public static final StreamCodec<RegistryFriendlyByteBuf, SetLocalColourblindPayload> STREAM_CODEC =
            StreamCodec.composite(
                    ByteBufCodecs.STRING_UTF8, SetLocalColourblindPayload::mode,
                    ByteBufCodecs.VAR_INT, SetLocalColourblindPayload::level,
                    SetLocalColourblindPayload::new
            );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}