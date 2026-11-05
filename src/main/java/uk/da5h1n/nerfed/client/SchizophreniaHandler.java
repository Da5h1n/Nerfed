package uk.da5h1n.nerfed.client;

import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.*;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.Vec3;
import uk.da5h1n.nerfed.Nerfed;
import uk.da5h1n.nerfed.util.DisabilityDataHelper;

import java.util.*;

public class SchizophreniaHandler {
    private static final Random RANDOM = new Random();

    private static int moodLogTimer = 0;

    private static final int BASE_DELAY_TICKS = 20 * 25;        // ~25 s at baseline, ~12 s in the dark
    private static final int PHANTOM_CHECK_INTERVAL = 100;      // roll for a phantom every 5 s
    private static final float PHANTOM_CHANCE = 0.25F;
    private static final double VANISH_DISTANCE_SQ = 36.0;      // phantom disappears within 6 blocks
    private static final int PHANTOM_LIFETIME_TICKS = 20 * 25;
    private static final SoundSource SOUND_CATEGORY = SoundSource.HOSTILE; // AMBIENT = vanilla cave-sound slider

    // Client-only entities get ids at or below this value. Real entity ids are never negative.
    private static final int FAKE_ID_START = -100_000;

    private static SoundEvent block(Optional<Holder<SoundEvent>> sound) {
        return sound.map(Holder::value).orElseThrow();
    }

    private record HallucinationSound(
            SoundEvent sound,
            double minDist, double maxDist,
            boolean behind,
            float volume, float minPitch, float maxPitch,
            int weight,
            int cooldownTicks,
            boolean startle
    ) {}

    private static final List<HallucinationSound> SOUNDS = List.of(
            // ---- Startle: close, loud, behind you ----
            new HallucinationSound(SoundEvents.CREEPER_PRIMED,       3,  6, true,  0.7F, 0.9F, 1.0F, 3, 20 * 120, true),
            new HallucinationSound(SoundEvents.ENDERMAN_SCREAM,      3,  7, true,  0.6F, 0.8F, 1.0F, 2, 20 * 180, true),
            new HallucinationSound(SoundEvents.ENDERMAN_STARE,       4,  8, true,  0.6F, 0.8F, 1.0F, 2, 20 * 180, true),
            new HallucinationSound(SoundEvents.GHAST_SCREAM,         6, 12, false, 0.5F, 0.8F, 1.0F, 1, 20 * 240, true),
            new HallucinationSound(SoundEvents.ZOMBIE_ATTACK_WOODEN_DOOR, 3, 6, true, 0.7F, 0.9F, 1.1F, 2, 20 * 150, true),
            new HallucinationSound(block(SoundEvents.GLASS.breakSound()),          3,  7, true,  0.7F, 0.9F, 1.1F, 2, 20 * 150, true),
            new HallucinationSound(SoundEvents.SKELETON_SHOOT,       3,  6, true,  0.7F, 0.9F, 1.1F, 2, 20 * 150, true),
            new HallucinationSound(SoundEvents.WARDEN_HEARTBEAT,     0,  1, false, 0.8F, 0.9F, 1.0F, 1, 20 * 240, true),

            // ---- Ambient: quieter, farther away ----
            new HallucinationSound(SoundEvents.ZOMBIE_AMBIENT,       8, 16, false, 0.4F, 0.7F, 0.9F, 5, 20 * 40,  false),
            new HallucinationSound(SoundEvents.SKELETON_AMBIENT,     8, 16, false, 0.4F, 0.8F, 1.0F, 4, 20 * 40,  false),
            new HallucinationSound(SoundEvents.SPIDER_AMBIENT,       6, 14, false, 0.4F, 0.8F, 1.0F, 4, 20 * 40,  false),
            new HallucinationSound(SoundEvents.ENDERMAN_TELEPORT,    6, 14, false, 0.4F, 0.7F, 1.0F, 3, 20 * 60,  false),
            new HallucinationSound(SoundEvents.PHANTOM_AMBIENT,      8, 16, false, 0.4F, 0.7F, 0.9F, 3, 20 * 60,  false),
            new HallucinationSound(SoundEvents.WITCH_AMBIENT,        8, 16, false, 0.4F, 0.8F, 1.0F, 2, 20 * 90,  false),
            new HallucinationSound(SoundEvents.WOODEN_DOOR_OPEN,     5, 12, false, 0.5F, 0.9F, 1.0F, 3, 20 * 60,  false),
            new HallucinationSound(SoundEvents.WOODEN_DOOR_CLOSE,    5, 12, false, 0.5F, 0.9F, 1.0F, 3, 20 * 60,  false),
            new HallucinationSound(SoundEvents.CHEST_OPEN,           4, 10, false, 0.5F, 0.9F, 1.0F, 2, 20 * 90,  false),
            new HallucinationSound(block(SoundEvents.GRAVEL.stepSound()),          2,  5, true,  0.5F, 0.8F, 1.0F, 3, 20 * 45,  false),
            new HallucinationSound(block(SoundEvents.STONE.stepSound()),           2,  5, true,  0.5F, 0.8F, 1.0F, 3, 20 * 45,  false)
    );

    private static long lastStartle = -1_000_000L;
    private static final int STARTLE_GLOBAL_COOLDOWN = 20 * 180;

    private static final Map<SoundEvent, Long> lastPlayed = new HashMap<>();
    private static double moodiness = 0.0;

    private record Phantom(Entity entity, Long spawnedAt) {}

    private static final List<Phantom> activePhantoms = new ArrayList<>();
    private static int nextFakeId = FAKE_ID_START;
    private static int phantomTimer = 0;

    public static boolean isPhantom(Entity entity) {
        return entity.getId() <= FAKE_ID_START;
    }

    public static void clientTick(Minecraft client) {
        if (client.player == null || client.level == null) {
            activePhantoms.clear();   // level is gone, so the entities went with it
            moodiness = 0.0;
            lastStartle = -1_000_000L;
            lastPlayed.clear();
            return;
        }

        tickPhantoms(client);

        if (!DisabilityDataHelper.isSchizophrenia(client.player)) {
            clearAllPhantoms(client);
            moodiness = 0.0;
            return;
        }

        tickAuditory(client);

        if (++phantomTimer >= PHANTOM_CHECK_INTERVAL) {
            phantomTimer = 0;
            if (activePhantoms.isEmpty() && RANDOM.nextFloat() < PHANTOM_CHANCE) {
                spawnPhantom(client);
            }
        }
    }

    private static void tickAuditory(Minecraft client) {
        var player = client.player;
        var level = client.level;

        BlockPos probe = BlockPos.containing(
                player.getX() + RANDOM.nextInt(17) - 8,
                player.getEyeY() + RANDOM.nextInt(17) - 8,
                player.getZ() + RANDOM.nextInt(17) - 8);

        int sky = level.getBrightness(LightLayer.SKY, probe);
        int block = level.getBrightness(LightLayer.BLOCK, probe);

        double rate = 1.0 / BASE_DELAY_TICKS;
        if (sky == 0 && block <= 1) rate *= 2.0;
        else if (sky > 8)           rate *= 0.5;
        if (player.getHealth() <= 6.0F) rate *= 1.5;

        moodiness += rate;

        // Mood readout once per second
        if (++moodLogTimer >= 20) {
            moodLogTimer = 0;
            Nerfed.LOGGER.info("[Schizophrenia] mood={}/1.00  perSec={}  sky={} block={}  hp={}",
                    String.format("%.2f", moodiness), String.format("%.3f", rate * 20),
                    sky, block, player.getHealth());
        }

        if (moodiness >= 1.0) {
            Nerfed.LOGGER.info("[Schizophrenia] mood full, triggering a hallucination");
            triggerAuditoryHallucination(client, probe);
            moodiness = RANDOM.nextDouble() * 0.3;
        }
    }

    private static void triggerAuditoryHallucination(Minecraft client, BlockPos probe) {
        long now = client.level.getGameTime();

        List<HallucinationSound> available = new ArrayList<>();
        for (HallucinationSound s : SOUNDS) {
            long last = lastPlayed.getOrDefault(s.sound(), -1_000_000L);
            if (now - last < s.cooldownTicks()) continue;
            if (s.startle() && now - lastStartle < STARTLE_GLOBAL_COOLDOWN) continue;
            available.add(s);
        }
        if (available.isEmpty()) {
            Nerfed.LOGGER.info("[Schizophrenia] nothing available (all on cooldown)");
            return;
        }

        int totalWeight = 0;
        for (HallucinationSound s : available) totalWeight += s.weight();
        int roll = RANDOM.nextInt(totalWeight);
        HallucinationSound chosen = available.get(0);
        for (HallucinationSound s : available) {
            roll -= s.weight();
            if (roll < 0) { chosen = s; break; }
        }

        double dist = chosen.minDist() + RANDOM.nextDouble() * (chosen.maxDist() - chosen.minDist());
        Vec3 eye = client.player.getEyePosition();
        double x, y, z;

        if (chosen.behind()) {

            double angle = Math.toRadians(client.player.getYRot() + 180 + (RANDOM.nextDouble() - 0.5) * 90);
            x = eye.x - Math.sin(angle) * dist;
            y = client.player.getY() + RANDOM.nextDouble() * 1.5;
            z = eye.z + Math.cos(angle) * dist;
        } else {
            Vec3 dir = Vec3.atCenterOf(probe).subtract(eye);
            if (dir.lengthSqr() < 1.0E-4) return;
            Vec3 at = eye.add(dir.normalize().scale(dist));
            x = at.x; y = at.y; z = at.z;
        }

        float pitch = chosen.minPitch() + RANDOM.nextFloat() * (chosen.maxPitch() - chosen.minPitch());
        client.level.playLocalSound(x, y, z, chosen.sound(), SOUND_CATEGORY, chosen.volume(), pitch, false);

        lastPlayed.put(chosen.sound(), now);
        if (chosen.startle()) lastStartle = now;

        Nerfed.LOGGER.info("[Schizophrenia] SOUND {}  startle={}  dist={}  at ({}, {}, {})  pitch={}  ({} candidates)",
        BuiltInRegistries.SOUND_EVENT.getKey(chosen.sound()), chosen.startle(),
        String.format("%.1f", dist), String.format("%.1f", x), String.format("%.1f", y),
        String.format("%.1f", z), String.format("%.2f", pitch), available.size());
    }

    private static void spawnPhantom(Minecraft client) {
        double distance = 12.0 + RANDOM.nextDouble() * 8.0;
        double angle = Math.toRadians(client.player.getYRot() + (RANDOM.nextBoolean() ? 45 : -45));

        double x = client.player.getX() - Math.sin(angle) * distance;
        double z = client.player.getZ() + Math.cos(angle) * distance;
        double y = client.level.getHeight(Heightmap.Types.MOTION_BLOCKING, Mth.floor(x), Mth.floor(z));

        EntityType<?> type = switch (RANDOM.nextInt(3)) {
            case 0 -> EntityTypes.ZOMBIE;
            case 1 -> EntityTypes.CREEPER;
            default -> EntityTypes.ENDERMAN;
        };

        // True = ignore spawn checks so it still works on peaceful
        Entity entity = type.create(client.level, new EntitySpawnRequest(EntitySpawnReason.LOAD, true));
        if (entity == null) return;

        entity.setId(nextFakeId--);
        entity.setPos(x, y, z);
        entity.setSilent(true);
        entity.noPhysics = true;
        if (entity instanceof Mob mob) mob.setNoAi(true);

        client.level.addEntity(entity);
        activePhantoms.add(new Phantom(entity, client.level.getGameTime()));
        Nerfed.LOGGER.info("[Schizophrenia] PHANTOM {} id={} at ({}, {}, {})",
                EntityType.getKey(type), entity.getId(),
                String.format("%.1f", x), String.format("%.1f", y), String.format("%.1f", z));
    }

    private static void tickPhantoms(Minecraft client) {
        if (activePhantoms.isEmpty()) return;

        Iterator<Phantom> it = activePhantoms.iterator();
        while (it.hasNext()) {
            Phantom p = it.next();
            Entity e = p.entity();

            double dx = client.player.getX() - e.getX();
            double dz = client.player.getZ() - e.getZ();
            float yaw = (float) (Mth.atan2(dz, dx) * (180.0 / Math.PI)) - 90.0F;
            e.setYRot(yaw);
            if (e instanceof LivingEntity living) {
                living.setYHeadRot(yaw);
                living.setYBodyRot(yaw);
            }
            e.clearFire();  // stops zombies visibly burning in daylight

            boolean close = client.player.distanceToSqr(e) <= VANISH_DISTANCE_SQ;
            boolean expired = client.level.getGameTime() - p.spawnedAt() > PHANTOM_LIFETIME_TICKS;

            if (close || expired || e.isRemoved()) {
                if (close) {
                    client.level.playLocalSound(e.getX(), e.getY(), e.getZ(),
                            SoundEvents.SOUL_ESCAPE.value(), SoundSource.AMBIENT, 0.3F, 1.2F, false);
                }
                client.level.removeEntity(e.getId(), Entity.RemovalReason.DISCARDED);
                it.remove();
            }
        }
    }

    public static void clearAllPhantoms(Minecraft client) {
        for (Phantom p : activePhantoms) {
            if (client.level != null) {
                client.level.removeEntity(p.entity().getId(), Entity.RemovalReason.DISCARDED);
            }
        }
        activePhantoms.clear();
    }
}