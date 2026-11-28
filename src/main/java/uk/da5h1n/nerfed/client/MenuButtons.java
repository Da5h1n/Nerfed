package uk.da5h1n.nerfed.client;

import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.fabricmc.fabric.api.client.screen.v1.Screens;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.PauseScreen;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.network.chat.Component;

public final class MenuButtons {
    private static final int BUTTON_W = 110;
    private static final int BUTTON_H = 20;
    private static final int MARGIN = 4;

    /** Stops the first-run screen from reopening if the title screen is re-initialised this session. */
    private static boolean firstRunShown = false;

    private MenuButtons() {}

    public static void register() {
        ScreenEvents.AFTER_INIT.register((client, screen, scaledWidth, scaledHeight) -> {
            boolean title = screen instanceof TitleScreen;
            boolean pause = screen instanceof PauseScreen;
            if (!title && !pause) return;

            // Top-left corner, clear of the vanilla button columns
            Button button = Button.builder(
                            Component.translatable("nerfed.settings.open"),
                            b -> client.gui.setScreen(new NerfedSettingsScreen(screen)))
                    .bounds(MARGIN, MARGIN, BUTTON_W, BUTTON_H)
                    .build();
            Screens.getWidgets(screen).add(button);

            // First launch: let the player choose accessibility options before any effect can show.
            // The screen marks the first run as done when it is closed.
            if (title && !firstRunShown && !NerfedSettings.get().isFirstRunDone()) {
                firstRunShown = true;
                client.execute(() -> client.gui.setScreen(new NerfedSettingsScreen(screen)));
            }
        });
    }
}