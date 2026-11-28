package uk.da5h1n.nerfed.client;

import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Player;
import uk.da5h1n.nerfed.Nerfed;
import uk.da5h1n.nerfed.util.Condition;
import uk.da5h1n.nerfed.util.DisabilityDataHelper;
import uk.da5h1n.nerfed.util.Strength;

import java.util.*;

public class ColourblindRenderHandler {

    private static final float[] LEVEL_STRENGTH = {0f, 0.4f, 0.7f, 1.0f};

    private static final float MIN_STRENGTH = 0.005f;

    private static final float[][] MYOPIA_RADIUS = {{0f, 0f}, {0.4f, 0.6f}, {0.7f, 1.2f}, {1f, 2.0f}};
    private static final float[][] TUNNEL_INNER = {{0f, 0.9f}, {0.4f, 0.35f}, {0.7f, 0.25f}, {1f, 0.15f}};
    private static final float[][] TUNNEL_OUTER = {{0f, 1.0f}, {0.4f, 0.75f}, {0.7f, 0.65f}, {1f, 0.6f}};

    private static final Identifier MYOPIA = id("myopia");
    private static final Identifier TUNNEL_VISION = id("tunnel_vision");
    private static final Identifier NIGHT_BLINDNESS = id("night_blindness");
    private static final Identifier LIGHT_SENSITIVITY = id("light_sensitivity");
    private static final Identifier DOUBLE_VISION = id("double_vision");
    private static final Identifier BLINDNESS = id("blindness");

    private static final Set<String> WORLD_ONLY = Set.of("night_blindness", "light_sensitivity", "blindness");

    // Insertion order is the order the filters are applied in
    private static final Map<String, Identifier> COLOURBLIND_EFFECTS = new LinkedHashMap<>();
    private static final Map<String, Integer> COLOURBLIND_MODE_VALUE = Map.of(
            "protan", 1, "deuteran", 2, "tritan", 3, "mono", 4);
    static {
        for (String mode : List.of("protan", "deuteran", "tritan", "mono")) {
            COLOURBLIND_EFFECTS.put(mode, id("colourblind_" + mode));
        }

        LiveUniforms.setSupplier(BLINDNESS, Echolocation::uniforms);
    }

    private static Identifier id(String path) {
        return Identifier.fromNamespaceAndPath(Nerfed.MOD_ID, path);
    }

    public static boolean isWorldOnly(Identifier effect) {
        return WORLD_ONLY.contains(effect.getPath());
    }

    private static float strengthOf(int level) {
        return LEVEL_STRENGTH[Math.max(0, Math.min(level, 3))];
    }

    private static float target(DisabilityDataHelper.DisabilityData data, Condition condition) {
        return strengthOf(data.getLevel(condition)) * NerfedSettings.get().scaleFor(condition);
    }

    private static float interp(float[][] knots, float s) {
        for (int i = 0; i < knots.length - 1; i++) {
            float x0 = knots[i][0], y0 = knots[i][1];
            float x1 = knots[i + 1][0], y1 = knots[i + 1][1];
            if (s <= x1) return y0 + (y1 - y0) * ((s - x0) / (x1 - x0));
        }
        return knots[knots.length - 1][1];
    }

    private static String canonicalMode(String mode) {
        if (mode == null) return "none";
        return switch (mode.toLowerCase()) {
            case "protan", "protanopia" -> "protan";
            case "deuteran", "deuteranopia" -> "deuteran";
            case "tritan", "tritanopia" -> "tritan";
            case "mono", "monochromacy", "achromatopsia" -> "mono";
            default -> "none";
        };
    }

    public static List<Identifier> effectsFor(Player player) {
        DisabilityDataHelper.DisabilityData data = DisabilityDataHelper.getData(player);
        List<Identifier> effects = new ArrayList<>();

        FilterFader.beginFrame();

        float night = FilterFader.fade("night_blindness", target(data, Condition.NIGHT_BLINDNESS));
        if (night > MIN_STRENGTH) {
            LiveUniforms.set(NIGHT_BLINDNESS, List.<Number>of(night));
            effects.add(NIGHT_BLINDNESS);
        }

        float glare = FilterFader.fade("light_sensitivity", target(data, Condition.LIGHT_SENSITIVITY));
        if (glare > MIN_STRENGTH) {
            LiveUniforms.set(LIGHT_SENSITIVITY, List.<Number>of(glare));
            effects.add(LIGHT_SENSITIVITY);
        }

        float blind = FilterFader.fade("blindness", target(data, Condition.BLINDNESS));
        if (blind > MIN_STRENGTH) {
            Echolocation.setShown(blind);
            effects.add(BLINDNESS);
        } else {
            Echolocation.setShown(0f);
        }

        float myopia = FilterFader.fade("myopia", target(data, Condition.MYOPIA));
        if (myopia > MIN_STRENGTH) {
            LiveUniforms.set(MYOPIA, List.<Number>of(interp(MYOPIA_RADIUS, myopia)));
            effects.add(MYOPIA);
        }

        float dbl = FilterFader.fade("double_vision", target(data, Condition.DOUBLE_VISION));
        if (dbl > MIN_STRENGTH) {

            double seconds = System.nanoTime() / 1.0e9;
            float offsetX = (float) ((0.004 + 0.012 * dbl) * (1.0 + 0.25 * Math.sin(seconds * 0.4)));
            float offsetY = (float) (0.003 * dbl * Math.sin(seconds * 0.3));
            LiveUniforms.set(DOUBLE_VISION, List.<Number>of(offsetX, offsetY, 0.5f * dbl));
            effects.add(DOUBLE_VISION);
        }

        float tunnel = FilterFader.fade("tunnel_vision", target(data, Condition.TUNNEL_VISION));
        if (tunnel > MIN_STRENGTH) {
            LiveUniforms.set(TUNNEL_VISION, List.<Number>of(
                    interp(TUNNEL_INNER, tunnel), interp(TUNNEL_OUTER, tunnel)));
            effects.add(TUNNEL_VISION);
        }

        // Colour filters last, so they tint the final image. Each mode fades on its own,
        // so switching modes fades one out while the other fades in.
        String active = canonicalMode(data.getColorblindMode());
        for (Map.Entry<String, Identifier> entry : COLOURBLIND_EFFECTS.entrySet()) {
            String mode = entry.getKey();
            float target = mode.equals(active) ? strengthOf(data.getColorblindLevel()) : 0f;
            float shown = FilterFader.fade("colourblind_" + mode, target);
            if (shown > MIN_STRENGTH) {
                LiveUniforms.set(entry.getValue(), List.<Number>of(COLOURBLIND_MODE_VALUE.get(mode), shown));
                effects.add(entry.getValue());
            }
        }

        FilterFader.endFrame();
        return effects;
    }
}