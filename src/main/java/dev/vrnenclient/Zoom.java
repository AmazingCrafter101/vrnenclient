package dev.vrnenclient;

import net.minecraft.client.Minecraft;

/**
 * Simple zoom: temporarily lowers the FOV option while the zoom key is held.
 * Limited to the vanilla FOV minimum (30). Deeper zoom needs a GameRenderer mixin.
 * The option is restored on release and never written to options.txt by this code.
 */
public final class Zoom {
    private static Integer saved;

    public static void tick(Minecraft mc, boolean held) {
        var fov = mc.options.fov();
        if (held && mc.screen == null) {
            if (saved == null) saved = fov.get();
            fov.set(Config.zoomFov);
        } else if (saved != null) {
            fov.set(saved);
            saved = null;
        }
    }

    private Zoom() {}
}
