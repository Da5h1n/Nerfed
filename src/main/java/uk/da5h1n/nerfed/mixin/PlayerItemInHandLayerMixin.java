package uk.da5h1n.nerfed.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.layers.PlayerItemInHandLayer;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.item.ItemStack;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import uk.da5h1n.nerfed.util.ILimbRenderState;

@Mixin(PlayerItemInHandLayer.class)
public abstract class PlayerItemInHandLayerMixin {

    @Inject(
            method = "submitArmWithItem",
            at = @At("HEAD"),
            cancellable = true
    )
    private void nerfed$hideDisabledArmItem(
            AvatarRenderState state,
            ItemStackRenderState item,
            ItemStack itemStack,
            HumanoidArm arm,
            PoseStack poseStack,
            SubmitNodeCollector collector,
            int lightCoords,
            CallbackInfo ci
    ) {
        ILimbRenderState limbState = (ILimbRenderState) state;

        if (arm == HumanoidArm.LEFT &&
                limbState.nerfed$isLeftArmDisabled()) {
            ci.cancel();
        }

        if (arm == HumanoidArm.RIGHT &&
                limbState.nerfed$isRightArmDisabled()) {
            ci.cancel();
        }
    }
}