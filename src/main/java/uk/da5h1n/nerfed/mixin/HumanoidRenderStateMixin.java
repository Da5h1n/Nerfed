package uk.da5h1n.nerfed.mixin;

import net.minecraft.client.renderer.entity.state.HumanoidRenderState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import uk.da5h1n.nerfed.util.ILimbRenderState;

@Mixin(HumanoidRenderState.class)
public class HumanoidRenderStateMixin implements ILimbRenderState {
    @Unique public boolean nerfed$leftArmDisabled = false;
    @Unique public boolean nerfed$rightArmDisabled = false;
    @Unique public boolean nerfed$leftLegDisabled = false;
    @Unique public boolean nerfed$rightLegDisabled = false;

    @Override
    public boolean nerfed$isLeftArmDisabled() { return nerfed$leftArmDisabled; }
    @Override
    public void nerfed$setLeftArmDisabled(boolean disabled) { this.nerfed$leftArmDisabled = disabled; }

    @Override
    public boolean nerfed$isRightArmDisabled() { return nerfed$rightArmDisabled; }
    @Override
    public void nerfed$setRightArmDisabled(boolean disabled) { this.nerfed$rightArmDisabled = disabled; }

    @Override
    public boolean nerfed$isLeftLegDisabled() { return nerfed$leftLegDisabled; }
    @Override
    public void nerfed$setLeftLegDisabled(boolean disabled) { this.nerfed$leftLegDisabled = disabled; }

    @Override
    public boolean nerfed$isRightLegDisabled() { return nerfed$rightLegDisabled; }
    @Override
    public void nerfed$setRightLegDisabled(boolean disabled) { this.nerfed$rightLegDisabled = disabled; }
}
