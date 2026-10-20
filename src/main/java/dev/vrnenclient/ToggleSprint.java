package dev.vrnenclient;

import com.mojang.blaze3d.platform.InputConstants;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import org.lwjgl.glfw.GLFW;

/**
 * Toggle sprint. While the feature is ON you sprint automatically whenever you walk forward
 * (no need to hold anything). Press your sprint key once to switch auto-sprint off, and again
 * to switch it back on. Holding the sprint key still sprints normally.
 * Keep Options > Controls > Sprint on "Hold" (the default) so this doesn't fight vanilla toggle.
 */
public final class ToggleSprint {
    private static boolean auto = true;
    private static boolean wasPhysicalDown;

    public static String statusText() {
        if (!Config.toggleSprint) return "Hold";
        return auto ? "Toggled" : "Off";
    }

    public static void tick(Minecraft mc) {
        if (!Config.toggleSprint || mc.player == null) {
            wasPhysicalDown = false;
            return;
        }
        if (mc.screen != null) return; // leave everything alone while a menu is open

        KeyMapping sprint = mc.options.keySprint;
        InputConstants.Key bound = KeyBindingHelper.getBoundKeyOf(sprint);
        long window = mc.getWindow().getWindow();

        // Read the real key state, because we override the KeyMapping state below.
        boolean physical;
        if (bound.getType() == InputConstants.Type.MOUSE) {
            physical = GLFW.glfwGetMouseButton(window, bound.getValue()) == GLFW.GLFW_PRESS;
        } else {
            physical = InputConstants.isKeyDown(window, bound.getValue());
        }

        if (physical && !wasPhysicalDown) auto = !auto;
        wasPhysicalDown = physical;

        sprint.setDown(auto || physical);
    }

    private ToggleSprint() {}
}
