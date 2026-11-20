package uk.da5h1n.nerfed.client;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;
import uk.da5h1n.nerfed.item.ModItems;
import uk.da5h1n.nerfed.util.Condition;
import uk.da5h1n.nerfed.util.DisabilityDataHelper;
//? if >26.3 {

//?} else {
/*import net.minecraft.world.level.block.SoundType;
*///?}

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class Echolocation {
    // ---------------------------------------------------------------- tuning
    private static final int SPHERE_RAYS = 96;
    private static final int MAX_BOUNCES = 3;

    private static final int MAX_PULSES = 3;            // must match Pulse[3] in the shader
    private static final int EMITTERS_PER_PULSE = 4;    // must match Emitters[3 * 4] in the shader

    private static final float SPEED = 40f;             // blocks per second the pulse travels
    private static final float LINGER_SECONDS = 3.0f;   // total life of one pulse
    private static final float FADE_OUT_SECONDS = 0.8f; // fade at the end of that life
    private static final float STEP_DISTANCE = 5.5f;    // blocks walked between footstep pulses
    private static final float MIN_SHOWN = 0.005f;

    // Pulse reach per blindness level 0..3
    private static final float[] STEP_RADIUS = {0f, 16f, 24f, 36f};
    private static final float[] TAP_RADIUS  = {0f, 26f, 38f, 52f};

    // {strength, value} knots, interpolated by how strongly blindness is currently shown
    private static final float[][] VISIBILITY = {{0f, 1f}, {0.4f, 0.30f}, {0.7f, 0.03f}, {1f, 0f}};
    private static final float[][] GLOW_KEEP  = {{0f, 0f}, {0.4f, 0.5f}, {0.7f, 0.9f}, {1f, 0f}};
    private static final float[][] ECHO_GAIN  = {{0f, 0f}, {0.4f, 0.55f}, {0.7f, 1.0f}, {1f, 1.4f}};
    private static final float[][] BLUR_PX    = {{0f, 0f}, {0.4f, 2.5f}, {0.7f, 3.0f}, {1f, 0f}};

    //? if >26.3 {
    private static final SoundEvent TAP_SOUND = SoundEvents.WOOD.stepSound().map(Holder::value).orElseThrow();
    //?} else {
    /*private static final SoundEvent TAP_SOUND = SoundType.WOOD.getStepSound();
    *///?}
    private static final Random RANDOM = new Random();

    // ----------------------------------------------------------------- state
    /** A point that re-radiates the pulse: the player's origin, or a block a ray bounced off. */
    public record Emitter(Vec3 pos, float pathDist) {}

    /** A ray hit, with the path length the ray had travelled when it got there. */
    private record Hit(Vec3 pos, float pathDist) {}

    /** One pulse: its own clock and its own emitters, so several can overlap. */
    private record Pulse(List<Emitter> emitters, long startNanos, float reach, float strength) {}

    private static final List<Pulse> PULSES = new ArrayList<>();

    private static float shown = 0f;
    private static Vec3 lastPos = null;
    private static float stepDistance = 0f;
    private static boolean wasOnGround = true;
    private static double lastVelY = 0.0;
    private static long lastTapNanos = 0L;

    private Echolocation() {}

    static void setShown(float strength) {
        shown = strength;
    }

    // ------------------------------------------------------------------ input
    public static void tap(Player player) {
        if (player == null) return;
        long now = System.nanoTime();
        if (now - lastTapNanos < 350_000_000L) return;
        lastTapNanos = now;

        int level = Math.clamp(DisabilityDataHelper.getLevel(player, Condition.BLINDNESS), 0, 3);
        if (level <= 0) return;

        player.level().playLocalSound(
                player.getX(), player.getY(), player.getZ(),
                TAP_SOUND, SoundSource.PLAYERS,
                0.9f, 0.85f + RANDOM.nextFloat() * 0.3f, false
        );

        if (player.level() instanceof ClientLevel clientLevel && player instanceof LocalPlayer localPlayer) {
            emitVibration(clientLevel, localPlayer, player.position().add(0, 0.9, 0), TAP_RADIUS[level], 1.0f);
        }
    }

    private static float reach(LocalPlayer player) {
        float reach = 1f;
        if (player.getMainHandItem().is(ModItems.WALKING_STICK) || player.getOffhandItem().is(ModItems.WALKING_STICK)) {
            reach *= 1.5f;
        }
        if (player.isShiftKeyDown()) {
            reach *= 0.4f;
        }
        return reach;
    }

    public static void tick(Minecraft client) {
        LocalPlayer player = client.player;
        if (player == null || !(client.level instanceof ClientLevel clientLevel)) {
            reset();
            return;
        }

        Vec3 pos = player.position();
        boolean onGround = player.onGround();
        int blindnessLevel = Math.clamp(DisabilityDataHelper.getLevel(player, Condition.BLINDNESS), 0, 3);

        if (blindnessLevel > 0) {
            float reach = reach(player);

            if (lastPos != null && onGround) {
                double dx = pos.x - lastPos.x;
                double dz = pos.z - lastPos.z;
                stepDistance += (float) Math.sqrt(dx * dx + dz * dz);
                if (stepDistance >= STEP_DISTANCE) {
                    stepDistance = 0f;
                    emitVibration(clientLevel, player, pos.add(0, 0.15, 0), STEP_RADIUS[blindnessLevel] * reach, 0.85f);
                }
            }

            if (onGround && !wasOnGround && lastVelY < -0.25) {
                float hard = lastVelY < -0.7 ? 1.4f : 1.0f;
                emitVibration(clientLevel, player, pos.add(0, 0.15, 0), STEP_RADIUS[blindnessLevel] * hard * reach, 1.0f);
            }
        } else {
            reset();
        }

        wasOnGround = onGround;
        lastVelY = player.getDeltaMovement().y;
        lastPos = pos;
    }

    // ------------------------------------------------------------------ pulse
    public static void emitVibration(ClientLevel level, LocalPlayer player, Vec3 origin, float maxRadius, float strength) {
        if (level == null || player == null || maxRadius <= 0.5f) return;

        // Strictly only visible to players with blindness
        int blindnessLevel = Math.clamp(DisabilityDataHelper.getLevel(player, Condition.BLINDNESS), 0, 3);
        if (blindnessLevel <= 0 || shown <= MIN_SHOWN) return;

        // 1. Every point where a ray hit a block, with the path length it had travelled so far
        List<Hit> hits = new ArrayList<>();
        for (int i = 0; i < SPHERE_RAYS; i++) {
            traceBounces(level, player, origin, fibonacciSphereDir(i, SPHERE_RAYS), maxRadius, hits);
        }

        // 2. Emitters: the origin first, then the hits furthest from the ones already chosen,
        //    so different walls light up instead of all the rays piling onto one spot
        List<Emitter> chosen = new ArrayList<>();
        chosen.add(new Emitter(origin, 0f));
        while (chosen.size() < EMITTERS_PER_PULSE && !hits.isEmpty()) {
            Hit best = null;
            double bestScore = -1.0;
            for (Hit h : hits) {
                double nearestSq = Double.MAX_VALUE;
                for (Emitter e : chosen) {
                    nearestSq = Math.min(nearestSq, h.pos().distanceToSqr(e.pos()));
                }
                double score = nearestSq / (1.0 + h.pathDist() * 0.05);   // spread out, prefer nearer
                if (score > bestScore) {
                    bestScore = score;
                    best = h;
                }
            }
            if (best == null || bestScore < 4.0) break;
            chosen.add(new Emitter(best.pos(), best.pathDist()));
            hits.remove(best);
        }

        // 3. Add as a NEW pulse. Only the oldest one is dropped, so earlier highlights keep fading out.
        PULSES.add(new Pulse(chosen, System.nanoTime(), maxRadius, strength));
        while (PULSES.size() > MAX_PULSES) {
            PULSES.remove(0);
        }
    }

    /** Follows one ray through up to MAX_BOUNCES reflections, recording every block it hits. */
    private static void traceBounces(ClientLevel level, LocalPlayer player, Vec3 start,
                                     Vec3 initialDir, float maxRadius, List<Hit> out) {
        Vec3 curr = start;
        Vec3 dir = initialDir.normalize();
        float remaining = maxRadius;
        float travelled = 0f;

        for (int bounce = 0; bounce < MAX_BOUNCES && remaining > 0.25f; bounce++) {
            Vec3 target = curr.add(dir.scale(remaining));
            BlockHitResult hit = level.clip(new ClipContext(
                    curr, target, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, player));
            if (hit.getType() == HitResult.Type.MISS) break;

            Vec3 hitPos = hit.getLocation();
            float segLen = (float) curr.distanceTo(hitPos);
            if (segLen < 0.15f) break;

            travelled += segLen;
            remaining -= segLen;
            out.add(new Hit(hitPos, travelled));

            // Reflect off the face that was hit, and nudge off the surface so the next ray doesn't re-hit it
            Direction face = hit.getDirection();
            Vec3 n = new Vec3(face.getStepX(), face.getStepY(), face.getStepZ());
            dir = dir.subtract(n.scale(2.0 * dir.dot(n))).normalize();
            curr = hitPos.add(dir.scale(0.03));
        }
    }

    private static Vec3 fibonacciSphereDir(int index, int total) {
        double goldenAngle = Math.PI * (3.0 - Math.sqrt(5.0));
        double y = 1.0 - (2.0 * index) / Math.max(1, total - 1);
        double r = Math.sqrt(Math.max(0.0, 1.0 - y * y));
        double theta = goldenAngle * index;
        return new Vec3(Math.cos(theta) * r, y, Math.sin(theta) * r).normalize();
    }

    public static void reset() {
        PULSES.clear();
        lastPos = null;
        stepDistance = 0f;
        wasOnGround = true;
        lastVelY = 0.0;
    }

    // --------------------------------------------------------------- uniforms
    private static float interp(float[][] knots, float s) {
        for (int i = 0; i < knots.length - 1; i++) {
            float x0 = knots[i][0], y0 = knots[i][1];
            float x1 = knots[i + 1][0], y1 = knots[i + 1][1];
            if (s <= x1) return y0 + (y1 - y0) * ((s - x0) / (x1 - x0));
        }
        return knots[knots.length - 1][1];
    }

    private static void addMatrix(List<Number> out, Matrix4f matrix) {
        float[] m = new float[16];
        matrix.get(m);
        for (float v : m) out.add(v);
    }

    /**
     * 100 floats, matching BlindnessConfig in the shader:
     * 2 matrices (32), ParamsA (4), ParamsB (4), Pulse[3] (12), Emitters[12] (48).
     */
    public static List<Number> uniforms() {
        Minecraft mc = Minecraft.getInstance();
        CameraRenderState cam = mc.gameRenderer.gameRenderState().levelRenderState.cameraRenderState;

        List<Number> out = new ArrayList<>(100);
        addMatrix(out, new Matrix4f(cam.projectionMatrix).invert());
        addMatrix(out, new Matrix4f(cam.viewRotationMatrix).invert());

        long now = System.nanoTime();
        PULSES.removeIf(p -> (now - p.startNanos()) / 1.0e9f > LINGER_SECONDS);

        // ParamsA: x base visibility, y bright light survival, z echo gain, w blur px
        float s = shown;
        out.add(interp(VISIBILITY, s));
        out.add(interp(GLOW_KEEP, s));
        out.add(interp(ECHO_GAIN, s));
        out.add(interp(BLUR_PX, s));

        // ParamsB: x depth 0..1 flag, y speed, zw unused
        out.add(RenderSystem.getDevice().getDeviceInfo().isZZeroToOne() ? 1f : 0f);
        out.add(SPEED);
        out.add(0f);
        out.add(0f);

        // Pulse[3]: x seconds since start, y reach, z gain (strength * fade), w < 0 = unused
        for (int i = 0; i < MAX_PULSES; i++) {
            if (i < PULSES.size()) {
                Pulse p = PULSES.get(i);
                float elapsed = (now - p.startNanos()) / 1.0e9f;
                float fade = Math.clamp((LINGER_SECONDS - elapsed) / FADE_OUT_SECONDS, 0f, 1f);
                out.add(elapsed);
                out.add(p.reach());
                out.add(p.strength() * fade);
                out.add(0f);
            } else {
                out.add(0f);
                out.add(0f);
                out.add(0f);
                out.add(-1f);
            }
        }

        // Emitters[12]: camera-relative position + path length (w < 0 = unused)
        Vec3 camPos = cam.pos;
        for (int i = 0; i < MAX_PULSES; i++) {
            for (int j = 0; j < EMITTERS_PER_PULSE; j++) {
                if (i < PULSES.size() && j < PULSES.get(i).emitters().size()) {
                    Emitter e = PULSES.get(i).emitters().get(j);
                    out.add((float) (e.pos().x - camPos.x));
                    out.add((float) (e.pos().y - camPos.y));
                    out.add((float) (e.pos().z - camPos.z));
                    out.add(e.pathDist());
                } else {
                    out.add(0f);
                    out.add(0f);
                    out.add(0f);
                    out.add(-1f);
                }
            }
        }
        return out;
    }
}