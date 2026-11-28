package uk.da5h1n.nerfed.client;

import net.minecraft.client.Minecraft;
import net.minecraft.core.Holder;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.phys.Vec3;
import uk.da5h1n.nerfed.util.Condition;
import uk.da5h1n.nerfed.util.DisabilityDataHelper;
import uk.da5h1n.nerfed.util.Strength;

public class TinnitusHandler {

    private static final RandomSource RANDOM = RandomSource.create();
    private static int ticksUntilNext = 60;

    public static void clientTick(Minecraft client) {
        if (client.player == null || client.level == null) {
            ticksUntilNext = 60;
            return;
        }

        if (!DisabilityDataHelper.getData(client.player).has(Condition.TINNITUS)) return;

        float strength = Strength.of(client.player, Condition.TINNITUS) * NerfedSettings.get().scaleFor(Condition.TINNITUS);

        if (strength <= 0.001f) return;

        if (--ticksUntilNext > 0) return;

        ticksUntilNext = (int) ((20 * 6 + RANDOM.nextInt(20 * 4)) / (0.5f + 0.5f * strength));

        float volume = 0.08f + 0.22f * strength;
        float pitch = 1.7f + RANDOM.nextFloat() * 0.3f;
        Vec3 at = client.player.getEyePosition();

        play(client, SoundEvents.AMETHYST_BLOCK_RESONATE, at, volume, pitch);
    }

    private static void play(Minecraft client, SoundEvent sound, Vec3 at, float volume, float pitch) {
        client.level.playLocalSound(at.x, at.y, at.z, sound, SoundSource.MASTER, volume, pitch, false);
    }

    private static void play(Minecraft client, Holder<SoundEvent> sound, Vec3 at, float volume, float pitch) {
        play(client, sound.value(), at, volume, pitch);
    }
}
