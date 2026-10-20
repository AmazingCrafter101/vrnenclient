package dev.vrnenclient;

import com.mojang.blaze3d.platform.InputConstants;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.fabricmc.fabric.api.event.player.AttackEntityCallback;
import net.minecraft.client.KeyMapping;
import net.minecraft.world.InteractionResult;
import org.lwjgl.glfw.GLFW;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class VrnenClient implements ClientModInitializer {
    public static final Logger LOGGER = LoggerFactory.getLogger("vrnenclient");

    private static KeyMapping menuKey;
    private static KeyMapping zoomKey;

    @Override
    public void onInitializeClient() {
        Config.load();

        // String category is correct for 1.21.5.
        menuKey = KeyBindingHelper.registerKeyBinding(new KeyMapping(
                "key.vrnenclient.menu", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_RIGHT_SHIFT, "category.vrnenclient"));
        zoomKey = KeyBindingHelper.registerKeyBinding(new KeyMapping(
                "key.vrnenclient.zoom", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_C, "category.vrnenclient"));

        HudRenderCallback.EVENT.register(HudRenderer::render);

        AttackEntityCallback.EVENT.register((player, level, hand, entity, hit) -> {
            Combat.onAttack(player, entity);
            return InteractionResult.PASS;
        });

        ClientTickEvents.END_CLIENT_TICK.register(mc -> {
            while (menuKey.consumeClick()) {
                mc.setScreen(new SettingsScreen(null));
            }
            ToggleSprint.tick(mc);
            Combat.tick(mc);
            Zoom.tick(mc, zoomKey.isDown());
            Fullbright.tick(mc);
        });
    }
}
