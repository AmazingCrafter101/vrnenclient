package dev.vrnenclient;

import com.mojang.blaze3d.platform.InputConstants;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;

/** Press the sprint key once to start sprinting, press again to stop. Keyboard-bound sprint only. */
public final class ToggleSprint {
    private static boolean toggled;
    private static boolean wasPhysicalDown;

    public static void tick(Minecraft mc) {
        if (!Config.toggleSprint || mc.player == null || mc.screen != null) {
            toggled = false;
            wasPhysicalDown = false;
            return;
        }
        KeyMapping sprint = mc.options.keySprint;
        InputConstants.Key bound = KeyBindingHelper.getBoundKeyOf(sprint);
        if (bound.getType() != InputConstants.Type.KEYSYM) return;

        // Read the real keyboard state, since we override the KeyMapping state below.
        boolean physical = InputConstants.isKeyDown(mc.getWindow().getWindow(), bound.getValue());
        if (physical && !wasPhysicalDown) toggled = !toggled;
        wasPhysicalDown = physical;

        sprint.setDown(toggled || physical);
    }

    private ToggleSprint() {}
}
