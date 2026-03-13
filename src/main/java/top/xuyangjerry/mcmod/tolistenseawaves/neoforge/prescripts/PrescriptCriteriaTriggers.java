package top.xuyangjerry.mcmod.tolistenseawaves.neoforge.prescripts;

import com.mojang.serialization.Codec;
import net.minecraft.core.Registry;
import top.xuyangjerry.mcmod.tolistenseawaves.neoforge.init.ToListenSeawavesBuiltInRegistries;

public class PrescriptCriteriaTriggers {
    public static final Codec<PrescriptCriterionTrigger<?>> CODEC;

    public PrescriptCriteriaTriggers() {
    }

    public static <T extends PrescriptCriterionTrigger<?>> T register(String name, T trigger) {
        return Registry.register(ToListenSeawavesBuiltInRegistries.PRESCRIPT_TRIGGER_TYPES, name, trigger);
    }

    public static PrescriptCriterionTrigger<?> bootstrap(Registry<PrescriptCriterionTrigger<?>> registry) {
        return IMPOSSIBLE;
    }

    static {
        CODEC = ToListenSeawavesBuiltInRegistries.PRESCRIPT_TRIGGER_TYPES.byNameCodec();
    }
}
