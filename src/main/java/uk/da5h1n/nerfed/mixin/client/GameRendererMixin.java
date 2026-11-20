package uk.da5h1n.nerfed.mixin.client;

import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.resource.CrossFrameResourcePool;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.PauseScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.PostChain;
import net.minecraft.resources.Identifier;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import uk.da5h1n.nerfed.Nerfed;
import uk.da5h1n.nerfed.client.ColourblindRenderHandler;
import uk.da5h1n.nerfed.client.FilterFader;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

@Mixin(GameRenderer.class)
public abstract class GameRendererMixin {

    @Shadow @Final private List<Identifier> requestedPostEffects;
    @Shadow @Final private List<PostChain> appliedPostEffects;
    @Shadow @Final private List<Identifier> failedPostEffects;
    @Shadow @Final private RenderTarget mainRenderTarget;
    @Shadow @Final private CrossFrameResourcePool resourcePool;

    @Unique
    private final List<PostChain> nerfed$guiEffects = new ArrayList<>();

    @Unique
    private static boolean nerfed$filterGui() {
        Screen screen = Minecraft.getInstance().gui.screen();
        if (screen == null) return true;
        if (screen instanceof PauseScreen) return false;
        return screen.isInGameUi();
    }

    @Inject(method = "update", at = @At("TAIL"))
    private void nerfed$addDisabilityEffects(DeltaTracker deltaTracker, CallbackInfo ci) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) {
            FilterFader.reset();
            return;
        }

        for (Identifier effect : ColourblindRenderHandler.effectsFor(mc.player)) {
            if (!requestedPostEffects.contains(effect)) {
                requestedPostEffects.add(effect);
            }
        }
    }

    @Inject(method = "preparePostEffects", at = @At("TAIL"))
    private void nerfed$deferEffects(List<Identifier> requested, CallbackInfo ci) {
        nerfed$guiEffects.clear();

        if (!nerfed$filterGui()) return;

        Iterator<PostChain> it = appliedPostEffects.iterator();
        while (it.hasNext()) {
            PostChain chain = it.next();
            if (chain.id().getNamespace().equals(Nerfed.MOD_ID) && !ColourblindRenderHandler.isWorldOnly(chain.id())) {
                nerfed$guiEffects.add(chain);
                it.remove();
            }
        }
    }

    // 3. Apply them after the GUI has been drawn, so the HUD and screens are filtered too
    @Inject(
            method = "render",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/gui/render/GuiRenderer;endFrame()V",
                    shift = At.Shift.AFTER
            )
    )
    private void nerfed$applyAfterGui(CallbackInfo ci) {
        for (PostChain chain : nerfed$guiEffects) {
            try {
                chain.process(mainRenderTarget, resourcePool);
            } catch (RuntimeException e) {
                Nerfed.LOGGER.warn("Failed to apply post effect {}", chain.id(), e);
                failedPostEffects.add(chain.id());
            }
        }
    }
}
