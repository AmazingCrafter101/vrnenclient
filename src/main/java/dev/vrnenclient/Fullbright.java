package dev.vrnenclient;

import net.minecraft.client.Minecraft;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;

/**
 * Client-only night vision effect. Simple, but the server can overwrite it.
 * Some servers' rules restrict fullbright, so check before using it in matches.
 */
public final class Fullbright {
    private static boolean applied;

    public static void tick(Minecraft mc) {
        if (mc.player == null) { applied = false; return; }
        if (Config.fullbright) {
            var cur = mc.player.getEffect(MobEffects.NIGHT_VISION);
            if (cur == null || cur.getDuration() < 600) {
                mc.player.addEffect(new MobEffectInstance(MobEffects.NIGHT_VISION, 1000, 0, false, false, false));
            }
            applied = true;
        } else if (applied) {
            mc.player.removeEffectNoUpdate(MobEffects.NIGHT_VISION);
            applied = false;
        }
    }

    private Fullbright() {}
}
