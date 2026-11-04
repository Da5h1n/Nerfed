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

public class DisabilityDataHelper {

    public static final AttachmentType<DisabilityData> DISABILITY_DATA = AttachmentRegistry.<DisabilityData>builder()
            .persistent(DisabilityData.CODEC)
            .copyOnDeath()
            .buildAndRegister(Identifier.fromNamespaceAndPath(Nerfed.MOD_ID, "disability_data"));

    public static class DisabilityData {
        private boolean leftArmDisabled;
        private boolean rightArmDisabled;
        private boolean leftLegDisabled;
        private boolean rightLegDisabled;

        // Sensory & Neurological States
        private String colorblindMode; // "none", "protan", "deuteran", "tritan", "mono"
        private boolean tunnelVision;
        private boolean myopia;
        private boolean tremors;

        public DisabilityData(boolean leftArm, boolean rightArm, boolean leftLeg, boolean rightLeg,
                              String colorblindMode, boolean tunnelVision, boolean myopia, boolean tremors) {
            this.leftArmDisabled = leftArm;
            this.rightArmDisabled = rightArm;
            this.leftLegDisabled = leftLeg;
            this.rightLegDisabled = rightLeg;
            this.colorblindMode = colorblindMode != null ? colorblindMode : "none";
            this.tunnelVision = tunnelVision;
            this.myopia = myopia;
            this.tremors = tremors;
        }

        public static final Codec<DisabilityData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                Codec.BOOL.fieldOf("left_arm").orElse(false).forGetter(o -> o.leftArmDisabled),
                Codec.BOOL.fieldOf("right_arm").orElse(false).forGetter(o -> o.rightArmDisabled),
                Codec.BOOL.fieldOf("left_leg").orElse(false).forGetter(o -> o.leftLegDisabled),
                Codec.BOOL.fieldOf("right_leg").orElse(false).forGetter(o -> o.rightLegDisabled),
                Codec.STRING.fieldOf("colorblind_mode").orElse("none").forGetter(o -> o.colorblindMode),
                Codec.BOOL.fieldOf("tunnel_vision").orElse(false).forGetter(o -> o.tunnelVision),
                Codec.BOOL.fieldOf("myopia").orElse(false).forGetter(o -> o.myopia),
                Codec.BOOL.fieldOf("tremors").orElse(false).forGetter(o -> o.tremors)
        ).apply(instance, DisabilityData::new));

        // Getters & Setters
        public boolean isLeftArmDisabled() { return leftArmDisabled; }
        public void setLeftArmDisabled(boolean v) { this.leftArmDisabled = v; }
        public boolean isRightArmDisabled() { return rightArmDisabled; }
        public void setRightArmDisabled(boolean v) { this.rightArmDisabled = v; }
        public boolean isLeftLegDisabled() { return leftLegDisabled; }
        public void setLeftLegDisabled(boolean v) { this.leftLegDisabled = v; }
        public boolean isRightLegDisabled() { return rightLegDisabled; }
        public void setRightLegDisabled(boolean v) { this.rightLegDisabled = v; }

        public String getColorblindMode() { return colorblindMode; }
        public void setColorblindMode(String mode) { this.colorblindMode = mode; }
        public boolean isTunnelVision() { return tunnelVision; }
        public void setTunnelVision(boolean v) { this.tunnelVision = v; }
        public boolean isMyopia() { return myopia; }
        public void setMyopia(boolean v) { this.myopia = v; }
        public boolean isTremors() { return tremors; }
        public void setTremors(boolean v) { this.tremors = v; }
    }

    // Helper shortcuts
    public static DisabilityData getData(Player player) {
        return player.getAttachedOrCreate(DISABILITY_DATA, () -> new DisabilityData(false, false, false, false, "none", false, false, false));
    }

    public static boolean isLeftArmDisabled(Player player) { return getData(player).isLeftArmDisabled(); }
    public static boolean isRightArmDisabled(Player player) { return getData(player).isRightArmDisabled(); }
    public static boolean isLeftLegDisabled(Player player) { return getData(player).isLeftLegDisabled(); }
    public static boolean isRightLegDisabled(Player player) { return getData(player).isRightLegDisabled(); }

    public static String getColorblindMode(Player player) { return getData(player).getColorblindMode(); }
    public static boolean isTunnelVision(Player player) { return getData(player).isTunnelVision(); }
    public static boolean isMyopia(Player player) { return getData(player).isMyopia(); }
    public static boolean isTremors(Player player) { return getData(player).isTremors(); }

    public static void setLimbDisabled(Player player, String limbType, String side, boolean disabled) {
        DisabilityData data = getData(player);
        boolean isLeft = side.equalsIgnoreCase("left");

        if (limbType.equalsIgnoreCase("arm")) {
            if (isLeft) data.setLeftArmDisabled(disabled);
            else data.setRightArmDisabled(disabled);
        } else if (limbType.equalsIgnoreCase("leg")) {
            if (isLeft) data.setLeftLegDisabled(disabled);
            else data.setRightLegDisabled(disabled);
        }
        player.setAttached(DISABILITY_DATA, data);
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