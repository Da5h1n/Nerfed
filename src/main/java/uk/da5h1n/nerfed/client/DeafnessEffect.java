package uk.da5h1n.nerfed.client;

import net.minecraft.client.Minecraft;
import org.lwjgl.openal.AL10;
import org.lwjgl.openal.EXTEfx;
import uk.da5h1n.nerfed.Nerfed;
import uk.da5h1n.nerfed.util.Condition;
import uk.da5h1n.nerfed.util.Strength;

public final class DeafnessEffect {

    private static final float FADE_SECONDS = 1.5f;

    private static final float MAX_VOLUME_CUT = 0.85f;

    private static final  float MIN_HIGH_GAIN = 0.03f;

    private static volatile float strength = 0f;
    private static volatile int filterId = 0;
    private static volatile boolean efxFailed = false;

    private DeafnessEffect() {}

    public static boolean tick() {
        Minecraft mc = Minecraft.getInstance();
        float target = mc.player == null ? 0f : Strength.of(mc.player, Condition.DEAFNESS);

        float old = strength;
        float step = 0.05f / FADE_SECONDS;
        if (old < target) strength = Math.min(target, old + step);
        else if (old > target) strength = Math.max(target, old - step);
        return strength != old;
    }

    public static float volumeFactor() {
        return 1f - MAX_VOLUME_CUT * strength;
    }

    public static void applyLowPass(int source) {
        if (efxFailed) return;

        float s = strength;
        if (s <= 0.001f && filterId == 0) return;

        try {
            if (s <= 0.001f) {
                AL10.alSourcei(source, EXTEfx.AL_DIRECT_FILTER, EXTEfx.AL_FILTER_NULL);
                return;
            }

            if (filterId == 0) {
                filterId = EXTEfx.alGenFilters();
                EXTEfx.alFilteri(filterId, EXTEfx.AL_FILTER_TYPE, EXTEfx.AL_FILTER_LOWPASS);
            }

            float highGain = (float) Math.pow(MIN_HIGH_GAIN, s * s);
            EXTEfx.alFilterf(filterId, EXTEfx.AL_LOWPASS_GAIN, 1.0f);
            EXTEfx.alFilterf(filterId, EXTEfx.AL_LOWPASS_GAINHF, highGain);
            AL10.alSourcei(source, EXTEfx.AL_DIRECT_FILTER, filterId);

            if (AL10.alGetError() != AL10.AL_NO_ERROR) {
                throw new IllegalStateException("OpenAL rejected the low-pass filter");
            }
        } catch (Throwable t) {
            efxFailed = true;
            Nerfed.LOGGER.warn("[Deafness] Low-pass filter unavailable, using volume only", t);
        }
    }

    public static void resetAudio() {
        filterId = 0;
        efxFailed = false;
    }
}
