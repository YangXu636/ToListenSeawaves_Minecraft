package top.xuyangjerry.mcmod.tolistenseawaves.neoforge.prescripts;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.advancements.CriterionTriggerInstance;
import net.minecraft.util.ExtraCodecs;

public record PrescriptCriterion<T extends CriterionTriggerInstance>(PrescriptCriterionTrigger<T> trigger, T triggerInstance) {
    private static final MapCodec<PrescriptCriterion<?>> MAP_CODEC;
    public static final Codec<PrescriptCriterion<?>> CODEC;

    public PrescriptCriterion(PrescriptCriterionTrigger<T> trigger, T triggerInstance) {
        this.trigger = trigger;
        this.triggerInstance = triggerInstance;
    }

    private static <T extends CriterionTriggerInstance> Codec<PrescriptCriterion<T>> criterionCodec(PrescriptCriterionTrigger<T> trigger) {
        return trigger.codec().xmap((t) -> new PrescriptCriterion<>(trigger, t), PrescriptCriterion::triggerInstance);
    }

    public PrescriptCriterionTrigger<T> trigger() {
        return this.trigger;
    }

    public T triggerInstance() {
        return this.triggerInstance;
    }

    static {
        MAP_CODEC = ExtraCodecs.dispatchOptionalValue("trigger", "conditions", PrescriptCriteriaTriggers.CODEC, PrescriptCriterion::trigger, PrescriptCriterion::criterionCodec);
        CODEC = MAP_CODEC.codec();
    }
}
