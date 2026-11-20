package uk.da5h1n.nerfed.client;

import net.minecraft.resources.Identifier;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

public final class LiveUniforms {

    public static final Map<String, List<Number>> VALUES = new ConcurrentHashMap<>();
    public static final Map<String, Supplier<List<Number>>> SUPPLIERS = new ConcurrentHashMap<>();

    private LiveUniforms() {}

    public static void set(Identifier effect, List<Number> values) {
        VALUES.put(effect.toString(), values);
    }

    public static void setSupplier(Identifier effect, Supplier<List<Number>> supplier) {
        SUPPLIERS.put(effect.toString(), supplier);
    }

    public static List<Number> get(String effectId) {
        Supplier<List<Number>> supplier = SUPPLIERS.get(effectId);
        if (supplier != null) return supplier.get();
        return VALUES.get(effectId);
    }
}
