package uk.da5h1n.nerfed.mixin.client;

import com.mojang.blaze3d.audio.Channel;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import uk.da5h1n.nerfed.client.DeafnessEffect;

@Mixin(Channel.class)
public abstract class ChannelMixin {

    @Shadow @Final
    private int source;

    @Inject(method = "setVolume", at = @At("TAIL"))
    private void nerfed$applyMuffle(float volume, CallbackInfo ci) {
        DeafnessEffect.applyLowPass(this.source);
    }
}
