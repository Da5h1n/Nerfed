package uk.da5h1n.nerfed.mixin;

import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.entity.state.HumanoidRenderState;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import uk.da5h1n.nerfed.util.ILimbRenderState;

@Mixin(HumanoidModel.class)
public class PlayerModelMixin {

    @Shadow @Final public ModelPart leftArm;
    @Shadow @Final public ModelPart rightArm;

    @Inject(method = "setupAnim(Lnet/minecraft/client/renderer/entity/state/HumanoidRenderState;)V", at = @At("TAIL"))
    private void hideDisabledLimbs(HumanoidRenderState state, CallbackInfo ci) {

        ILimbRenderState limbState = (ILimbRenderState) state;

        this.leftArm.visible = !limbState.nerfed$isLeftArmDisabled();
        this.rightArm.visible = !limbState.nerfed$isRightArmDisabled();
    }
}
