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

import java.util.Locale;

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

        // Sensory & Neurological & Physical States
        private String colorblindMode; // "none", "protan", "deuteran", "tritan", "mono"
        private boolean tunnelVision;
        private boolean myopia;
        private boolean tremors;
        private boolean adhd;
        private boolean vertigo;
        private boolean scoliosis;
        private boolean prosopagnosia;
        private boolean schizophrenia;

        public DisabilityData(boolean leftArm, boolean rightArm, boolean leftLeg, boolean rightLeg,
                              String colorblindMode, boolean tunnelVision, boolean myopia, boolean tremors,
                              boolean adhd, boolean vertigo, boolean scoliosis, boolean prosopagnosia, boolean schizophrenia) {
            this.leftArmDisabled = leftArm;
            this.rightArmDisabled = rightArm;
            this.leftLegDisabled = leftLeg;
            this.rightLegDisabled = rightLeg;
            this.colorblindMode = colorblindMode != null ? colorblindMode : "none";
            this.tunnelVision = tunnelVision;
            this.myopia = myopia;
            this.tremors = tremors;
            this.adhd = adhd;
            this.vertigo = vertigo;
            this.scoliosis = scoliosis;
            this.prosopagnosia = prosopagnosia;
            this.schizophrenia = schizophrenia;
        }

        public static final Codec<DisabilityData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                Codec.BOOL.fieldOf("left_arm").orElse(false).forGetter(o -> o.leftArmDisabled),
                Codec.BOOL.fieldOf("right_arm").orElse(false).forGetter(o -> o.rightArmDisabled),
                Codec.BOOL.fieldOf("left_leg").orElse(false).forGetter(o -> o.leftLegDisabled),
                Codec.BOOL.fieldOf("right_leg").orElse(false).forGetter(o -> o.rightLegDisabled),
                Codec.STRING.fieldOf("colorblind_mode").orElse("none").forGetter(o -> o.colorblindMode),
                Codec.BOOL.fieldOf("tunnel_vision").orElse(false).forGetter(o -> o.tunnelVision),
                Codec.BOOL.fieldOf("myopia").orElse(false).forGetter(o -> o.myopia),
                Codec.BOOL.fieldOf("tremors").orElse(false).forGetter(o -> o.tremors),
                Codec.BOOL.fieldOf("adhd").orElse(false).forGetter(o -> o.adhd),
                Codec.BOOL.fieldOf("vertigo").orElse(false).forGetter(o -> o.vertigo),
                Codec.BOOL.fieldOf("scoliosis").orElse(false).forGetter(o -> o.scoliosis),
                Codec.BOOL.fieldOf("prosopagnosia").orElse(false).forGetter(o -> o.prosopagnosia),
                Codec.BOOL.fieldOf("schizophrenia").orElse(false).forGetter(o -> o.schizophrenia)
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
        public boolean isAdhd() { return adhd; }
        public void setAdhd(boolean v) { this.adhd = v; }
        public boolean isVertigo() { return vertigo; }
        public void setVertigo(boolean v) { this.vertigo = v; }
        public boolean isScoliosis() { return scoliosis; }
        public void setScoliosis(boolean v) { this.scoliosis = v; }
        public boolean isProsopagnosia() { return prosopagnosia; }
        public void setProsopagnosia(boolean v) { this.prosopagnosia = v; }
        public boolean isSchizophrenia() { return schizophrenia; }
        public void setSchizophrenia(boolean v) { this.schizophrenia = v; }
    }

    // Helper shortcuts
    public static DisabilityData getData(Player player) {
        return player.getAttachedOrCreate(DISABILITY_DATA, () -> new DisabilityData(false, false, false, false, "none", false, false, false, false, false, false, false, false));
    }

    public static boolean isLeftArmDisabled(Player player) { return getData(player).isLeftArmDisabled(); }
    public static boolean isRightArmDisabled(Player player) { return getData(player).isRightArmDisabled(); }
    public static boolean isLeftLegDisabled(Player player) { return getData(player).isLeftLegDisabled(); }
    public static boolean isRightLegDisabled(Player player) { return getData(player).isRightLegDisabled(); }

    public static String getColorblindMode(Player player) { return getData(player).getColorblindMode(); }
    public static boolean isTunnelVision(Player player) { return getData(player).isTunnelVision(); }
    public static boolean isMyopia(Player player) { return getData(player).isMyopia(); }
    public static boolean isTremors(Player player) { return getData(player).isTremors(); }
    public static boolean isAdhd(Player player) { return getData(player).isAdhd(); }
    public static boolean isVertigo(Player player) { return getData(player).isVertigo(); }
    public static boolean isScoliosis(Player player) { return getData(player).isScoliosis(); }
    public static boolean isProsopagnosia(Player player) { return getData(player).isProsopagnosia(); }
    public static boolean isSchizophrenia(Player player) { return getData(player).isSchizophrenia(); }

    public static void setFlag(Player player, String target, boolean value) {
        DisabilityData data = getData(player);

        switch (target.toLowerCase()) {
            case "left_arm" -> data.setLeftArmDisabled(value);
            case "right_arm" -> data.setRightArmDisabled(value);
            case "left_leg" -> data.setLeftLegDisabled(value);
            case "right_leg" -> data.setRightLegDisabled(value);
            case "tunnel_vision" -> data.setTunnelVision(value);
            case "myopia" -> data.setMyopia(value);
            case "tremors" -> data.setTremors(value);
            case "adhd" -> data.setAdhd(value);
            case "vertigo" -> data.setVertigo(value);
            case "scoliosis" -> data.setScoliosis(value);
            case "prosopagnosia" -> data.setProsopagnosia(value);
            case "schizophrenia" -> data.setSchizophrenia(value);
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