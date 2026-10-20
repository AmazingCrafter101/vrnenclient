package dev.vrnenclient;

import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public final class HudRenderer {
    private static final EquipmentSlot[] ARMOR = {
            EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET
    };
    private static final int GAP = 4;
    private static final int KEY_OFF = 0x99101014;
    private static final int KEY_ON = 0xCCE5192C;
    /** Smooth press animation per key: W A S D Space LMB RMB. */
    private static final float[] KEY_ANIM = new float[7];

    public static void render(GuiGraphics g, DeltaTracker delta) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.options.hideGui) return;
        if (mc.screen == null) CpsTracker.poll(mc.getWindow().getWindow());

        Font font = mc.font;
        int x = 6, y = 6;

        if (Config.showCps) {
            y = textPanel(g, font, x, y, "CPS", CpsTracker.left() + " | " + CpsTracker.right());
        }
        if (Config.showCoords) {
            y = textPanel(g, font, x, y, "XYZ",
                    mc.player.getBlockX() + " " + mc.player.getBlockY() + " " + mc.player.getBlockZ());
        }
        if (Config.showArmor) y = armor(g, mc, font, x, y);
        if (Config.showPotions) y = potions(g, mc, font, x, y);
        if (Config.showKeystrokes) keystrokes(g, mc, font, x, y);
    }

    // ---------- single-line panel ----------
    private static int textPanel(GuiGraphics g, Font f, int x, int y, String label, String value) {
        int w = 10 + f.width(label) + 6 + f.width(value);
        int h = 14;
        Theme.panel(g, x, y, w, h);
        g.drawString(f, label, x + 6, y + 3, Theme.MUTED, true);
        g.drawString(f, value, x + 6 + f.width(label) + 6, y + 3, Theme.TEXT, true);
        return y + h + GAP;
    }

    // ---------- armor ----------
    private static int armor(GuiGraphics g, Minecraft mc, Font f, int x, int y) {
        List<ItemStack> pieces = new ArrayList<>();
        for (EquipmentSlot slot : ARMOR) {
            ItemStack s = mc.player.getItemBySlot(slot);
            if (!s.isEmpty()) pieces.add(s);
        }
        if (pieces.isEmpty()) return y;

        int textW = 0;
        for (ItemStack s : pieces) {
            if (s.isDamageableItem()) {
                textW = Math.max(textW, f.width(String.valueOf(s.getMaxDamage() - s.getDamageValue())));
            }
        }
        int w = 5 + 16 + 4 + textW + 6;
        int h = 4 + pieces.size() * 18;
        Theme.panel(g, x, y, w, h);

        int ry = y + 2;
        for (ItemStack s : pieces) {
            g.renderItem(s, x + 5, ry + 1);
            if (s.isDamageableItem()) {
                int max = s.getMaxDamage();
                int left = max - s.getDamageValue();
                g.drawString(f, String.valueOf(left), x + 5 + 16 + 4, ry + 5,
                        durabilityColor((float) left / Math.max(1, max)), true);
            }
            ry += 18;
        }
        return y + h + GAP;
    }

    private static int durabilityColor(float frac) {
        if (frac > 0.5f) return 0xFF55FF55;
        if (frac > 0.25f) return 0xFFFFFF55;
        return 0xFFFF5555;
    }

    // ---------- potions ----------
    private static int potions(GuiGraphics g, Minecraft mc, Font f, int x, int y) {
        Collection<MobEffectInstance> effects = mc.player.getActiveEffects();
        if (effects.isEmpty()) return y;

        List<String> names = new ArrayList<>();
        List<String> times = new ArrayList<>();
        List<Integer> timeColors = new ArrayList<>();
        int nameW = 0, timeW = 0;
        for (MobEffectInstance e : effects) {
            String name = e.getEffect().value().getDisplayName().getString() + " " + (e.getAmplifier() + 1);
            String time = e.isInfiniteDuration() ? "**:**" : formatTicks(e.getDuration());
            names.add(name);
            times.add(time);
            timeColors.add(!e.isInfiniteDuration() && e.getDuration() <= 200 ? 0xFFFF5555 : Theme.MUTED);
            nameW = Math.max(nameW, f.width(name));
            timeW = Math.max(timeW, f.width(time));
        }
        int w = 6 + nameW + 10 + timeW + 6;
        int h = 4 + names.size() * 12;
        Theme.panel(g, x, y, w, h);
        int ry = y + 4;
        for (int i = 0; i < names.size(); i++) {
            g.drawString(f, names.get(i), x + 6, ry, Theme.TEXT, true);
            g.drawString(f, times.get(i), x + 6 + nameW + 10, ry, timeColors.get(i), true);
            ry += 12;
        }
        return y + h + GAP;
    }

    // ---------- keystrokes ----------
    private static void keystrokes(GuiGraphics g, Minecraft mc, Font f, int x, int y) {
        var o = mc.options;
        boolean[] down = {
                o.keyUp.isDown(), o.keyLeft.isDown(), o.keyDown.isDown(), o.keyRight.isDown(),
                o.keyJump.isDown(), o.keyAttack.isDown(), o.keyUse.isDown()
        };
        for (int i = 0; i < KEY_ANIM.length; i++) {
            KEY_ANIM[i] += ((down[i] ? 1f : 0f) - KEY_ANIM[i]) * 0.35f;
        }

        int s = 22, gap = 2;
        key(g, f, x + s + gap, y, s, s, "W", KEY_ANIM[0]);
        int r1 = y + s + gap;
        key(g, f, x, r1, s, s, "A", KEY_ANIM[1]);
        key(g, f, x + s + gap, r1, s, s, "S", KEY_ANIM[2]);
        key(g, f, x + 2 * (s + gap), r1, s, s, "D", KEY_ANIM[3]);

        int r2 = r1 + s + gap;
        int total = 3 * s + 2 * gap;
        int mw = (total - gap) / 2;
        mouseKey(g, f, x, r2, mw, s, "LMB", CpsTracker.left(), KEY_ANIM[5]);
        mouseKey(g, f, x + mw + gap, r2, mw, s, "RMB", CpsTracker.right(), KEY_ANIM[6]);

        int r3 = r2 + s + gap;
        Theme.rounded(g, x, r3, total, 12, Theme.lerp(KEY_OFF, KEY_ON, KEY_ANIM[4]));
        g.fill(x + total / 2 - 12, r3 + 5, x + total / 2 + 12, r3 + 7,
                Theme.lerp(Theme.MUTED, Theme.TEXT, KEY_ANIM[4]));
    }

    private static void key(GuiGraphics g, Font f, int x, int y, int w, int h, String label, float p) {
        Theme.rounded(g, x, y, w, h, Theme.lerp(KEY_OFF, KEY_ON, p));
        g.drawString(f, label, x + (w - f.width(label)) / 2, y + (h - 8) / 2, Theme.TEXT, true);
    }

    private static void mouseKey(GuiGraphics g, Font f, int x, int y, int w, int h, String label, int cps, float p) {
        Theme.rounded(g, x, y, w, h, Theme.lerp(KEY_OFF, KEY_ON, p));
        g.drawString(f, label, x + (w - f.width(label)) / 2, y + 3, Theme.TEXT, true);
        String c = String.valueOf(cps);
        g.drawString(f, c, x + (w - f.width(c)) / 2, y + 12, Theme.lerp(Theme.MUTED, Theme.TEXT, p), true);
    }

    private static String formatTicks(int ticks) {
        int s = ticks / 20;
        return String.format("%d:%02d", s / 60, s % 60);
    }

    private HudRenderer() {}
}
