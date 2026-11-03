package uk.da5h1n.nerfed.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.FirstPersonHandsAndItemsRenderer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import net.minecraft.client.renderer.state.level.FirstPersonHandsAndItemsRenderState;
import net.minecraft.client.renderer.state.level.PlayerRenderState;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import uk.da5h1n.nerfed.util.ILimbRenderState;

@Mixin(FirstPersonHandsAndItemsRenderer.class)
public abstract class ItemInHandRendererMixin {

    @Inject(
            method = "submitArmWithItem",
            at = @At("HEAD"),
            cancellable = true
    )
    private void nerfed$hideDisabledFirstPersonHandAndItem(PlayerRenderState playerState, FirstPersonHandsAndItemsRenderState state, float partialTicks, float xRot, InteractionHand hand, float attack, ItemStack itemStack, float inverseArmHeight, PoseStack poseStack, SubmitNodeCollector submitNodeCollector, int lightCoords, CallbackInfo ci) {

        AvatarRenderState avatarRenderState = playerState.avatarRenderState;
        if (avatarRenderState != null) {
            ILimbRenderState limbState = (ILimbRenderState) avatarRenderState;

            boolean isMainHand = hand == InteractionHand.MAIN_HAND;
            HumanoidArm arm = isMainHand ? avatarRenderState.mainArm : avatarRenderState.mainArm.getOpposite();

            // Cancel rendering for this hand/item if the corresponding arm is disabled
            if (arm == HumanoidArm.LEFT && limbState.nerfed$isLeftArmDisabled()) {
                ci.cancel();
            }
            if (arm == HumanoidArm.RIGHT && limbState.nerfed$isRightArmDisabled()) {
                ci.cancel();
            }
        }
    }
}