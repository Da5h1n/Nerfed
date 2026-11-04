package uk.da5h1n.nerfed.client;

import net.minecraft.resources.Identifier;
import uk.da5h1n.nerfed.Nerfed;

public class ColourblindRenderHandler {
    private static final Identifier PROTAN   = id("colourblind_protan");
    private static final Identifier DEUTERAN = id("colourblind_deuteran");
    private static final Identifier TRITAN   = id("colourblind_tritan");
    private static final Identifier MONO     = id("colourblind_mono");

    private static Identifier id(String path) {
        return Identifier.fromNamespaceAndPath(Nerfed.MOD_ID, path);
    }

    /** Returns the post effect for a mode, or null for none. */
    public static Identifier effectFor(String mode) {
        if (mode == null) return null;
        return switch (mode.toLowerCase()) {
            case "protan", "protanopia" -> PROTAN;
            case "deuteran", "deuteranopia" -> DEUTERAN;
            case "tritan", "tritanopia" -> TRITAN;
            case "mono", "monochromacy", "achromatopsia" -> MONO;
            default -> null;
        };
    }
}