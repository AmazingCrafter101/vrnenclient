package dev.vrnenclient;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;

import java.nio.file.Files;
import java.nio.file.Path;

/** Tiny JSON config stored at config/vrnenclient.json. */
public final class Config {
    // HUD
    public static boolean showCps = true;
    public static boolean showFps = true;
    public static boolean showPing = true;
    public static boolean showCoords = true;
    public static boolean showSprint = true;
    public static boolean showKeystrokes = true;
    public static boolean showArmor = true;
    public static boolean showPotions = true;
    public static boolean showItems = true;
    // Combat
    public static boolean showCombo = true;
    public static boolean showReach = true;
    public static boolean showTarget = true;
    // Gameplay
    public static boolean toggleSprint = true;
    public static boolean fullbright = false;
    public static int zoomFov = 30; // vanilla FOV slider minimum is 30

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    private static final class Data {
        boolean showCps = true, showFps = true, showPing = true, showCoords = true, showSprint = true;
        boolean showKeystrokes = true, showArmor = true, showPotions = true, showItems = true;
        boolean showCombo = true, showReach = true, showTarget = true;
        boolean toggleSprint = true, fullbright = false;
        int zoomFov = 30;
    }

    private static Path path() {
        return FabricLoader.getInstance().getConfigDir().resolve("vrnenclient.json");
    }

    public static void load() {
        try {
            Path p = path();
            if (!Files.exists(p)) return;
            Data d = GSON.fromJson(Files.readString(p), Data.class);
            if (d == null) return;
            showCps = d.showCps; showFps = d.showFps; showPing = d.showPing;
            showCoords = d.showCoords; showSprint = d.showSprint;
            showKeystrokes = d.showKeystrokes; showArmor = d.showArmor;
            showPotions = d.showPotions; showItems = d.showItems;
            showCombo = d.showCombo; showReach = d.showReach; showTarget = d.showTarget;
            toggleSprint = d.toggleSprint; fullbright = d.fullbright;
            zoomFov = Math.max(30, Math.min(110, d.zoomFov));
        } catch (Exception e) {
            VrnenClient.LOGGER.warn("Could not read config", e);
        }
    }

    public static void save() {
        try {
            Data d = new Data();
            d.showCps = showCps; d.showFps = showFps; d.showPing = showPing;
            d.showCoords = showCoords; d.showSprint = showSprint;
            d.showKeystrokes = showKeystrokes; d.showArmor = showArmor;
            d.showPotions = showPotions; d.showItems = showItems;
            d.showCombo = showCombo; d.showReach = showReach; d.showTarget = showTarget;
            d.toggleSprint = toggleSprint; d.fullbright = fullbright;
            d.zoomFov = zoomFov;
            Files.writeString(path(), GSON.toJson(d));
        } catch (Exception e) {
            VrnenClient.LOGGER.warn("Could not save config", e);
        }
    }

    private Config() {}
}
