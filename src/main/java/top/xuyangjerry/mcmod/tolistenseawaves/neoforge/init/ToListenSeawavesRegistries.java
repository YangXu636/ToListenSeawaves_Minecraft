package top.xuyangjerry.mcmod.tolistenseawaves.neoforge.init;

import net.minecraft.core.Registry;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import top.xuyangjerry.mcmod.tolistenseawaves.neoforge.ToListenSeawaves;
import top.xuyangjerry.mcmod.tolistenseawaves.neoforge.prescripts.Prescript;
import top.xuyangjerry.mcmod.tolistenseawaves.neoforge.prescripts.PrescriptCriterionTrigger;

public class ToListenSeawavesRegistries {

    public static final ResourceKey<Registry<Prescript>> PRESCRIPT = createRegistryKey("prescript");
    public static final ResourceKey<Registry<PrescriptCriterionTrigger<?>>> PRESCRIPT_TRIGGER_TYPE = createRegistryKey("prescript_trigger_type");

    public ToListenSeawavesRegistries(){}

    private static <T> ResourceKey<Registry<T>> createRegistryKey(String name) {
        return ResourceKey.createRegistryKey(Identifier.fromNamespaceAndPath(ToListenSeawaves.MODID, name));
    }
}
