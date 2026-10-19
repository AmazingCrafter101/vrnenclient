package dev.vrnenclient;

import org.lwjgl.glfw.GLFW;

import java.util.ArrayDeque;

/**
 * Counts clicks by polling GLFW every rendered frame (not every 20Hz tick), so fast
 * clicking isn't undercounted. No mixins needed.
 */
public final class CpsTracker {
    private static final ArrayDeque<Long> LEFT = new ArrayDeque<>();
    private static final ArrayDeque<Long> RIGHT = new ArrayDeque<>();
    private static boolean lastLeft, lastRight;

    public static void poll(long window) {
        long now = System.currentTimeMillis();
        boolean l = GLFW.glfwGetMouseButton(window, GLFW.GLFW_MOUSE_BUTTON_LEFT) == GLFW.GLFW_PRESS;
        boolean r = GLFW.glfwGetMouseButton(window, GLFW.GLFW_MOUSE_BUTTON_RIGHT) == GLFW.GLFW_PRESS;
        if (l && !lastLeft) LEFT.addLast(now);
        if (r && !lastRight) RIGHT.addLast(now);
        lastLeft = l;
        lastRight = r;
        prune(LEFT, now);
        prune(RIGHT, now);
    }

    private static void prune(ArrayDeque<Long> q, long now) {
        while (!q.isEmpty() && now - q.peekFirst() > 1000) q.removeFirst();
    }

    public static int left() { return LEFT.size(); }
    public static int right() { return RIGHT.size(); }

    private CpsTracker() {}
}
