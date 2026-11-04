package uk.da5h1n.nerfed.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import uk.da5h1n.nerfed.Nerfed;

public record DisabilitySyncPayload(int entityId, boolean leftArmDisabled, boolean rightArmDisabled, boolean leftLegDisabled, boolean rightLegDisabled, String colorblindMode, boolean tunnelVision, boolean myopia, boolean tremors) implements CustomPacketPayload {

    public static final CustomPacketPayload.Type<DisabilitySyncPayload> TYPE =
            new CustomPacketPayload.Type<>(Identifier.fromNamespaceAndPath(Nerfed.MOD_ID, "disability_sync"));

    public static final StreamCodec<FriendlyByteBuf, DisabilitySyncPayload> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT, DisabilitySyncPayload::entityId,
            ByteBufCodecs.BOOL, DisabilitySyncPayload::leftArmDisabled,
            ByteBufCodecs.BOOL, DisabilitySyncPayload::rightArmDisabled,
            ByteBufCodecs.BOOL, DisabilitySyncPayload::leftLegDisabled,
            ByteBufCodecs.BOOL, DisabilitySyncPayload::rightLegDisabled,
            ByteBufCodecs.STRING_UTF8, DisabilitySyncPayload::colorblindMode,
            ByteBufCodecs.BOOL, DisabilitySyncPayload::tunnelVision,
            ByteBufCodecs.BOOL, DisabilitySyncPayload::myopia,
            ByteBufCodecs.BOOL, DisabilitySyncPayload::tremors,
            DisabilitySyncPayload::new
    );

    @Override
    public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
