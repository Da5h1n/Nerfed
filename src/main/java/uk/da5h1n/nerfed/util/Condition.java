package uk.da5h1n.nerfed.util;

import java.util.EnumSet;
import java.util.List;
import java.util.Set;

public enum Condition {
    LEFT_ARM("left_arm", Category.LIMBS, 1),
    RIGHT_ARM("right_arm", Category.LIMBS, 1),
    LEFT_LEG("left_leg", Category.LIMBS, 1),
    RIGHT_LEG("right_leg", Category.LIMBS, 1),

    TUNNEL_VISION("tunnel_vision", Category.VISION),
    MYOPIA("myopia", Category.VISION),
    NIGHT_BLINDNESS("night_blindness", Category.VISION),

    LIGHT_SENSITIVITY("light_sensitivity", Category.VISION, Risk.PHOTOSENSITIVITY),

    DOUBLE_VISION("double_vision", Category.VISION, Risk.MOTION),

    BLINDNESS("blindness", Category.VISION, Risk.PHOTOSENSITIVITY),

    DEAFNESS("deafness", Category.HEARING),
    TINNITUS("tinnitus", Category.HEARING),

    TREMORS("tremors", Category.NEUROLOGICAL, Risk.MOTION),

    ADHD("adhd", Category.NEUROLOGICAL),
    VERTIGO("vertigo", Category.NEUROLOGICAL, Risk.MOTION),
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
    private final Set<Risk> risks;

    Condition(String id, Category category, Risk... risks) {
        this(id, category, 3, risks);
    }

    Condition(String id, Category category, int maxLevel, Risk... risks) {
        this.id = id;
        this.category = category;
        this.maxLevel = maxLevel;
        this.risks = risks.length == 0 ? EnumSet.noneOf(Risk.class) : EnumSet.copyOf(List.of(risks));
    }

    public enum Risk {
        /** Flashing, pulsing, strong brightness changes or high-contrast patterns. */
        PHOTOSENSITIVITY,
        /** Camera movement, shaking or swaying that can cause motion sickness. */
        MOTION;

        public String langKey() { return "nerfed.risk." + name().toLowerCase(); }
    }


    public String id() { return id; }
    public Category category() { return category; }
    public int maxLevel() { return maxLevel; }
    public boolean isBinary() { return maxLevel <= 1; }

    public boolean hasRisk(Risk risk) { return risks.contains(risk); }
    public Set<Risk> risks() { return java.util.Collections.unmodifiableSet(risks); }

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