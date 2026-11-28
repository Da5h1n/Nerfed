package uk.da5h1n.nerfed.client;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;
import uk.da5h1n.nerfed.Nerfed;
import uk.da5h1n.nerfed.util.Condition;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

public class NerfedSettings {

    public enum RenderMode {
        FULL(1.0f),
        GENTLE(0.5f),
        OFF(0.0f);

        private final float scale;

        RenderMode(float scale) { this.scale = scale; }

        /* multiplier added to strength */
        public float scale() { return scale; }

        public RenderMode mostRestrictive(RenderMode other) {
            return ordinal() >= other.ordinal() ? this : other;
        }

        public RenderMode next() {
            RenderMode[] all = values();
            return all[(ordinal() + 1) % all.length];
        }

        public String langKey() { return "nerfed.render_mode." + name().toLowerCase(); }
    }

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static volatile NerfedSettings instance;

    /** Master limit for effects flagged PHOTOSENSITIVITY. FULL means no limit. */
    private RenderMode photosensitivityLimit = RenderMode.FULL;
    /** Master limit for effects flagged MOTION. FULL means no limit. */
    private RenderMode motionLimit = RenderMode.FULL;
    /** Per-effect override, keyed by condition id. Missing means FULL. */
    private Map<String, RenderMode> effects = new HashMap<>();
    /** Set once the first-run accessibility prompt has been shown. */
    private boolean firstRunDone = false;

    private NerfedSettings() {}

    // Access

    public static NerfedSettings get() {
        NerfedSettings s = instance;
        if (s == null) {
            synchronized (NerfedSettings.class) {
                if (instance == null) instance = load();
                s = instance;
            }
        }
        return s;
    }

    public RenderMode effectiveMode(Condition condition) {
        RenderMode mode = nz(effects == null ? null : effects.get(condition.id()));
        if (condition.hasRisk(Condition.Risk.PHOTOSENSITIVITY)) {
            mode = mode.mostRestrictive(nz(photosensitivityLimit));
        }
        if (condition.hasRisk(Condition.Risk.MOTION)) {
            mode = mode.mostRestrictive(nz(motionLimit));
        }
        return mode;
    }

    /** Strength multiplier for a condition (1 = full, 0.5 = gentle, 0 = off). */
    public float scaleFor(Condition condition) {
        return effectiveMode(condition).scale();
    }

    public RenderMode getEffectMode(Condition condition) {
        return nz(effects == null ? null : effects.get(condition.id()));
    }

    public void setEffectMode(Condition condition, RenderMode mode) {
        if (effects == null) effects = new HashMap<>();
        if (mode == RenderMode.FULL) effects.remove(condition.id());
        else effects.put(condition.id(), mode);
    }

    public RenderMode getPhotosensitivityLimit() { return nz(photosensitivityLimit); }
    public void setPhotosensitivityLimit(RenderMode mode) { this.photosensitivityLimit = mode; }

    public RenderMode getMotionLimit() { return nz(motionLimit); }
    public void setMotionLimit(RenderMode mode) { this.motionLimit = mode; }

    public boolean isFirstRunDone() { return firstRunDone; }
    public void setFirstRunDone(boolean done) { this.firstRunDone = done; }

    private static RenderMode nz(RenderMode mode) {
        return mode == null ? RenderMode.FULL : mode;
    }

    // Persistance

    private static Path path() {
        return FabricLoader.getInstance().getConfigDir().resolve("nerfed.json");
    }

    private static NerfedSettings load() {
        Path file = path();
        if (Files.exists(file)) {
            try {
                NerfedSettings loaded = GSON.fromJson(Files.readString(file), NerfedSettings.class);
                if (loaded != null) return loaded;
            } catch (Exception e) {
                Nerfed.LOGGER.warn("[Nerfed] Could not read {}, using defaults", file, e);
            }
        }
        NerfedSettings fresh = new NerfedSettings();
        fresh.save();
        return fresh;
    }

    public void save() {
        try {
            Files.writeString(path(), GSON.toJson(this));
        } catch (IOException e) {
            Nerfed.LOGGER.warn("[Nerfed] Could not save settings", e);
        }
    }
}
