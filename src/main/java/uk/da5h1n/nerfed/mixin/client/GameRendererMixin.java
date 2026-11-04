package uk.da5h1n.nerfed.mixin.client;

import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.resources.Identifier;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import uk.da5h1n.nerfed.client.ColourblindRenderHandler;
import uk.da5h1n.nerfed.util.DisabilityDataHelper;

import java.util.List;

@Mixin(GameRenderer.class)
public abstract class GameRendererMixin {

    @Shadow @Final
    private List<Identifier> requestedPostEffects;

    @Inject(method = "update", at = @At("TAIL"))
    private void nerfed$addColourblindEffect(DeltaTracker deltaTracker, CallbackInfo ci) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) return;

        Identifier effect = ColourblindRenderHandler.effectFor(
                DisabilityDataHelper.getColorblindMode(mc.player));

        if (effect != null && !requestedPostEffects.contains(effect)) {
            requestedPostEffects.add(effect);
        }
    }
}
