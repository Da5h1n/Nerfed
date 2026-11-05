package uk.da5h1n.nerfed.client;

import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Player;
import uk.da5h1n.nerfed.Nerfed;
import uk.da5h1n.nerfed.util.DisabilityDataHelper;

import java.util.ArrayList;
import java.util.List;

public class ColourblindRenderHandler {
    private static final Identifier PROTAN   = id("colourblind_protan");
    private static final Identifier DEUTERAN = id("colourblind_deuteran");
    private static final Identifier TRITAN   = id("colourblind_tritan");
    private static final Identifier MONO     = id("colourblind_mono");
    private static final Identifier TUNNEL_VISION = id("tunnel_vision");
    private static final Identifier MYOPIA   = id("myopia");

    private static Identifier id(String path) {
        return Identifier.fromNamespaceAndPath(Nerfed.MOD_ID, path);
    }

    /** Returns the post effect for a mode, or null for none. */
    public static Identifier colourblindEffectFor(String mode) {
        if (mode == null) return null;
        return switch (mode.toLowerCase()) {
            case "protan", "protanopia" -> PROTAN;
            case "deuteran", "deuteranopia" -> DEUTERAN;
            case "tritan", "tritanopia" -> TRITAN;
            case "mono", "monochromacy", "achromatopsia" -> MONO;
            default -> null;
        };
    }

    public static List<Identifier> effectsFor(Player player) {
        List<Identifier> effects = new ArrayList<>();

        DisabilityDataHelper.DisabilityData data = DisabilityDataHelper.getData(player);

        if (data.isTunnelVision()) effects.add(TUNNEL_VISION);
        if (data.isMyopia()) effects.add(MYOPIA);

        Identifier colourblind = colourblindEffectFor(data.getColorblindMode());
        if (colourblind != null) effects.add(colourblind);

        return effects;
    }
}