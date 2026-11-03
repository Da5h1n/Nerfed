package uk.da5h1n.nerfed.util;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.fabricmc.fabric.api.attachment.v1.AttachmentRegistry;
import net.fabricmc.fabric.api.attachment.v1.AttachmentType;
import net.minecraft.resources.Identifier;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.player.Player;
import uk.da5h1n.nerfed.Nerfed;

public class LimbDataHelper {
    public static class LimbData {
        private boolean leftDisabled;
        private boolean rightDisabled;

        public LimbData(boolean leftDisabled, boolean rightDisabled) {
            this.leftDisabled = leftDisabled;
            this.rightDisabled = rightDisabled;
        }

        public boolean isLeftDisabled() { return leftDisabled; }
        public void setLeftDisabled(boolean leftDisabled) { this.leftDisabled = leftDisabled; }

        public boolean isRightDisabled() { return rightDisabled; }
        public void setRightDisabled(boolean rightDisabled) { this.rightDisabled = rightDisabled; }
    }

    public static final Codec<LimbData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.BOOL.fieldOf("leftDisabled").forGetter(LimbData::isLeftDisabled),
            Codec.BOOL.fieldOf("rightDisabled").forGetter(LimbData::isRightDisabled)
    ).apply(instance, LimbData::new));

    public static final AttachmentType<LimbData> LIMB_DATA = AttachmentRegistry.createPersistent(
            Identifier.fromNamespaceAndPath(Nerfed.MOD_ID, "limb_data"),
            CODEC
    );

    public static boolean isLeftArmDisabled(Player player) {
        return player.getAttachedOrCreate(LIMB_DATA, () -> new LimbData(false, false)).isLeftDisabled();
    }

    public static boolean isRightArmDisabled(Player player) {
        return player.getAttachedOrCreate(LIMB_DATA, () -> new LimbData(false, false)).isRightDisabled();
    }

    public static void setArmDisabled(Player player, String side, boolean disabled) {
        LimbData data = player.getAttachedOrCreate(LIMB_DATA, () -> new LimbData(false, false));
        if (side.equalsIgnoreCase("left")) {
            data.setLeftDisabled(disabled);
        } else if (side.equalsIgnoreCase("right")) {
            data.setRightDisabled(disabled);
        }
        player.setAttached(LIMB_DATA, data);
    }

    public static boolean isHandDisabled(Player player, InteractionHand hand) {
        boolean isMain = (hand == InteractionHand.MAIN_HAND);
        boolean isLeftMain = (player.getMainArm() == HumanoidArm.LEFT);

        boolean isLeftHand = isMain ? isLeftMain : !isLeftMain;

        return isLeftHand ? isLeftArmDisabled(player) : isRightArmDisabled(player);
    }

    public static boolean isMainArmDisabled(Player player) {
        return isHandDisabled(player, InteractionHand.MAIN_HAND);
    }
}