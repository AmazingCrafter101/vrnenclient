package dev.vrnenclient;

import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public final class HudRenderer {
    private static final EquipmentSlot[] ARMOR = {
            EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET
    };
    private static final int GAP = 4;
    private static final int MARGIN = 6;
    private static final int LEFT = 0, RIGHT = 1, CENTER = 2;
    private static final int KEY_OFF = 0x99101014;
    private static final int KEY_ON = 0xCCE5192C;

    /** Smooth press animation per key: W A S D Space LMB RMB. */
    private static final float[] KEY_ANIM = new float[7];

    // our own FPS counter (counts rendered HUD frames per second)
    private static long fpsStart = System.currentTimeMillis();
    private static int fpsFrames, fps;

    public static void render(GuiGraphics g, DeltaTracker delta) {
        Minecraft mc = Minecraft.getInstance();

        long now = System.currentTimeMillis();
        fpsFrames++;
        if (now - fpsStart >= 1000) {
            fps = fpsFrames;
            fpsFrames = 0;
            fpsStart = now;
        }

        if (mc.player == null || mc.options.hideGui) return;
        if (mc.screen == null) CpsTracker.poll(mc.getWindow().getWindow());

        Font f = mc.font;
        int sw = mc.getWindow().getGuiScaledWidth();
        int sh = mc.getWindow().getGuiScaledHeight();

        // ----- left column -----
        int y = MARGIN;
        if (Config.showFps) y = textPanel(g, f, LEFT, sw, y, "FPS", String.valueOf(fps), Theme.TEXT);
        if (Config.showPing) {
            int ping = ping(mc);
            y = textPanel(g, f, LEFT, sw, y, "PING", ping < 0 ? "--" : ping + " ms", pingColor(ping));
        }
        if (Config.showCps) {
            y = textPanel(g, f, LEFT, sw, y, "CPS", CpsTracker.left() + " | " + CpsTracker.right(), Theme.TEXT);
        }
        if (Config.showSprint) {
            y = textPanel(g, f, LEFT, sw, y, "SPRINT", ToggleSprint.statusText(), Theme.TEXT);
        }
        if (Config.showCoords) {
            y = textPanel(g, f, LEFT, sw, y, "XYZ",
                    mc.player.getBlockX() + " " + mc.player.getBlockY() + " " + mc.player.getBlockZ(), Theme.TEXT);
        }
        if (Config.showKeystrokes) keystrokes(g, mc, f, MARGIN, y);

        // ----- right column -----
        int ry = MARGIN;
        if (Config.showArmor) ry = armor(g, mc, f, sw, ry);
        if (Config.showItems) ry = items(g, mc, f, sw, ry);
        if (Config.showPotions) potions(g, mc, f, sw, ry);

        // ----- under the crosshair -----
        int cy = sh / 2 + 14;
        if (Config.showCombo && Combat.combo() > 0) {
            cy = textPanel(g, f, CENTER, sw, cy, "COMBO", String.valueOf(Combat.combo()), Theme.TEXT);
        }
        if (Config.showReach && Combat.reachFresh()) {
            textPanel(g, f, CENTER, sw, cy, "REACH", String.format("%.2f", Combat.reach()), Theme.TEXT);
        }

        // ----- top centre -----
        if (Config.showTarget) targetHud(g, f, sw, MARGIN);
    }

    private static int px(int align, int sw, int w) {
        return switch (align) {
            case RIGHT -> sw - MARGIN - w;
            case CENTER -> (sw - w) / 2;
            default -> MARGIN;
        };
    }

    // ---------- single-line panel ----------
    private static int textPanel(GuiGraphics g, Font f, int align, int sw, int y,
                                 String label, String value, int valueColor) {
        int w = 10 + f.width(label) + 6 + f.width(value);
        int h = 14;
        int x = px(align, sw, w);
        Theme.panel(g, x, y, w, h);
        g.drawString(f, label, x + 6, y + 3, Theme.MUTED, true);
        g.drawString(f, value, x + 6 + f.width(label) + 6, y + 3, valueColor, true);
        return y + h + GAP;
    }

    private static int ping(Minecraft mc) {
        ClientPacketListener conn = mc.getConnection();
        if (conn == null) return -1;
        PlayerInfo info = conn.getPlayerInfo(mc.player.getUUID());
        return info == null ? -1 : info.getLatency();
    }

    private static int pingColor(int ping) {
        if (ping < 0) return Theme.MUTED;
        if (ping < 80) return 0xFF55FF55;
        if (ping < 150) return 0xFFFFFF55;
        return 0xFFFF5555;
    }

    // ---------- armor ----------
    private static int armor(GuiGraphics g, Minecraft mc, Font f, int sw, int y) {
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
        int x = px(RIGHT, sw, w);
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

    // ---------- item counter (pearls, gapples, splash potions, totems) ----------
    private static int items(GuiGraphics g, Minecraft mc, Font f, int sw, int y) {
        Item[] counted = {Items.ENDER_PEARL, Items.GOLDEN_APPLE, Items.SPLASH_POTION, Items.TOTEM_OF_UNDYING};
        Inventory inv = mc.player.getInventory();

        List<ItemStack> icons = new ArrayList<>();
        List<String> counts = new ArrayList<>();
        int textW = 0;
        for (Item item : counted) {
            int total = 0;
            for (int i = 0; i < inv.getContainerSize(); i++) {
                ItemStack s = inv.getItem(i);
                if (s.is(item)) total += s.getCount();
            }
            if (total > 0) {
                icons.add(new ItemStack(item));
                String t = "x" + total;
                counts.add(t);
                textW = Math.max(textW, f.width(t));
            }
        }
        if (icons.isEmpty()) return y;

        int w = 5 + 16 + 4 + textW + 6;
        int h = 4 + icons.size() * 18;
        int x = px(RIGHT, sw, w);
        Theme.panel(g, x, y, w, h);
        int ry = y + 2;
        for (int i = 0; i < icons.size(); i++) {
            g.renderItem(icons.get(i), x + 5, ry + 1);
            g.drawString(f, counts.get(i), x + 5 + 16 + 4, ry + 5, Theme.TEXT, true);
            ry += 18;
        }
        return y + h + GAP;
    }

    // ---------- potions ----------
    private static int potions(GuiGraphics g, Minecraft mc, Font f, int sw, int y) {
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
        int x = px(RIGHT, sw, w);
        Theme.panel(g, x, y, w, h);
        int ry = y + 4;
        for (int i = 0; i < names.size(); i++) {
            g.drawString(f, names.get(i), x + 6, ry, Theme.TEXT, true);
            g.drawString(f, times.get(i), x + 6 + nameW + 10, ry, timeColors.get(i), true);
            ry += 12;
        }
        return y + h + GAP;
    }

    // ---------- target HUD ----------
    private static void targetHud(GuiGraphics g, Font f, int sw, int y) {
        LivingEntity t = Combat.target();
        if (t == null) return;
        String name = t.getName().getString();
        float hp = t.getHealth();
        float max = Math.max(1f, t.getMaxHealth());
        String hpText = String.format("%.1f", hp);

        int w = Math.max(110, f.width(name) + f.width(hpText) + 24);
        int h = 26;
        int x = (sw - w) / 2;
        Theme.panel(g, x, y, w, h);
        g.drawString(f, name, x + 6, y + 3, Theme.TEXT, true);
        g.drawString(f, hpText, x + w - 6 - f.width(hpText), y + 3, Theme.MUTED, true);

        float frac = Math.max(0f, Math.min(1f, hp / max));
        int bx = x + 6, by = y + 16, bw = w - 12;
        g.fill(bx, by, bx + bw, by + 5, 0xFF2A2A30);
        g.fill(bx, by, bx + Math.round(bw * frac), by + 5, Theme.lerp(0xFFE5192C, 0xFF55FF55, frac));
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
