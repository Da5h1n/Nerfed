package uk.da5h1n.nerfed.client;

import com.mojang.blaze3d.platform.InputConstants;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.Identifier;
import uk.da5h1n.nerfed.Nerfed;

public final class SettingsKeybind {
    private static KeyMapping open;

    private SettingsKeybind() {}

    public static void register() {
        KeyMapping.Category category = KeyMapping.Category.register(
                Identifier.fromNamespaceAndPath(Nerfed.MOD_ID, "main"));

        open = KeyMappingHelper.registerKeyMapping(new KeyMapping(
                "key.nerfed.open_settings",
                InputConstants.UNKNOWN.getValue(),
                category));

        ClientTickEvents.END_CLIENT_TICK.register(SettingsKeybind::tick);
    }

    private static void tick(Minecraft client) {
        while (open.consumeClick()) {
            client.gui.setScreen(new NerfedSettingsScreen(null));
        }
    }
}