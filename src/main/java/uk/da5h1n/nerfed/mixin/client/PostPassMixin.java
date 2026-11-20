package uk.da5h1n.nerfed.mixin.client;


import com.mojang.blaze3d.buffers.Std140Builder;
import com.mojang.blaze3d.buffers.Std140SizeCalculator;
import com.mojang.blaze3d.framegraph.FrameGraphBuilder;
import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.resource.ResourceHandle;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.renderpearl.api.buffers.GpuBuffer;
import com.mojang.renderpearl.api.buffers.GpuBufferSlice;
import net.minecraft.client.renderer.PostPass;
import org.intellij.lang.annotations.Identifier;
import org.lwjgl.system.MemoryStack;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import uk.da5h1n.nerfed.client.LiveUniforms;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Mixin(PostPass.class)
public abstract class PostPassMixin {

    @Shadow @Final private String name;
    @Shadow @Final private Map<String, GpuBuffer> customUniforms;

    @Unique private final Map<String, GpuBuffer[]> nerfed$rings = new HashMap<>();
    @Unique private int nerfed$cursor = 1;

    @Inject(method = "addToFrame", at = @At("HEAD"))
    private void nerfed$applyLiveUniforms(FrameGraphBuilder frame,
                                          Map<Identifier, ResourceHandle<RenderTarget>> targets,
                                          GpuBufferSlice shaderOrthoMatrix,
                                          CallbackInfo ci) {
        if (customUniforms.isEmpty()) return;

        int slash = name.lastIndexOf('/');
        if (slash < 0) return;
        List<Number> values = LiveUniforms.get(name.substring(0, slash));
        if (values == null) return;

        int slot = nerfed$cursor;
        nerfed$cursor = (nerfed$cursor + 1) % 3;

        for (String key : new ArrayList<>(customUniforms.keySet())) {
            GpuBuffer[] ring = nerfed$rings.computeIfAbsent(key, k -> {
                GpuBuffer[] created = new GpuBuffer[3];
                created[0] = customUniforms.get(k);
                return created;
            });

            GpuBuffer fresh = nerfed$build(name + " / " + key + " (live)", values);

            GpuBuffer old = ring[slot];
            if (old != null) old.close();
            ring[slot] = fresh;
            customUniforms.put(key, fresh);
        }
    }

    @Inject(method = "close", at = @At("HEAD"))
    private void nerfed$closeRings(CallbackInfo ci) {
        for (Map.Entry<String, GpuBuffer[]> entry : nerfed$rings.entrySet()) {
            GpuBuffer current = customUniforms.get(entry.getKey());
            for (GpuBuffer buffer : entry.getValue()) {
                // the one in customUniforms is closed by PostPass.close() itself
                if (buffer != null && buffer != current) buffer.close();
            }
        }
        nerfed$rings.clear();
    }

    @Unique
    private static GpuBuffer nerfed$build(String label, List<Number> values) {
        Std140SizeCalculator calculator = new Std140SizeCalculator();
        for (Number value : values) {
            if (value instanceof Integer) calculator.putInt();
            else calculator.putFloat();
        }
        int size = calculator.get();

        try (MemoryStack stack = MemoryStack.stackPush()) {
            Std140Builder builder = Std140Builder.onStack(stack, size);
            for (Number value : values) {
                if (value instanceof Integer i) builder.putInt(i);
                else builder.putFloat(value.floatValue());
            }
            return RenderSystem.getDevice().createBuffer(() -> label, 128, builder.get());
        }
    }
}
