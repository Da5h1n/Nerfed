package uk.da5h1n.nerfed.mixin.client;

import com.mojang.math.Axis;
import net.minecraft.client.Camera;
import net.minecraft.client.DeltaTracker;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import org.joml.Quaternionf;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import uk.da5h1n.nerfed.util.DisabilityDataHelper;

@Mixin(Camera.class)
public abstract class CameraMixin {

    @Shadow @Final
    private Quaternionf rotation;

    @Shadow
    private Entity entity;

    @Shadow
    private int matrixPropertiesDirty;

    @Inject(method = "update", at = @At("RETURN"))
    private void nerfed$applyCameraRoll(DeltaTracker deltaTracker, CallbackInfo ci) {
        if (this.entity == null) return;

        // Resolve the player entity correctly from the camera target or vehicle
        Player player = null;
        if (this.entity instanceof Player p) {
            player = p;
        } else if (this.entity.getVehicle() instanceof Player p) {
            player = p;
        }
        if (player == null) return;

        float rollDegrees = 0.0f;

        // Static tilt for Scoliosis
        if (DisabilityDataHelper.isScoliosis(player)) {
            rollDegrees += 12.5f;
        }

        // Oscillating sin-wave roll for Vertigo
        if (DisabilityDataHelper.isVertigo(player)) {
            long gameTime = player.level().getGameTime();
            float partialTick = deltaTracker.getGameTimeDeltaPartialTick(true);
            float time = (float) gameTime + partialTick;

            rollDegrees += (float) Math.sin(time * 0.04f) * 20.0f;
        }

        // Apply roll rotation and mark matrix properties dirty so the renderer updates
        if (rollDegrees != 0.0f) {
            this.rotation.mul(Axis.ZP.rotationDegrees(rollDegrees));
            this.matrixPropertiesDirty |= 1; // DIRTY_VIEW_ROT flag
        }
    }
}