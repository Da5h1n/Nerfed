package uk.da5h1n.nerfed.client;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import uk.da5h1n.nerfed.client.NerfedSettings.RenderMode;
import uk.da5h1n.nerfed.util.Condition;

import java.util.ArrayList;
import java.util.List;

/**
 * Accessibility settings: master limits for risky effect types, plus a render mode per effect.
 * Opened by the keybind or from Mod Menu. Works without Mod Menu installed.
 */
@Environment(EnvType.CLIENT)
public class NerfedSettingsScreen extends Screen {
    private static final int BORDER = 0xFF5A5A6E;
    private static final int CARD_BG = 0xFF1E1E2A;
    private static final int WHITE = 0xFFFFFFFF;
    private static final int GREY = 0xFFB0B0B8;
    private static final int HEADER = 0xFF8AB4F8;

    private static final int ROW_H = 24;
    private static final int BTN_H = 20;
    private static final int BTN_W = 84;

    private record Row(Condition condition, Button button) {}

    private final Screen parent;
    private final List<Row> rows = new ArrayList<>();

    private Button photoButton;
    private Button motionButton;

    private int scroll = 0;
    private int px, py, panelW, panelH, listTop, listBottom, buttonX;

    public NerfedSettingsScreen(Screen parent) {
        super(Component.translatable("nerfed.settings.title"));
        this.parent = parent;
    }

    /** Limbs and mute have no visual or audio effect to soften. */
    private static boolean isAdjustable(Condition c) {
        return c.category() != Condition.Category.LIMBS
                && c.category() != Condition.Category.COMMUNICATION;
    }

    private static int accent(Condition.Category category) {
        return switch (category) {
            case LIMBS -> 0xFFE06C6C;
            case VISION -> 0xFF6CA8E0;
            case HEARING -> 0xFF6CE0A8;
            case COMMUNICATION -> 0xFFE06CC0;
            case NEUROLOGICAL -> 0xFFB48CE0;
            case PHYSICAL -> 0xFFE0B46C;
        };
    }

    @Override
    protected void init() {
        rows.clear();

        panelW = Math.min(this.width - 20, 320);
        panelH = Math.min(this.height - 20, 260);
        px = (this.width - panelW) / 2;
        py = (this.height - panelH) / 2;
        listTop = py + 86;
        listBottom = py + panelH - 34;
        buttonX = px + panelW - 8 - 6 - BTN_W;

        int bx = px + 8;
        int bw = panelW - 16;

        photoButton = this.addRenderableWidget(Button.builder(Component.empty(), b -> {
            NerfedSettings s = NerfedSettings.get();
            s.setPhotosensitivityLimit(s.getPhotosensitivityLimit().next());
            s.save();
            refresh();
        }).bounds(bx, py + 28, bw, BTN_H).build());

        motionButton = this.addRenderableWidget(Button.builder(Component.empty(), b -> {
            NerfedSettings s = NerfedSettings.get();
            s.setMotionLimit(s.getMotionLimit().next());
            s.save();
            refresh();
        }).bounds(bx, py + 50, bw, BTN_H).build());

        for (Condition condition : Condition.values()) {
            if (!isAdjustable(condition)) continue;
            Button button = this.addRenderableWidget(Button.builder(Component.empty(), b -> {
                NerfedSettings s = NerfedSettings.get();
                s.setEffectMode(condition, s.getEffectMode(condition).next());
                s.save();
                refresh();
            }).bounds(buttonX, 0, BTN_W, BTN_H).build());
            rows.add(new Row(condition, button));
        }

        this.addRenderableWidget(Button.builder(Component.translatable("gui.done"), b -> onClose())
                .bounds(px + panelW / 2 - 50, py + panelH - 28, 100, BTN_H).build());

        refresh();
    }

    private static Component settingLabel(String key, RenderMode mode) {
        return Component.translatable(key).append(": ").append(Component.translatable(mode.langKey()));
    }

    private int maxScroll() {
        return Math.max(0, rows.size() * ROW_H - (listBottom - listTop));
    }

    /** Updates every label and repositions the list buttons for the current scroll. */
    private void refresh() {
        NerfedSettings s = NerfedSettings.get();
        photoButton.setMessage(settingLabel("nerfed.settings.photosensitivity", s.getPhotosensitivityLimit()));
        motionButton.setMessage(settingLabel("nerfed.settings.motion", s.getMotionLimit()));

        scroll = Math.max(0, Math.min(scroll, maxScroll()));

        for (int i = 0; i < rows.size(); i++) {
            Row row = rows.get(i);
            int y = listTop + i * ROW_H - scroll;

            RenderMode chosen = s.getEffectMode(row.condition());
            RenderMode effective = s.effectiveMode(row.condition());
            Component label = Component.translatable(chosen.langKey());
            if (effective != chosen) label = label.copy().append(" *");
            row.button().setMessage(label);

            row.button().setY(y + (ROW_H - BTN_H) / 2);
            // Only rows fully inside the list area are shown, so nothing draws over the header or footer
            boolean isRowVisible = y >= listTop && y + ROW_H <= listBottom;
            //? if <=26.3 {
                        /*row.button().visible = isRowVisible;
            *///?} else {
                        row.button().setVisible(isRowVisible);
            //?}
        }
    }

    //? if >26.3 {
    @Override
    public void extractRenderState(GuiGraphicsExtractor g) {
        super.extractRenderState(g);
        drawLabels(g);
    }
    //?} else {
    /*@Override
    public void extractRenderState(GuiGraphicsExtractor g, int mouseX, int mouseY, float a) {
        super.extractRenderState(g, mouseX, mouseY, a);
        drawLabels(g);
    }
    *///?}

    /** Drawn after the buttons, so it only touches areas the buttons don't cover. */
    private void drawLabels(GuiGraphicsExtractor g) {
        // Title and dividers
        g.text(this.font, this.title, px + (panelW - this.font.width(this.title)) / 2, py + 8, WHITE, true);
        g.fill(px + 8, py + 22, px + panelW - 8, py + 23, BORDER);
        g.fill(px + 8, py + 77, px + panelW - 8, py + 78, BORDER);

        // Per-effect list: name cards on the left, buttons (already drawn) on the right
        NerfedSettings s = NerfedSettings.get();
        boolean anyLimited = false;

        g.enableScissor(px + 8, listTop, px + panelW - 8, listBottom);
        for (int i = 0; i < rows.size(); i++) {
            Row row = rows.get(i);
            Condition c = row.condition();
            int y = listTop + i * ROW_H - scroll;
            if (y + ROW_H < listTop || y > listBottom) continue;

            g.fill(px + 8, y + 1, buttonX - 4, y + ROW_H - 1, CARD_BG);
            g.fill(px + 8, y + 1, px + 11, y + ROW_H - 1, accent(c.category()));

            Component name = Component.translatable(c.nameKey());
            g.text(this.font, name, px + 16, y + (ROW_H - this.font.lineHeight) / 2, WHITE, false);

            if (s.effectiveMode(c) != s.getEffectMode(c)) anyLimited = true;
        }
        g.disableScissor();

        // Scrollbar
        int listH = listBottom - listTop;
        int total = rows.size() * ROW_H;
        if (total > listH) {
            int trackX = px + panelW - 8 - 3;
            g.fill(trackX, listTop, trackX + 3, listBottom, 0x40FFFFFF);
            int thumbH = Math.max(16, listH * listH / total);
            int thumbY = listTop + (int) ((long) (listH - thumbH) * scroll / (total - listH));
            g.fill(trackX, thumbY, trackX + 3, thumbY + thumbH, 0xFFC0C0C0);
        }

        // Legend, only when a safe-mode limit is overriding a per-effect setting
        if (anyLimited) {
            g.text(this.font, Component.translatable("nerfed.settings.limited"),
                    px + 8, py + panelH - 22, GREY, false);
        }
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        scroll = Math.max(0, Math.min(scroll - (int) (scrollY * 12), maxScroll()));
        refresh();
        return true;
    }

    @Override
    public void onClose() {
        NerfedSettings s = NerfedSettings.get();
        s.setFirstRunDone(true);
        s.save();
        Minecraft.getInstance().gui.setScreen(parent);
    }
}