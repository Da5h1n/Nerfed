package uk.da5h1n.nerfed.client;

import java.util.HashMap;
import java.util.Map;

public class FilterFader {

    public static final float FADE_SECONDS = 1.5f;

    private static final Map<String, Float> shown = new HashMap<>();
    private static long lastNanos = 0L;
    private static float dt = 0f;
    private static boolean snap = true;


    private FilterFader() {}

    public static void beginFrame() {
        long now = System.nanoTime();
        dt = lastNanos == 0L ? 0f : Math.min((now - lastNanos) / 1_000_000_000f, 0.1f);
        lastNanos = now;
    }

    public static float fade(String key, float target) {
        float current = shown.getOrDefault(key, 0f);
        if (snap) {
            current = target;
        } else {
            float step = dt / FADE_SECONDS;
            if (current < target) current = Math.min(target, current + step);
            else if (current > target) current = Math.max(target, current - step);
        }
        shown.put(key, current);
        return current;
    }

    public static void endFrame() {
        snap = false;
    }

    public static void reset() {
        shown.clear();
        lastNanos = 0L;
        snap = true;
    }
}
