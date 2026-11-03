package uk.da5h1n.nerfed.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import uk.da5h1n.nerfed.Nerfed;

public record LimbSyncPayload(int entityId, boolean leftDisabled, boolean rightDisabled) implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<LimbSyncPayload> TYPE =
            new CustomPacketPayload.Type<>(Identifier.fromNamespaceAndPath(Nerfed.MOD_ID, "limb_sync"));

    public static final StreamCodec<FriendlyByteBuf, LimbSyncPayload> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT, LimbSyncPayload::entityId,
            ByteBufCodecs.BOOL, LimbSyncPayload::leftDisabled,
            ByteBufCodecs.BOOL, LimbSyncPayload::rightDisabled,
            LimbSyncPayload::new
    );

    @Override
    public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
