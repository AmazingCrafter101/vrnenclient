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

    // layout
    private static final int PW = 320, PH = 206;
    private static final int COL_W = 145, ROW_H = 20, ROW_STEP = 24;

    private record Toggle(String label, BooleanSupplier get, Consumer<Boolean> set) {}

    private final Screen parent;
    private final List<Toggle> left = new ArrayList<>();
    private final List<Toggle> right = new ArrayList<>();
    private final Map<Toggle, Float> anim = new HashMap<>();

    public SettingsScreen(Screen parent) {
        super(Component.literal("Vrnen Client"));
        this.parent = parent;
        left.add(new Toggle("CPS counter", () -> Config.showCps, v -> Config.showCps = v));
        left.add(new Toggle("Keystrokes", () -> Config.showKeystrokes, v -> Config.showKeystrokes = v));
        left.add(new Toggle("Armor status", () -> Config.showArmor, v -> Config.showArmor = v));
        left.add(new Toggle("Potion status", () -> Config.showPotions, v -> Config.showPotions = v));
        left.add(new Toggle("Coordinates", () -> Config.showCoords, v -> Config.showCoords = v));
        right.add(new Toggle("Toggle sprint", () -> Config.toggleSprint, v -> Config.toggleSprint = v));
        right.add(new Toggle("Fullbright", () -> Config.fullbright, v -> Config.fullbright = v));
    }

    // ---------- geometry ----------
    private int px() { return (width - PW) / 2; }
    private int py() { return (height - PH) / 2; }
    private int colLeftX() { return px() + 10; }
    private int colRightX() { return px() + 165; }
    private int rowY(int i) { return py() + 56 + i * ROW_STEP; }
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

        g.drawString(font, "HUD", colLeftX(), py + 42, Theme.ACCENT, true);
        g.drawString(font, "GAMEPLAY", colRightX(), py + 42, Theme.ACCENT, true);

        for (int i = 0; i < left.size(); i++) {
            int y = rowY(i);
            drawToggle(g, colLeftX(), y, left.get(i), inside(mx, my, colLeftX(), y, COL_W, ROW_H));
        }
        for (int i = 0; i < right.size(); i++) {
            int y = rowY(i);
            drawToggle(g, colRightX(), y, right.get(i), inside(mx, my, colRightX(), y, COL_W, ROW_H));
        }

        // zoom row (sits under the gameplay toggles)
        int zy = rowY(right.size());
        boolean zh = inside(mx, my, colRightX(), zy, COL_W, ROW_H);
        Theme.rounded(g, colRightX(), zy, COL_W, ROW_H, zh ? Theme.PANEL_HOVER : Theme.ROW);
        g.drawString(font, "Zoom FOV", colRightX() + 6, zy + 6, Theme.TEXT, true);
        String zv = String.valueOf(Config.zoomFov);
        g.drawString(font, zv, colRightX() + COL_W - 8 - font.width(zv), zy + 6, Theme.ACCENT, true);

        // done button
        boolean dh = inside(mx, my, doneX(), doneY(), 80, 18);
        Theme.rounded(g, doneX(), doneY(), 80, 18, dh ? Theme.ACCENT : Theme.ACCENT_DIM);
        String done = "Done";
        g.drawString(font, done, doneX() + (80 - font.width(done)) / 2, doneY() + 5, Theme.TEXT, true);
    }

    private void drawToggle(GuiGraphics g, int x, int y, Toggle t, boolean hover) {
        boolean on = t.get().getAsBoolean();
        float a = anim.getOrDefault(t, on ? 1f : 0f);
        a += ((on ? 1f : 0f) - a) * 0.3f;
        anim.put(t, a);

        Theme.rounded(g, x, y, COL_W, ROW_H, hover ? Theme.PANEL_HOVER : Theme.ROW);
        g.drawString(font, t.label(), x + 6, y + 6, Theme.TEXT, true);

        int sx = x + COL_W - 28, sy = y + 5;
        Theme.rounded(g, sx, sy, 22, 10, Theme.lerp(Theme.OFF, Theme.ACCENT, a));
        int kx = sx + 1 + Math.round(a * 12);
        Theme.rounded(g, kx, sy + 1, 8, 8, 0xFFFFFFFF);
    }

    // ---------- input ----------
    @Override
    public boolean mouseClicked(double mx, double my, int button) {
        if (button == 0) {
            for (int i = 0; i < left.size(); i++) {
                if (inside(mx, my, colLeftX(), rowY(i), COL_W, ROW_H)) {
                    flip(left.get(i));
                    return true;
                }
            }
            for (int i = 0; i < right.size(); i++) {
                if (inside(mx, my, colRightX(), rowY(i), COL_W, ROW_H)) {
                    flip(right.get(i));
                    return true;
                }
            }
            if (inside(mx, my, colRightX(), rowY(right.size()), COL_W, ROW_H)) {
                int idx = 0;
                for (int k = 0; k < ZOOM_STEPS.length; k++) if (ZOOM_STEPS[k] == Config.zoomFov) idx = k;
                Config.zoomFov = ZOOM_STEPS[(idx + 1) % ZOOM_STEPS.length];
                return true;
            }
            if (inside(mx, my, doneX(), doneY(), 80, 18)) {
                onClose();
                return true;
            }
        }
        return super.mouseClicked(mx, my, button);
    }

    private static void flip(Toggle t) {
        t.set().accept(!t.get().getAsBoolean());
    }

    @Override
    public void onClose() {
        Config.save();
        minecraft.setScreen(parent);
    }
}
