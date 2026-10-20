package dev.vrnenclient;

import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

/** Tracks combo, reach and the current target. Fed by the attack event and the client tick. */
public final class Combat {
    private static int combo;
    private static long lastHitMs;
    private static double reach;
    private static long reachMs;
    private static LivingEntity target;
    private static long targetMs;
    private static int lastHurt;

    public static void onAttack(Player player, Entity entity) {
        Minecraft mc = Minecraft.getInstance();
        // The event also fires for the integrated server's player; only count our own swings.
        if (player != mc.player) return;
        if (!(entity instanceof LivingEntity living) || !living.isAlive()) return;

        long now = System.currentTimeMillis();
        if (now - lastHitMs >= 450) { // roughly one real hit per damage-immunity window
            combo++;
            lastHitMs = now;
        }
        reach = distanceToHitbox(player, entity);
        reachMs = now;
        target = living;
        targetMs = now;
    }

    public static void tick(Minecraft mc) {
        if (mc.player == null) {
            combo = 0;
            target = null;
            lastHurt = 0;
            return;
        }
        long now = System.currentTimeMillis();
        if (combo > 0 && now - lastHitMs > 3000) combo = 0;
        int hurt = mc.player.hurtTime;
        if (hurt > lastHurt) combo = 0; // we just got hit, combo is broken
        lastHurt = hurt;
    }

    private static double distanceToHitbox(Player p, Entity e) {
        Vec3 eye = p.getEyePosition();
        AABB b = e.getBoundingBox();
        double dx = Math.max(Math.max(b.minX - eye.x, 0.0), eye.x - b.maxX);
        double dy = Math.max(Math.max(b.minY - eye.y, 0.0), eye.y - b.maxY);
        double dz = Math.max(Math.max(b.minZ - eye.z, 0.0), eye.z - b.maxZ);
        return Math.sqrt(dx * dx + dy * dy + dz * dz);
    }

    public static int combo() { return combo; }
    public static double reach() { return reach; }
    public static boolean reachFresh() { return System.currentTimeMillis() - reachMs < 3000; }

    /** The entity you last hit, while it is alive and the hit was within 5 seconds. */
    public static LivingEntity target() {
        if (target == null) return null;
        if (!target.isAlive() || System.currentTimeMillis() - targetMs > 5000) return null;
        return target;
    }

    private Combat() {}
}
