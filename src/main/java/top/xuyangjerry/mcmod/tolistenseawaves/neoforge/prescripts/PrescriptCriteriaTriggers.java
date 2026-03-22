package top.xuyangjerry.mcmod.tolistenseawaves.neoforge.prescripts;

import com.mojang.serialization.Codec;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import top.xuyangjerry.mcmod.tolistenseawaves.neoforge.ToListenSeawaves;
import top.xuyangjerry.mcmod.tolistenseawaves.neoforge.init.ToListenSeawavesRegistries;
import top.xuyangjerry.mcmod.tolistenseawaves.neoforge.prescripts.criterion.ImpossibleTrigger;

public class PrescriptCriteriaTriggers {
    public static final DeferredRegister<PrescriptCriterionTrigger<?>> PRESCRIPT_TRIGGERS =
            DeferredRegister.create(ToListenSeawavesRegistries.PRESCRIPT_TRIGGER_REGISTRY, ToListenSeawaves.MOD_ID);

    public static final Codec<PrescriptCriterionTrigger<?>> CODEC;
    public static final DeferredHolder<PrescriptCriterionTrigger<?>, ImpossibleTrigger> IMPOSSIBLE = PRESCRIPT_TRIGGERS.register("impossible", ImpossibleTrigger::new);

    public PrescriptCriteriaTriggers() {
    }

    static {
        CODEC = Codec.lazyInitialized(() ->
                PRESCRIPT_TRIGGERS.getRegistry().get().byNameCodec()
        );
    }
}
