package uk.da5h1n.nerfed.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.ClientAvatarEntity;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.player.AvatarRenderer;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.Avatar;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import uk.da5h1n.nerfed.util.ILimbRenderState;
import uk.da5h1n.nerfed.util.LimbDataHelper;

@Mixin(AvatarRenderer.class)
public class AvatarRendererMixin<AvatarlikeEntity extends Avatar & ClientAvatarEntity> {

    @Inject(method = "extractRenderState(Lnet/minecraft/world/entity/Avatar;Lnet/minecraft/client/renderer/entity/state/AvatarRenderState;F)V", at = @At("TAIL"))
    private void extractLimbState(AvatarlikeEntity entity, AvatarRenderState state, float partialTicks, CallbackInfo ci) {
        ILimbRenderState limbState = (ILimbRenderState) state;

        if (entity instanceof Player player) {
            limbState.nerfed$setLeftArmDisabled(LimbDataHelper.isLeftArmDisabled(player));
            limbState.nerfed$setRightArmDisabled(LimbDataHelper.isRightArmDisabled(player));
        }
    }

    @Inject(method = "renderRightHand", at = @At("HEAD"), cancellable = true)
    private void hideRightHandFirstPerson(PoseStack poseStack, SubmitNodeCollector submitNodeCollector, int lightCoords, Identifier skinTexture, boolean hasSleeve, CallbackInfo ci) {
        LocalPlayer player = Minecraft.getInstance().player;
        if (player != null && LimbDataHelper.isRightArmDisabled(player)) {
            ci.cancel();
        }
    }

    @Inject(method = "renderLeftHand", at = @At("HEAD"), cancellable = true)
    private void hideLeftHandFirstPerson(PoseStack poseStack, SubmitNodeCollector submitNodeCollector, int lightCoords, Identifier skinTexture, boolean hasSleeve, CallbackInfo ci) {
        LocalPlayer player = Minecraft.getInstance().player;
        if (player != null && LimbDataHelper.isLeftArmDisabled(player)) {
            ci.cancel();
        }
    }
}
