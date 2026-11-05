package uk.da5h1n.nerfed.mixin;

import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import uk.da5h1n.nerfed.client.SchizophreniaHandler;

@Mixin(LivingEntity.class)
public abstract class LivingEntityPhantomMixin {

    @Inject(method = "isPickable", at = @At("HEAD"), cancellable = true)
    private void nerfed$notPickable(CallbackInfoReturnable<Boolean> cir) {
        if (SchizophreniaHandler.isPhantom((LivingEntity) (Object) this)) {
            cir.setReturnValue(false);
        }
    }

    @Inject(method = "isPushable", at = @At("HEAD"), cancellable = true)
    private void nerfed$notPushable(CallbackInfoReturnable<Boolean> cir) {
        if (SchizophreniaHandler.isPhantom((LivingEntity) (Object) this)) {
            cir.setReturnValue(false);
        }
    }
}