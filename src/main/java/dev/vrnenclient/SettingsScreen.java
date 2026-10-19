package dev.vrnenclient;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

import java.util.function.BooleanSupplier;
import java.util.function.Consumer;

public class SettingsScreen extends Screen {
    private static final int[] ZOOM_STEPS = {30, 40, 50, 60};
    private static final ResourceLocation BACKGROUND =
            ResourceLocation.fromNamespaceAndPath("vrnenclient", "textures/gui/background.png");
    private static final ResourceLocation LOGO =
            ResourceLocation.fromNamespaceAndPath("vrnenclient", "textures/gui/icon.png");
    private static final int BG_W = 1280, BG_H = 720;
    private static final int LOGO_SIZE = 256;

    private final Screen parent;

    public SettingsScreen(Screen parent) {
        super(Component.literal("Vrnen Client"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        int x = width / 2 - 100;
        int y = height / 4;
        toggle(x, y, "CPS counter", () -> Config.showCps, v -> Config.showCps = v);
        toggle(x, y + 24, "Keystrokes", () -> Config.showKeystrokes, v -> Config.showKeystrokes = v);
        toggle(x, y + 48, "Armor status", () -> Config.showArmor, v -> Config.showArmor = v);
        toggle(x, y + 72, "Potion status", () -> Config.showPotions, v -> Config.showPotions = v);
        toggle(x, y + 96, "Toggle sprint", () -> Config.toggleSprint, v -> Config.toggleSprint = v);
        toggle(x, y + 120, "Fullbright", () -> Config.fullbright, v -> Config.fullbright = v);

        addRenderableWidget(Button.builder(zoomLabel(), b -> {
            int i = 0;
            for (int k = 0; k < ZOOM_STEPS.length; k++) if (ZOOM_STEPS[k] == Config.zoomFov) i = k;
            Config.zoomFov = ZOOM_STEPS[(i + 1) % ZOOM_STEPS.length];
            b.setMessage(zoomLabel());
        }).bounds(x, y + 144, 200, 20).build());

        addRenderableWidget(Button.builder(Component.literal("Done"), b -> onClose())
                .bounds(x, y + 176, 200, 20).build());
    }

    private void toggle(int x, int y, String label, BooleanSupplier get, Consumer<Boolean> set) {
        addRenderableWidget(Button.builder(text(label, get.getAsBoolean()), b -> {
            set.accept(!get.getAsBoolean());
            b.setMessage(text(label, get.getAsBoolean()));
        }).bounds(x, y, 200, 20).build());
    }

    private static Component text(String label, boolean on) {
        return Component.literal(label + ": " + (on ? "ON" : "OFF"));
    }

    private static Component zoomLabel() {
        return Component.literal("Zoom FOV: " + Config.zoomFov);
    }

    /**
     * Draws our own background instead of the vanilla blurred one, scaled to cover the
     * screen (cropping the edges so the image is never stretched).
     * 1.21.5 blit signature: blit(Function<ResourceLocation, RenderType>, ...).
     */
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
        g.fill(0, 0, width, height, 0x66000000); // dim so the buttons stay readable
    }

    @Override
    public void render(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
        super.render(g, mouseX, mouseY, partialTick);
        g.blit(RenderType::guiTextured, LOGO, 8, 8, 0f, 0f, 48, 48, LOGO_SIZE, LOGO_SIZE, LOGO_SIZE, LOGO_SIZE);
        g.drawCenteredString(font, title, width / 2, height / 4 - 20, 0xFFFFFFFF);
    }

    @Override
    public void onClose() {
        Config.save();
        minecraft.setScreen(parent);
    }
}
