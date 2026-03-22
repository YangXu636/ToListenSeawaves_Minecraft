package top.xuyangjerry.mcmod.tolistenseawaves.neoforge.init;

import net.minecraft.core.Registry;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.neoforged.neoforge.registries.*;
import top.xuyangjerry.mcmod.tolistenseawaves.neoforge.ToListenSeawaves;
import top.xuyangjerry.mcmod.tolistenseawaves.neoforge.prescripts.Prescript;
import top.xuyangjerry.mcmod.tolistenseawaves.neoforge.prescripts.PrescriptCriterionTrigger;

public class ToListenSeawavesRegistries {

    public static final ResourceKey<Registry<Prescript>> PRESCRIPT = createRegistryKey("prescript");
    public static final ResourceKey<Registry<PrescriptCriterionTrigger<?>>> PRESCRIPT_TRIGGER_TYPE = createRegistryKey("prescript_trigger_type");

    /*public static final Registry<Prescript> PRESCRIPT_REGISTRY =
            new RegistryBuilder<>(PRESCRIPT)
                    .sync(true)
                    // .defaultKey(Identifier.fromNamespaceAndPath(ToListenSeawaves.MOD_ID, "empty")) // 默认键（类似minecraft:air）
                    // .maxId(256) // 可选：限制最大ID
                    .create(); // 构建注册表*/

    public static final Registry<PrescriptCriterionTrigger<?>> PRESCRIPT_TRIGGER_REGISTRY =
            new RegistryBuilder<>(PRESCRIPT_TRIGGER_TYPE)
                    .sync(true)
                    .create();

    public ToListenSeawavesRegistries(){}

    private static <T> ResourceKey<Registry<T>> createRegistryKey(String name) {
        return ResourceKey.createRegistryKey(Identifier.fromNamespaceAndPath(ToListenSeawaves.MOD_ID, name));
    }

    //@SubscribeEvent
    public static void registerCustomRegistries(NewRegistryEvent event) {
        //event.register(ToListenSeawavesRegistries.PRESCRIPT_REGISTRY);
        event.register(ToListenSeawavesRegistries.PRESCRIPT_TRIGGER_REGISTRY);
    }

    //@SubscribeEvent
    public static void registerDatapackRegistries(DataPackRegistryEvent.NewRegistry event) {
        event.dataPackRegistry(
                // The registry key.
                ToListenSeawavesRegistries.PRESCRIPT,
                // The codec of the registry contents.
                Prescript.CODEC,
                // The network codec of the registry contents. Often identical to the normal codec.
                // May be a reduced variant of the normal codec that omits data that is not needed on the client.
                // May be null. If null, registry entries will not be synced to the client at all.
                // May be omitted, which is functionally identical to passing null (a method overload
                // with two parameters is called that passes null to the normal three parameter method).
                Prescript.CODEC
                // A consumer which configures the constructed registry via the RegistryBuilder.
                // May be omitted, which is functionally identical to passing builder -> {}.
                // builder -> builder.maxId(256)
        );
    }
}
