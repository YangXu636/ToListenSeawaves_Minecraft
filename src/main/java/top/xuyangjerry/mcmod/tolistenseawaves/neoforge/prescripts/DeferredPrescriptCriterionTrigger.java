package top.xuyangjerry.mcmod.tolistenseawaves.neoforge.prescripts;

import net.minecraft.resources.ResourceKey;
import net.neoforged.neoforge.registries.DeferredHolder;
import org.jetbrains.annotations.NotNull;

public class DeferredPrescriptCriterionTrigger<T extends PrescriptCriterionTrigger<?>> extends DeferredHolder<PrescriptCriterionTrigger<?>, @NotNull T> {

    protected DeferredPrescriptCriterionTrigger(ResourceKey<PrescriptCriterionTrigger<?>> key) {
        super(key);
    }
}
