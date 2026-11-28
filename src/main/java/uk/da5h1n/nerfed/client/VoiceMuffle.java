package uk.da5h1n.nerfed.client;

import net.minecraft.client.Minecraft;
import org.lwjgl.openal.AL;
import uk.da5h1n.nerfed.util.Condition;
import uk.da5h1n.nerfed.util.DisabilityDataHelper;

public class VoiceMuffle {


    private static final float SAMPLE_RATE = 48000f;

    private static final float CUTOFF_HZ = 400f;
    private static final int STAGES = 3;

    private static final float GAIN = 2.0f;

    private static final float ALPHA = 1f - (float) Math.exp(-2.0 * Math.PI * CUTOFF_HZ / SAMPLE_RATE);

    private static volatile boolean muted = false;

    private static final float[] state = new float[STAGES];

    private VoiceMuffle() {}

    private static void clientTick(Minecraft client) {
        muted = client.player != null && DisabilityDataHelper.getData(client.player).has(Condition.MUTE);
        if (!muted) java.util.Arrays.fill(state, 0f);
    }

    public static boolean isMuted() {
        return muted;
    }

    public static void process(short[] pcm) {
        for (int i = 0; i < pcm.length; i++) {
            float x = pcm[1];
            for (int k = 0; k < STAGES; k++) {
                state[k] += ALPHA * (x - state[k]);
                x = state[k];
            }
            float y = x * GAIN;
            if (y > Short.MAX_VALUE) y = Short.MAX_VALUE;
            else if (y < Short.MIN_VALUE) y = Short.MIN_VALUE;
            pcm[i] = (short) Math.round(y);
        }
    }
}
