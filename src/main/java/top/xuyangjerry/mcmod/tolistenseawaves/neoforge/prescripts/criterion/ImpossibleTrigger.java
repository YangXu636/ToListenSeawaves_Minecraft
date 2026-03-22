package top.xuyangjerry.mcmod.tolistenseawaves.neoforge.prescripts.criterion;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import net.minecraft.advancements.CriterionTriggerInstance;
import net.minecraft.advancements.criterion.CriterionValidator;
import org.jetbrains.annotations.NotNull;
import top.xuyangjerry.mcmod.tolistenseawaves.neoforge.prescripts.PrescriptCriterionTrigger;
import top.xuyangjerry.mcmod.tolistenseawaves.neoforge.server.PlayerPrescripts;

public class ImpossibleTrigger implements PrescriptCriterionTrigger<ImpossibleTrigger.TriggerInstance> {
    @Override
    public void addPlayerListener(PlayerPrescripts var1, Listener var2) {

    }

    @Override
    public void removePlayerListener(PlayerPrescripts var1, Listener var2) {

    }

    @Override
    public void removePlayerListeners(PlayerPrescripts var1) {

    }

    @Override
    public Codec<TriggerInstance> codec() {
        return TriggerInstance.CODEC;
    }

    public static record TriggerInstance() implements CriterionTriggerInstance {
        public static final Codec<TriggerInstance> CODEC = MapCodec.unitCodec(new TriggerInstance());

        @Override
        public void validate(@NotNull CriterionValidator criterionValidator) {

        }
    }
}
