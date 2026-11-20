package uk.da5h1n.nerfed.client;

import net.minecraft.client.Minecraft;
import org.lwjgl.openal.AL10;
import org.lwjgl.openal.EXTEfx;
import uk.da5h1n.nerfed.Nerfed;
import uk.da5h1n.nerfed.util.Condition;
import uk.da5h1n.nerfed.util.Strength;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class DeafnessEffect {

    private static final float FADE_SECONDS = 1.5f;

    private static final float MAX_VOLUME_CUT = 0.85f;

    private static final float MIN_HIGH_GAIN = 0.03f;

    // Voice chat processing (Simple Voice Chat decodes to 48 kHz mono PCM)
    private static final float VOICE_SAMPLE_RATE = 48000f;
    private static final float VOICE_CUTOFF_OPEN = 20000f; // strength 0 (effectively no filtering)
    private static final float VOICE_CUTOFF_CLOSED = 400f; // strength 1 (very muffled)

    private static volatile float strength = 0f;
    private static volatile int filterId = 0;
    private static volatile boolean efxFailed = false;

    /** Per-speaker low-pass filter state: two cascaded one-pole stages. */
    private static final Map<UUID, float[]> VOICE_STATE = new ConcurrentHashMap<>();

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

    /**
     * Muffles and quietens one frame of decoded voice chat audio in place.
     * Pure DSP with no voice chat API types, so it is safe to load without the mod.
     * Called from the voice chat audio thread.
     *
     * @param speaker id of the sender, so each voice keeps its own filter state
     * @param pcm     16-bit mono samples, modified in place
     */
    public static void processVoice(UUID speaker, short[] pcm) {
        float s = strength;
        if (s <= 0.001f) {
            if (!VOICE_STATE.isEmpty()) VOICE_STATE.clear();
            return;
        }

        // Same s^2 curve as the OpenAL filter, mapped logarithmically onto a cutoff frequency.
        float cutoff = (float) (VOICE_CUTOFF_OPEN
                * Math.pow(VOICE_CUTOFF_CLOSED / VOICE_CUTOFF_OPEN, s * s));
        float a = 1f - (float) Math.exp(-2.0 * Math.PI * cutoff / VOICE_SAMPLE_RATE);
        float gain = volumeFactor();

        float[] st = VOICE_STATE.computeIfAbsent(speaker, k -> new float[2]);
        float s0 = st[0];
        float s1 = st[1];

        for (int i = 0; i < pcm.length; i++) {
            s0 += a * (pcm[i] - s0);
            s1 += a * (s0 - s1);
            float y = s1 * gain;
            if (y > Short.MAX_VALUE) y = Short.MAX_VALUE;
            else if (y < Short.MIN_VALUE) y = Short.MIN_VALUE;
            pcm[i] = (short) Math.round(y);
        }

        st[0] = s0;
        st[1] = s1;
    }

    public static void resetAudio() {
        filterId = 0;
        efxFailed = false;
        VOICE_STATE.clear();
    }
}