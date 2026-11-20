package uk.da5h1n.nerfed.item;

import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.Item;
import uk.da5h1n.nerfed.Nerfed;

public class ModItems {

    public static final ResourceKey<Item> MEDICAL_CHART_KEY = ResourceKey.create(Registries.ITEM, Identifier.fromNamespaceAndPath(Nerfed.MOD_ID, "medical_chart"));
    public static final ResourceKey<Item> WALKING_STICK_KEY = ResourceKey.create(Registries.ITEM, Identifier.fromNamespaceAndPath(Nerfed.MOD_ID, "walking_stick"));

    public static final Item MEDICAL_CHART = Registry.register(
            BuiltInRegistries.ITEM,
            MEDICAL_CHART_KEY,
            new MedicalChartItem(new Item.Properties().setId(MEDICAL_CHART_KEY).stacksTo(1))
    );

    public static final Item WALKING_STICK = Registry.register(
            BuiltInRegistries.ITEM,
            WALKING_STICK_KEY,
            new WalkingStickItem(new Item.Properties().setId(WALKING_STICK_KEY).stacksTo(1))
    );

    public static void init() {}
}
