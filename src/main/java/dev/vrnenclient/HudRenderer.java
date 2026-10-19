package dev.vrnenclient;

import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;

public final class HudRenderer {
    private static final EquipmentSlot[] ARMOR = {
            EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET
    };

    public static void render(GuiGraphics g, DeltaTracker delta) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.options.hideGui) return;
        if (mc.screen == null) CpsTracker.poll(mc.getWindow().getWindow());

        Font font = mc.font;
        int x = 4, y = 4;

        if (Config.showCps) {
            g.drawString(font, "CPS: " + CpsTracker.left() + " | " + CpsTracker.right(), x, y, 0xFFFFFFFF, true);
            y += 12;
        }

        if (Config.showArmor) {
            for (EquipmentSlot slot : ARMOR) {
                ItemStack s = mc.player.getItemBySlot(slot);
                if (s.isEmpty()) continue;
                g.renderItem(s, x, y);
                if (s.isDamageableItem()) {
                    int left = s.getMaxDamage() - s.getDamageValue();
                    g.drawString(font, String.valueOf(left), x + 20, y + 4, 0xFFFFFFFF, true);
                }
                y += 18;
            }
        }

        if (Config.showPotions) {
            for (MobEffectInstance e : mc.player.getActiveEffects()) {
                String name = e.getEffect().value().getDisplayName().getString();
                String time = e.isInfiniteDuration() ? "**:**" : formatTicks(e.getDuration());
                g.drawString(font, name + " " + (e.getAmplifier() + 1) + "  " + time, x, y, 0xFFFFFFFF, true);
                y += 10;
            }
            y += 2;
        }

        if (Config.showKeystrokes) {
            y += 4;
            var o = mc.options;
            key(g, font, x + 22, y, "W", o.keyUp.isDown());
            key(g, font, x, y + 22, "A", o.keyLeft.isDown());
            key(g, font, x + 22, y + 22, "S", o.keyDown.isDown());
            key(g, font, x + 44, y + 22, "D", o.keyRight.isDown());
        }
    }

    private static void key(GuiGraphics g, Font font, int x, int y, String label, boolean down) {
        g.fill(x, y, x + 20, y + 20, down ? 0x90FFFFFF : 0x90000000);
        int color = down ? 0xFF000000 : 0xFFFFFFFF;
        g.drawString(font, label, x + 10 - font.width(label) / 2, y + 6, color, false);
    }

    private static String formatTicks(int ticks) {
        int s = ticks / 20;
        return String.format("%d:%02d", s / 60, s % 60);
    }

    private HudRenderer() {}
}
