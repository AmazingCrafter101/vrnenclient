# Vrnen Client (Fabric, client-side)

Features: CPS counter, keystrokes, armor + potion HUD, toggle sprint, zoom (hold C), fullbright,
settings menu (Right Shift). Config: config/vrnenclient.json.

## Build
1. Generate a matching template at https://fabricmc.net/develop and copy its exact version numbers
   into gradle.properties (the ones here are unverified), plus its gradle wrapper.
2. `./gradlew build` -> build/libs/vrnenclient-0.1.0.jar into your mods folder (needs Fabric API).

## Build without installing anything (GitHub Actions)
1. Create a new GitHub repo and upload everything in this folder (including the hidden `.github` folder).
2. Open the repo's **Actions** tab. The "Build Vrnen Client" run starts on its own (or press "Run workflow").
3. When it turns green, open the run and download the **vrnenclient-jar** artifact (a zip containing the mod jar).
4. Put that jar in `.minecraft/mods` with Fabric Loader and Fabric API 0.128.2+1.21.5 for 1.21.5.
If the run fails, open the log and send me the red error lines.

## Versions
- Locked to **Minecraft 1.21.5** (Fabric Loader 0.16.14, Fabric API 0.128.2+1.21.5, Loom 1.10). Mojang names.
- Not compiled or tested yet. If Gradle complains about a version, copy the numbers from a template at
  https://fabricmc.net/develop (pick 1.21.5).
- Porting later: 1.21.6+ replaces HudRenderCallback with HudElementRegistry and blit takes a RenderPipeline;
  1.21.9+ KeyMapping takes a Category; 26.1+ is unobfuscated (https://docs.fabricmc.net/develop/porting/).

## Art
- Mod icon: assets/vrnenclient/icon.png (shown in Mod Menu). Also used as the logo on the settings screen.
- Settings background: assets/vrnenclient/textures/gui/background.png (1280x720, cropped to fit any window).

## Not done yet
- Hit color (needs a mixin on the damage overlay), deeper zoom than FOV 30 (GameRenderer mixin),
  draggable HUD positions.
