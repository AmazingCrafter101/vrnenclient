package dev.vrnenclient;

import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;

public class SettingsScreen extends Screen {
    private static final ResourceLocation BACKGROUND =
            ResourceLocation.fromNamespaceAndPath("vrnenclient", "textures/gui/background.png");
    private static final ResourceLocation LOGO =
            ResourceLocation.fromNamespaceAndPath("vrnenclient", "textures/gui/icon.png");
    private static final int BG_W = 1280, BG_H = 720;
    private static final int LOGO_SIZE = 256;
    private static final int[] ZOOM_STEPS = {30, 40, 50, 60};
    private static final String[] TAB_NAMES = {"HUD", "Combat", "Gameplay"};

    // layout
    private static final int PW = 320, PH = 216;
    private static final int COL_W = 145, ROW_H = 20, ROW_STEP = 24, ROWS_PER_COL = 5;
    private static final int TAB_W = 80, TAB_H = 16;

    private record Entry(String label, BooleanSupplier get, Consumer<Boolean> set, boolean zoom) {
        static Entry toggle(String label, BooleanSupplier get, Consumer<Boolean> set) {
            return new Entry(label, get, set, false);
        }
        static Entry zoom(String label) {
            return new Entry(label, null, null, true);
        }
    }

    private final Screen parent;
    private final List<List<Entry>> tabs = new ArrayList<>();
    private final Map<Entry, Float> anim = new HashMap<>();
    private int tab = 0;

    public SettingsScreen(Screen parent) {
        super(Component.literal("Vrnen Client"));
        this.parent = parent;

        tabs.add(List.of(
                Entry.toggle("CPS counter", () -> Config.showCps, v -> Config.showCps = v),
                Entry.toggle("FPS", () -> Config.showFps, v -> Config.showFps = v),
                Entry.toggle("Ping", () -> Config.showPing, v -> Config.showPing = v),
                Entry.toggle("Coordinates", () -> Config.showCoords, v -> Config.showCoords = v),
                Entry.toggle("Sprint status", () -> Config.showSprint, v -> Config.showSprint = v),
                Entry.toggle("Keystrokes", () -> Config.showKeystrokes, v -> Config.showKeystrokes = v),
                Entry.toggle("Armor status", () -> Config.showArmor, v -> Config.showArmor = v),
                Entry.toggle("Potion status", () -> Config.showPotions, v -> Config.showPotions = v),
                Entry.toggle("Item counter", () -> Config.showItems, v -> Config.showItems = v)));

        tabs.add(List.of(
                Entry.toggle("Combo counter", () -> Config.showCombo, v -> Config.showCombo = v),
                Entry.toggle("Reach display", () -> Config.showReach, v -> Config.showReach = v),
                Entry.toggle("Target HUD", () -> Config.showTarget, v -> Config.showTarget = v)));

        tabs.add(List.of(
                Entry.toggle("Toggle sprint", () -> Config.toggleSprint, v -> Config.toggleSprint = v),
                Entry.toggle("Fullbright", () -> Config.fullbright, v -> Config.fullbright = v),
                Entry.zoom("Zoom FOV")));
    }

    // ---------- geometry ----------
    private int px() { return (width - PW) / 2; }
    private int py() { return (height - PH) / 2; }
    private int tabX(int t) { return px() + 10 + t * (TAB_W + 4); }
    private int tabY() { return py() + 40; }
    private int entryX(int i) { return px() + 10 + (i / ROWS_PER_COL) * 155; }
    private int entryY(int i) { return py() + 64 + (i % ROWS_PER_COL) * ROW_STEP; }
    private int doneX() { return px() + PW / 2 - 40; }
    private int doneY() { return py() + PH - 26; }

    private static boolean inside(double mx, double my, int x, int y, int w, int h) {
        return mx >= x && mx < x + w && my >= y && my < y + h;
    }

    // ---------- drawing ----------
    /** Our own artwork instead of the vanilla blurred background, cropped to cover the screen. */
    @Override
    public void renderBackground(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
        float screenAspect = (float) width / height;
        float texAspect = (float) BG_W / BG_H;
        int regionW = BG_W, regionH = BG_H;
        float u = 0, v = 0;
        if (screenAspect > texAspect) {
            regionH = Math.round(BG_W / screenAspect);
            v = (BG_H - regionH) / 2f;
        } else {
            regionW = Math.round(BG_H * screenAspect);
            u = (BG_W - regionW) / 2f;
        }
        g.blit(RenderType::guiTextured, BACKGROUND, 0, 0, u, v, width, height, regionW, regionH, BG_W, BG_H);
        g.fill(0, 0, width, height, 0x66000000);
    }

    @Override
    public void render(GuiGraphics g, int mx, int my, float partialTick) {
        super.render(g, mx, my, partialTick);

        int px = px(), py = py();
        Theme.rounded(g, px, py, PW, PH, 0xE00C0C10);
        g.fill(px + 1, py, px + PW - 1, py + 2, Theme.ACCENT);

        g.blit(RenderType::guiTextured, LOGO, px + 8, py + 8, 0f, 0f, 28, 28,
                LOGO_SIZE, LOGO_SIZE, LOGO_SIZE, LOGO_SIZE);
        g.drawString(font, Component.literal("VRNEN CLIENT").withStyle(ChatFormatting.BOLD),
                px + 42, py + 11, Theme.TEXT, true);
        g.drawString(font, "Minecraft 1.21.5", px + 42, py + 23, Theme.MUTED, true);

        // tabs
        for (int t = 0; t < TAB_NAMES.length; t++) {
            int x = tabX(t), y = tabY();
            boolean active = t == tab;
            boolean hover = inside(mx, my, x, y, TAB_W, TAB_H);
            Theme.rounded(g, x, y, TAB_W, TAB_H, active || hover ? Theme.PANEL_HOVER : Theme.ROW);
            if (active) g.fill(x, y + TAB_H - 2, x + TAB_W, y + TAB_H, Theme.ACCENT);
            String name = TAB_NAMES[t];
            g.drawString(font, name, x + (TAB_W - font.width(name)) / 2, y + 4,
                    active ? Theme.TEXT : Theme.MUTED, true);
        }

        // entries of the current tab
        List<Entry> list = tabs.get(tab);
        for (int i = 0; i < list.size(); i++) {
            drawEntry(g, list.get(i), entryX(i), entryY(i),
                    inside(mx, my, entryX(i), entryY(i), COL_W, ROW_H));
        }

        // done button
        boolean dh = inside(mx, my, doneX(), doneY(), 80, 18);
        Theme.rounded(g, doneX(), doneY(), 80, 18, dh ? Theme.ACCENT : Theme.ACCENT_DIM);
        String done = "Done";
        g.drawString(font, done, doneX() + (80 - font.width(done)) / 2, doneY() + 5, Theme.TEXT, true);
    }

    private void drawEntry(GuiGraphics g, Entry e, int x, int y, boolean hover) {
        Theme.rounded(g, x, y, COL_W, ROW_H, hover ? Theme.PANEL_HOVER : Theme.ROW);
        g.drawString(font, e.label(), x + 6, y + 6, Theme.TEXT, true);

        if (e.zoom()) {
            String zv = String.valueOf(Config.zoomFov);
            g.drawString(font, zv, x + COL_W - 8 - font.width(zv), y + 6, Theme.ACCENT, true);
            return;
        }

        boolean on = e.get().getAsBoolean();
        float a = anim.getOrDefault(e, on ? 1f : 0f);
        a += ((on ? 1f : 0f) - a) * 0.3f;
        anim.put(e, a);

        int sx = x + COL_W - 28, sy = y + 5;
        Theme.rounded(g, sx, sy, 22, 10, Theme.lerp(Theme.OFF, Theme.ACCENT, a));
        int kx = sx + 1 + Math.round(a * 12);
        Theme.rounded(g, kx, sy + 1, 8, 8, 0xFFFFFFFF);
    }

    // ---------- input ----------
    @Override
    public boolean mouseClicked(double mx, double my, int button) {
        if (button == 0) {
            for (int t = 0; t < TAB_NAMES.length; t++) {
                if (inside(mx, my, tabX(t), tabY(), TAB_W, TAB_H)) {
                    tab = t;
                    return true;
                }
            }
            List<Entry> list = tabs.get(tab);
            for (int i = 0; i < list.size(); i++) {
                if (inside(mx, my, entryX(i), entryY(i), COL_W, ROW_H)) {
                    click(list.get(i));
                    return true;
                }
            }
            if (inside(mx, my, doneX(), doneY(), 80, 18)) {
                onClose();
                return true;
            }
        }
        return super.mouseClicked(mx, my, button);
    }

    private static void click(Entry e) {
        if (e.zoom()) {
            int idx = 0;
            for (int k = 0; k < ZOOM_STEPS.length; k++) if (ZOOM_STEPS[k] == Config.zoomFov) idx = k;
            Config.zoomFov = ZOOM_STEPS[(idx + 1) % ZOOM_STEPS.length];
        } else {
            e.set().accept(!e.get().getAsBoolean());
        }
    }

    @Override
    public void onClose() {
        Config.save();
        minecraft.setScreen(parent);
    }
}
