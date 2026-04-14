package top.xuyangjerry.mcmod.tolistenseawaves.neoforge.prescripts;

import com.mojang.serialization.Codec;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import org.jetbrains.annotations.NotNull;
import top.xuyangjerry.mcmod.tolistenseawaves.neoforge.ToListenSeawaves;
import top.xuyangjerry.mcmod.tolistenseawaves.neoforge.init.ToListenSeawavesRegistries;
import top.xuyangjerry.mcmod.tolistenseawaves.neoforge.prescripts.criterion.AnyBlockInteractionTrigger;
import top.xuyangjerry.mcmod.tolistenseawaves.neoforge.prescripts.criterion.ImpossibleTrigger;
import top.xuyangjerry.mcmod.tolistenseawaves.neoforge.prescripts.criterion.SendMessageTrigger;

public class PrescriptCriteriaTriggers {
    public static final DeferredRegister<PrescriptCriterionTrigger<?>> PRESCRIPT_TRIGGERS =
            DeferredRegister.create(ToListenSeawavesRegistries.PRESCRIPT_TRIGGER_REGISTRY, ToListenSeawaves.MOD_ID);

    public static final Codec<PrescriptCriterionTrigger<?>> CODEC;
    public static final DeferredHolder<PrescriptCriterionTrigger<?>, @NotNull ImpossibleTrigger> IMPOSSIBLE = PRESCRIPT_TRIGGERS.register("impossible", ImpossibleTrigger::new);
    public static final DeferredHolder<PrescriptCriterionTrigger<?>, @NotNull SendMessageTrigger> SEND_MESSAGE = PRESCRIPT_TRIGGERS.register("send_message", SendMessageTrigger::new);
    public static final DeferredHolder<PrescriptCriterionTrigger<?>, @NotNull AnyBlockInteractionTrigger> ANY_BLOCK_USE = PRESCRIPT_TRIGGERS.register("any_block_use", AnyBlockInteractionTrigger::new);

    public PrescriptCriteriaTriggers() {
    }

    static {
        CODEC = Codec.lazyInitialized(() ->
                PRESCRIPT_TRIGGERS.getRegistry().get().byNameCodec()
        );
    }
}
