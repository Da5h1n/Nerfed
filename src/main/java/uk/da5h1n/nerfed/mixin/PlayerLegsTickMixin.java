package uk.da5h1n.nerfed.mixin;

import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import uk.da5h1n.nerfed.util.DisabilityDataHelper;

@Mixin(Player.class)
public class PlayerLegsTickMixin {

    @Inject(method = "tick", at = @At("HEAD"))
    private void handleLegDisabilities(CallbackInfo ci) {
        Player player = (Player) (Object) this;

        boolean leftLegDisabled = DisabilityDataHelper.isLeftLegDisabled(player);
        boolean rightLegDisabled = DisabilityDataHelper.isRightLegDisabled(player);

        if (leftLegDisabled && rightLegDisabled) {
            player.setPose(Pose.SWIMMING);
            player.setSprinting(false);
            player.refreshDimensions();
        } else if (leftLegDisabled ^ rightLegDisabled) {
            player.setSprinting(false);
        }
    }

    @ModifyVariable(method = "travel", at = @At("HEAD"), argsOnly = true)
    private Vec3 restrictGroundInput(Vec3 travelVector) {
        Player player = (Player) (Object) this;

        boolean oneLegDisabled = DisabilityDataHelper.isLeftLegDisabled(player)
                ^ DisabilityDataHelper.isRightLegDisabled(player);

        if (oneLegDisabled
                && player.onGround()
                && !player.isInWater()
                && !player.onClimbable()
                && !player.getAbilities().flying) {
            // Keep the y component, drop sideways/forward input
            return new Vec3(0.0, travelVector.y, 0.0);
        }
        return travelVector;
    }

    @Inject(method = "updatePlayerPose", at = @At("TAIL"))
    private void forceCrawlPose(CallbackInfo ci) {
        Player player = (Player) (Object) this;

        if (DisabilityDataHelper.isLeftLegDisabled(player) && DisabilityDataHelper.isRightLegDisabled(player)) {
            player.setPose(Pose.SWIMMING);
        }
    }
}