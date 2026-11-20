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

import java.util.*;

public class DisabilityDataHelper {

    public static final AttachmentType<DisabilityData> DISABILITY_DATA = AttachmentRegistry.<DisabilityData>builder()
            .persistent(DisabilityData.CODEC)
            .copyOnDeath()
            .buildAndRegister(Identifier.fromNamespaceAndPath(Nerfed.MOD_ID, "disability_data"));

    public static class DisabilityData {
        public static final Codec<DisabilityData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                Codec.unboundedMap(Codec.STRING, Codec.INT).fieldOf("levels").orElse(Map.of()).forGetter(DisabilityData::levelsById),
                Codec.STRING.fieldOf("colorblind_mode").orElse("none").forGetter(o -> o.colorblindMode),
                Codec.INT.fieldOf("colorblind_level").orElse(0).forGetter(o -> o.colorblindLevel)
        ).apply(instance, DisabilityData::fromCodec));

        private final Map<Condition, Integer> levels = new EnumMap<>(Condition.class);
        private String colorblindMode = "none";
        private int colorblindLevel = 0;

        private static DisabilityData fromCodec(Map<String, Integer> byId, String mode, int level) {
            DisabilityData data = new DisabilityData();
            data.applyFrom(byId, mode, level);
            return data;
        }

        public int getLevel(Condition c) { return levels.getOrDefault(c, 0); }

        public boolean has(Condition c) { return getLevel(c) > 0; }

        public void setLevel(Condition c, int level) {
            int clamped = Math.max(0, Math.min(level, c.maxLevel()));
            if (clamped == 0) levels.remove(c);
            else levels.put(c, clamped);
        }

        public String getColorblindMode() { return colorblindMode; }
        public int getColorblindLevel() { return colorblindLevel; }

        public void setColorblind(String mode, int level) {
            if (mode == null || mode.equalsIgnoreCase("none") || level <= 0) {
                colorblindMode = "none";
                colorblindLevel = 0;
            } else {
                colorblindMode = mode.toLowerCase(Locale.ROOT);
                colorblindLevel = Math.min(level, Condition.COLOURBLIND_MAX_LEVEL);
            }
        }

        public void setColorblindMode(String mode) {
            boolean off = mode == null || mode.equalsIgnoreCase("none");
            setColorblind(mode, off ? 0 : (colorblindLevel > 0 ? colorblindLevel : Condition.COLOURBLIND_MAX_LEVEL));
        }

        public  Map<String, Integer> levelsById() {
            Map<String, Integer> out = new LinkedHashMap<>();
            for (Condition c : Condition.values()) {
                int level = getLevel(c);
                if (level > 0) out.put(c.id(), level);
            }
            return out;
        }

        public Map<String, Integer> activeLevels() {
            Map<String, Integer> out = levelsById();
            if (colorblindLevel > 0 && !colorblindMode.equals("none")) {
                out.put("colourblind_" + colorblindMode, colorblindLevel);
            }
            return out;
        }

        public void applyFrom(Map<String, Integer> byId, String mode, int level) {
            levels.clear();
            for (Condition c : Condition.values()) {
                setLevel(c, byId.getOrDefault(c.id(), 0));
            }
            setColorblind(mode, level);
        }

        public boolean isLeftArmDisabled() { return has(Condition.LEFT_ARM); }
        public void setLeftArmDisabled(boolean v) { setLevel(Condition.LEFT_ARM, v ? Condition.LEFT_ARM.maxLevel() : 0); }
        public boolean isRightArmDisabled() { return has(Condition.RIGHT_ARM); }
        public void setRightArmDisabled(boolean v) { setLevel(Condition.RIGHT_ARM, v ? Condition.RIGHT_ARM.maxLevel() : 0); }
        public boolean isLeftLegDisabled() { return has(Condition.LEFT_LEG); }
        public void setLeftLegDisabled(boolean v) { setLevel(Condition.LEFT_LEG, v ? Condition.LEFT_LEG.maxLevel() : 0); }
        public boolean isRightLegDisabled() { return has(Condition.RIGHT_LEG); }
        public void setRightLegDisabled(boolean v) { setLevel(Condition.RIGHT_LEG, v ? Condition.RIGHT_LEG.maxLevel() : 0); }

        public boolean isTunnelVision() { return has(Condition.TUNNEL_VISION); }
        public void setTunnelVision(boolean v) { setLevel(Condition.TUNNEL_VISION, v ? Condition.TUNNEL_VISION.maxLevel() : 0); }
        public boolean isMyopia() { return has(Condition.MYOPIA); }
        public void setMyopia(boolean v) { setLevel(Condition.MYOPIA, v ? Condition.MYOPIA.maxLevel() : 0); }
        public boolean isTremors() { return has(Condition.TREMORS); }
        public void setTremors(boolean v) { setLevel(Condition.TREMORS, v ? Condition.TREMORS.maxLevel() : 0); }
        public boolean isAdhd() { return has(Condition.ADHD); }
        public void setAdhd(boolean v) { setLevel(Condition.ADHD, v ? Condition.ADHD.maxLevel() : 0); }
        public boolean isVertigo() { return has(Condition.VERTIGO); }
        public void setVertigo(boolean v) { setLevel(Condition.VERTIGO, v ? Condition.VERTIGO.maxLevel() : 0); }
        public boolean isScoliosis() { return has(Condition.SCOLIOSIS); }
        public void setScoliosis(boolean v) { setLevel(Condition.SCOLIOSIS, v ? Condition.SCOLIOSIS.maxLevel() : 0); }
        public boolean isProsopagnosia() { return has(Condition.PROSOPAGNOSIA); }
        public void setProsopagnosia(boolean v) { setLevel(Condition.PROSOPAGNOSIA, v ? Condition.PROSOPAGNOSIA.maxLevel() : 0); }
        public boolean isSchizophrenia() { return has(Condition.SCHIZOPHRENIA); }
        public void setSchizophrenia(boolean v) { setLevel(Condition.SCHIZOPHRENIA, v ? Condition.SCHIZOPHRENIA.maxLevel() : 0); }
    }


    public static DisabilityData getData(Player player) {
        return player.getAttachedOrCreate(DISABILITY_DATA, DisabilityData::new);
    }

    public static int getLevel(Player player, Condition c) { return getData(player).getLevel(c); }

    public static float getSeverity(Player player, Condition c) {
        return (float) getData(player).getLevel(c) / c.maxLevel();
    }

    public static float getColourblindSeverity(Player player) {
        return (float) getData(player).getColorblindLevel() / Condition.COLOURBLIND_MAX_LEVEL;
    }

    public static void setLevel(Player player, Condition c, int level) {
        DisabilityData data = getData(player);
        data.setLevel(c, level);
        player.setAttached(DISABILITY_DATA, data);
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
        Condition c = Condition.byId(target.toLowerCase(Locale.ROOT));
        if (c != null) setLevel(player, c, value ? c.maxLevel() : 0);
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