package top.xuyangjerry.mcmod.tolistenseawaves.neoforge.init;

import com.mojang.serialization.Lifecycle;
import net.minecraft.core.MappedRegistry;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceKey;
import top.xuyangjerry.mcmod.tolistenseawaves.neoforge.prescripts.PrescriptCriteriaTriggers;
import top.xuyangjerry.mcmod.tolistenseawaves.neoforge.prescripts.PrescriptCriterionTrigger;
import top.xuyangjerry.mcmod.tolistenseawaves.neoforge.tool.XyTools;

import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.function.Consumer;

public class ToListenSeawavesBuiltInRegistries {
    public static final Registry<PrescriptCriterionTrigger<?>> PRESCRIPT_TRIGGER_TYPES = registerSimple(ToListenSeawavesRegistries.PRESCRIPT_TRIGGER_TYPE, PrescriptCriteriaTriggers::bootstrap);

    public static <T> Registry<T> registerSimple(ResourceKey<? extends Registry<T>> key, Consumer<Registry<T>> bootstrap) {
        try {
            return (Registry<T>) XyTools.Reflection.RunMethod(BuiltInRegistries.class, null, "internalRegister", List.of(key,  new MappedRegistry<>(key, Lifecycle.stable(), false), bootstrap));
        } catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException e) {
            throw new RuntimeException(e);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
