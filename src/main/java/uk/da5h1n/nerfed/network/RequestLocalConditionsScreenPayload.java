package uk.da5h1n.nerfed.network;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import uk.da5h1n.nerfed.Nerfed;

public record RequestLocalConditionsScreenPayload() implements CustomPacketPayload {
    public static final Type<RequestLocalConditionsScreenPayload> TYPE =
            new Type<>(Nerfed.id("request_local_conditions_screen"));

    public static final StreamCodec<RegistryFriendlyByteBuf, RequestLocalConditionsScreenPayload> STREAM_CODEC =
            StreamCodec.unit(new RequestLocalConditionsScreenPayload());

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}