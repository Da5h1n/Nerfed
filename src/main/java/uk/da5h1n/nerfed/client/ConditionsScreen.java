package uk.da5h1n.nerfed.client;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.locale.Language;
import net.minecraft.network.chat.Component;
import uk.da5h1n.nerfed.util.Condition;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Environment(EnvType.CLIENT)
public class ConditionsScreen extends Screen {
    private static final int PANEL_BG = 0xE0101018;
    private static final int BORDER = 0xFF5A5A6E;
    private static final int CARD_BG = 0xFF1E1E2A;
    private static final int WHITE = 0xFFFFFFFF;
    private static final int GREY = 0xFFB0B0B8;
    private static final int EFFECT = 0xFFE0C060;
    private static final int HEADER = 0xFF8AB4F8;
    private static final int PIP_OFF = 0xFF3A3A4A;

    private record Entry(Condition.Category category, String id, int level) {}

    private final String playerName;
    private final boolean self;
    private final List<Entry> entries = new ArrayList<>();

    private int scroll = 0;
    private int contentHeight = 0;
    private int listHeight = 0;

    public ConditionsScreen(String playerName, boolean self, Map<String, Integer> conditions) {
        super(self
                ? Component.translatable("nerfed.screen.title.self")
                : Component.translatable("nerfed.screen.title.other", playerName));
        this.playerName = playerName;
        this.self = self;

        // Group by category, in the enum's order
        for (Condition.Category category : Condition.Category.values()) {
            for (Map.Entry<String, Integer> e : conditions.entrySet()) {
                if (categoryOf(e.getKey()) == category) {
                    entries.add(new Entry(category, e.getKey(), e.getValue()));
                }
            }
        }
    }

    private static Condition.Category categoryOf(String id) {
        if (id.startsWith("colourblind_")) return Condition.Category.VISION;
        Condition c = Condition.byId(id);
        return c != null ? c.category() : Condition.Category.PHYSICAL;
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

    //? if >26.3 {
    @Override
    public void extractRenderState(GuiGraphicsExtractor g) {
        super.extractRenderState(g);
        renderContents(g);
    }
    //?} else {
    /*@Override
    public void extractRenderState(GuiGraphicsExtractor g, int mouseX, int mouseY, float a) {
        super.extractRenderState(g, mouseX, mouseY, a);
        renderContents(g);
    }
    *///?}

    private void renderContents(GuiGraphicsExtractor g) {
        int panelW = Math.min(this.width - 20, 320);
        int panelH = Math.min(this.height - 20, 260);
        int px = (this.width - panelW) / 2;
        int py = (this.height - panelH) / 2;

        // Panel and border
        g.fill(px, py, px + panelW, py + panelH, PANEL_BG);
        g.fill(px, py, px + panelW, py + 1, BORDER);
        g.fill(px, py + panelH - 1, px + panelW, py + panelH, BORDER);
        g.fill(px, py, px + 1, py + panelH, BORDER);
        g.fill(px + panelW - 1, py, px + panelW, py + panelH, BORDER);

        // Title and divider
        g.text(this.font, this.title, px + (panelW - this.font.width(this.title)) / 2, py + 8, WHITE, true);
        g.fill(px + 8, py + 22, px + panelW - 8, py + 23, BORDER);

        // List area
        int lx = px + 8;
        int ly = py + 28;
        int lw = panelW - 16;
        listHeight = panelH - 36;

        if (entries.isEmpty()) {
            Component empty = Component.translatable("nerfed.screen.empty");
            g.text(this.font, empty, px + (panelW - this.font.width(empty)) / 2, ly + 20, GREY, false);
            return;
        }

        int maxScroll = Math.max(0, contentHeight - listHeight);
        scroll = Math.max(0, Math.min(scroll, maxScroll));

        g.enableScissor(lx, ly, lx + lw, ly + listHeight);
        contentHeight = drawContent(g, lx, ly - scroll, lw - 8);
        g.disableScissor();

        // Scrollbar
        if (contentHeight > listHeight) {
            int trackX = lx + lw - 4;
            g.fill(trackX, ly, trackX + 3, ly + listHeight, 0x40FFFFFF);
            int thumbH = Math.max(16, listHeight * listHeight / contentHeight);
            int thumbY = ly + (int) ((long) (listHeight - thumbH) * scroll / (contentHeight - listHeight));
            g.fill(trackX, thumbY, trackX + 3, thumbY + thumbH, 0xFFC0C0C0);
        }
    }

    /** Draws all headings and cards starting at y. Returns the total height used. */
    private int drawContent(GuiGraphicsExtractor g, int x, int y, int w) {
        int lh = this.font.lineHeight;
        int cy = y;
        Condition.Category last = null;

        for (Entry e : entries) {
            if (e.category() != last) {
                last = e.category();
                if (cy != y) cy += 6;
                Component heading = Component.translatable(e.category().langKey()).withStyle(ChatFormatting.BOLD);
                g.text(this.font, heading, x + 2, cy, HEADER, false);
                cy += lh + 4;
            }

            String base = "nerfed.condition." + e.id();
            Component name = Component.translatable(base + ".name").withStyle(ChatFormatting.BOLD);
            Component desc = Component.translatable(base + ".desc");
            boolean hasEffect = Language.getInstance().has(base + ".effect");
            int max = Condition.maxLevelOf(e.id());
            int accent = accent(e.category());

            int textW = w - 14;
            var descLines = this.font.split(desc, textW);
            var effectLines = hasEffect
                    ? this.font.split(Component.translatable("nerfed.screen.effect",
                    Component.translatable(base + ".effect")), textW)
                    : List.<net.minecraft.util.FormattedCharSequence>of();

            int cardH = 6 + lh + 3 + descLines.size() * lh
                    + (hasEffect ? 3 + effectLines.size() * lh : 0) + 6;

            // Card background and category accent bar
            g.fill(x, cy, x + w, cy + cardH, CARD_BG);
            g.fill(x, cy, x + 3, cy + cardH, accent);

            int ty = cy + 6;
            g.text(this.font, name, x + 8, ty, WHITE, false);

            // Level: a name after the title and one square per level on the right
            if (max > 1) {
                String levelKey = Condition.levelKey(e.level());
                if (Language.getInstance().has(levelKey)) {
                    Component levelName = Component.translatable(levelKey);
                    g.text(this.font, levelName, x + 8 + this.font.width(name) + 6, ty, accent, false);
                }
                for (int i = 1; i <= max; i++) {
                    int pipX = x + w - 8 - (max - i + 1) * 8 + 2;
                    g.fill(pipX, ty + 1, pipX + 6, ty + 7, i <= e.level() ? accent : PIP_OFF);
                }
            }
            ty += lh + 3;

            for (var line : descLines) {
                g.text(this.font, line, x + 8, ty, GREY, false);
                ty += lh;
            }
            if (hasEffect) {
                ty += 3;
                for (var line : effectLines) {
                    g.text(this.font, line, x + 8, ty, EFFECT, false);
                    ty += lh;
                }
            }

            cy += cardH + 4;
        }
        return cy - y;
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        int maxScroll = Math.max(0, contentHeight - listHeight);
        scroll = Math.max(0, Math.min(scroll - (int) (scrollY * 12), maxScroll));
        return true;
    }

    @Override
    public boolean isPauseScreen() { return false; }

    @Override
    public boolean isInGameUi() { return true; }
}