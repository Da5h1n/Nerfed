package uk.da5h1n.nerfed.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.PostChain;
import net.minecraft.resources.Identifier;
import uk.da5h1n.nerfed.Nerfed;
import uk.da5h1n.nerfed.util.DisabilityDataHelper;

public class ColourblindShaderManager {
    public static final Identifier SHADER_ID = Identifier.fromNamespaceAndPath(Nerfed.MOD_ID, "shaders/post/colorblind.json");

    private static PostChain activePostChain = null;
    private static String currentMode = "none";

    public static int getModeInt(String mode) {
        return switch (mode.toLowerCase()) {
            case "protan" -> 1;
            case "deuteran" -> 2;
            case "tritan" -> 3;
            case "mono", "monochrome", "achromatopsia" -> 4;
            default -> 0;
        };
    }

    public static void updateShader(Minecraft client) {
        if (client.player == null) return;

        String mode = DisabilityDataHelper.getColorblindMode(client.player);
        int modeInt = getModeInt(mode);

        if (modeInt == 0) {
            currentMode = "none";
            activePostChain = null;
            return;
        }

        currentMode = mode;
        // In modern renderer pipelines, bind the active PostChain & update the 'Mode' uniform integer:
        // Example: client.gameRenderer.loadEffect(SHADER_ID);
    }

    public static String getCurrentMode() {
        return currentMode;
    }
}
