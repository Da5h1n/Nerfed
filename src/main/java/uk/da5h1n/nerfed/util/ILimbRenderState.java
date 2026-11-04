package uk.da5h1n.nerfed.util;

public interface ILimbRenderState {
    boolean nerfed$isLeftArmDisabled();
    void nerfed$setLeftArmDisabled(boolean disabled);

    boolean nerfed$isRightArmDisabled();
    void nerfed$setRightArmDisabled(boolean disabled);

    boolean nerfed$isLeftLegDisabled();
    void nerfed$setLeftLegDisabled(boolean disabled);

    boolean nerfed$isRightLegDisabled();
    void nerfed$setRightLegDisabled(boolean disabled);
}
