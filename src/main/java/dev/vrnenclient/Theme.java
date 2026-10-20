package dev.vrnenclient;

import net.minecraft.client.gui.GuiGraphics;

/** Shared colours and tiny drawing helpers so everything looks consistent. */
public final class Theme {
    public static final int ACCENT = 0xFFE5192C;
    public static final int ACCENT_DIM = 0xFFB3121F;
    public static final int PANEL = 0x99101014;
    public static final int PANEL_HOVER = 0xB0262630;
    public static final int ROW = 0x80181820;
    public static final int TEXT = 0xFFFFFFFF;
    public static final int MUTED = 0xFFA8A8B0;
    public static final int OFF = 0xFF3A3A42;

    /** Filled rectangle with the four corner pixels cut off (small rounded look). */
    public static void rounded(GuiGraphics g, int x, int y, int w, int h, int color) {
        g.fill(x + 1, y, x + w - 1, y + h, color);
        g.fill(x, y + 1, x + 1, y + h - 1, color);
        g.fill(x + w - 1, y + 1, x + w, y + h - 1, color);
    }

    /** HUD panel: dark translucent box with a red accent bar on the left. */
    public static void panel(GuiGraphics g, int x, int y, int w, int h) {
        rounded(g, x, y, w, h, PANEL);
        g.fill(x, y + 1, x + 2, y + h - 1, ACCENT);
    }

    public static int lerp(int a, int b, float t) {
        t = Math.max(0f, Math.min(1f, t));
        int aa = (a >>> 24) & 255, ar = (a >>> 16) & 255, ag = (a >>> 8) & 255, ab = a & 255;
        int ba = (b >>> 24) & 255, br = (b >>> 16) & 255, bg = (b >>> 8) & 255, bb = b & 255;
        int ra = Math.round(aa + (ba - aa) * t);
        int rr = Math.round(ar + (br - ar) * t);
        int rg = Math.round(ag + (bg - ag) * t);
        int rb = Math.round(ab + (bb - ab) * t);
        return (ra << 24) | (rr << 16) | (rg << 8) | rb;
    }

    private Theme() {}
}
