package uk.da5h1n.nerfed.component;

import net.minecraft.nbt.CompoundTag;

public class LimbDataComponent {
    private boolean leftArmDisabled = false;
    private boolean rightArmDisabled = false;

    public boolean isLeftArmDisabled() { return leftArmDisabled; }
    public void setLeftArmDisabled(boolean disabled) { this.leftArmDisabled = disabled; }

    public boolean isRightArmDisabled() { return rightArmDisabled; }
    public void setRightArmDisabled(boolean disabled) { this.rightArmDisabled = disabled; }

    public void writeToNbt(CompoundTag tag) {
        tag.putBoolean("LeftArmDisabled", leftArmDisabled);
        tag.putBoolean("RightArmDisabled", rightArmDisabled);
    }

    public void readFromNbt(CompoundTag tag) {
        this.leftArmDisabled = tag.getBoolean("LeftArmDisabled").orElse(false);
        this.rightArmDisabled = tag.getBoolean("RightArmDisabled").orElse(false);
    }
}
