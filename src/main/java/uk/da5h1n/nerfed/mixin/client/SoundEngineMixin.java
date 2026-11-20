package uk.da5h1n.nerfed.mixin.client;

import net.minecraft.client.sounds.SoundEngine;
import net.minecraft.sounds.SoundSource;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import uk.da5h1n.nerfed.client.DeafnessEffect;

@Mixin(SoundEngine.class)
public abstract class SoundEngineMixin {

    @Shadow
    public abstract void refreshCategoryVolume(SoundSource source);

    @Inject(method = "calculateVolume(FLnet/minecraft/sounds/SoundSource;)F", at = @At("RETURN"), cancellable = true)
    private void nerfed$muffleVolume(float volume, SoundSource source, CallbackInfoReturnable<Float> cir) {
        cir.setReturnValue(cir.getReturnValueF() * DeafnessEffect.volumeFactor());
    }

    @Inject(method = "tick", at = @At("HEAD"))
    private void nerfed$tickDeafness(boolean paused, CallbackInfo ci) {
        if (DeafnessEffect.tick()) {
            this.refreshCategoryVolume(SoundSource.MASTER);
        }
    }

    @Inject(method = "destroy", at = @At("HEAD"))
    private void nerfed$resetAudio(CallbackInfo ci) {
        DeafnessEffect.resetAudio();
    }
}
