package uk.da5h1n.nerfed.util;

import net.minecraft.client.gui.screens.Screen;
import net.minecraft.world.entity.animal.feline.Cat;
import uk.da5h1n.nerfed.util.DisabilityDataHelper.DisabilityData;

import java.util.function.BiConsumer;
import java.util.function.Predicate;

public enum Condition {
    LEFT_ARM("left_arm", Category.LIMBS, 1),
    RIGHT_ARM("right_arm", Category.LIMBS, 1),
    LEFT_LEG("left_leg", Category.LIMBS, 1),
    RIGHT_LEG("right_leg", Category.LIMBS, 1),

    TUNNEL_VISION("tunnel_vision", Category.VISION),
    MYOPIA("myopia", Category.VISION),
    NIGHT_BLINDNESS("night_blindness", Category.VISION),
    LIGHT_SENSITIVITY("light_sensitivity", Category.VISION),
    DOUBLE_VISION("double_vision", Category.VISION),
    BLINDNESS("blindness", Category.VISION),

    DEAFNESS("deafness", Category.HEARING),

    TREMORS("tremors", Category.NEUROLOGICAL),
    ADHD("adhd", Category.NEUROLOGICAL),
    VERTIGO("vertigo", Category.NEUROLOGICAL),
    PROSOPAGNOSIA("prosopagnosia", Category.NEUROLOGICAL),
    SCHIZOPHRENIA("schizophrenia", Category.NEUROLOGICAL),

    SCOLIOSIS("scoliosis", Category.PHYSICAL),

    MUTE("mute", Category.COMMUNICATION, 1);

    public static final int COLOURBLIND_MAX_LEVEL = 3;

    public enum Category {
        LIMBS, VISION, HEARING, COMMUNICATION, NEUROLOGICAL, PHYSICAL;

        public String langKey() { return "nerfed.category." + name().toLowerCase(); }
    }

    private final String id;
    private final Category category;
    private final int maxLevel;

    Condition(String id, Category category) {
        this(id, category, 3);
    }

    Condition(String id, Category category, int maxLevel) {
        this.id = id;
        this.category = category;
        this.maxLevel = maxLevel;
    }


    public String id() { return id; }
    public Category category() { return category; }
    public int maxLevel() { return maxLevel; }
    public boolean isBinary() { return maxLevel <= 1; }

    public String nameKey() { return "nerfed.condition." + id + ".name"; }
    public String descKey() { return "nerfed.condition." + id + ".desc"; }
    public String effectKey() { return "nerfed.condition." + id + ".effect"; }

    public static String levelKey(int level) { return "nerfed.level." + level; }

    public static Condition byId(String id) {
        for (Condition c : values()) {
            if (c.id.equals(id)) return c;
        }
        return null;
    }

    public static int maxLevelOf(String id) {
        if (id.startsWith("colourblind_")) return COLOURBLIND_MAX_LEVEL;
        Condition c = byId(id);
        return c != null ? c.maxLevel() : 3;
    }
}