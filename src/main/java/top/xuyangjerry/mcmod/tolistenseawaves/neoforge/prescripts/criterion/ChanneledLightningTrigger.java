package top.xuyangjerry.mcmod.tolistenseawaves.neoforge.prescripts.criterion;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.advancements.criterion.ContextAwarePredicate;
import net.minecraft.advancements.criterion.CriterionValidator;
import net.minecraft.advancements.criterion.EntityPredicate;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.storage.loot.LootContext;
import top.xuyangjerry.mcmod.tolistenseawaves.neoforge.prescripts.SimplePrescriptCriterionTrigger;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class ChanneledLightningTrigger extends SimplePrescriptCriterionTrigger<ChanneledLightningTrigger.TriggerInstance> {

    @Override
    public Codec<TriggerInstance> codec() {
        return TriggerInstance.CODEC;
    }

    public void trigger(ServerPlayer player, Collection<? extends Entity> entityTriggered) {
        List<LootContext> list = entityTriggered.stream().map((entity) -> EntityPredicate.createContext(player, entity)).collect(Collectors.toList());
        this.trigger(player, (instance) -> instance.matches(list));
    }

    public static record TriggerInstance(Optional<ContextAwarePredicate> player, List<ContextAwarePredicate> victims) implements SimplePrescriptInstance {
        public static final Codec<TriggerInstance> CODEC = RecordCodecBuilder.create(
                (instance) -> instance.group(
                        EntityPredicate.ADVANCEMENT_CODEC.optionalFieldOf("player").forGetter(TriggerInstance::player),
                        EntityPredicate.ADVANCEMENT_CODEC.listOf().optionalFieldOf("victims", List.of()).forGetter(TriggerInstance::victims)
                ).apply(instance, TriggerInstance::new));

        public boolean matches(Collection<? extends LootContext> victims) {
            return this.victims.stream().allMatch(p -> victims.stream().anyMatch(p::matches));
        }

        public void validate(CriterionValidator validator) {
            SimplePrescriptInstance.super.validate(validator);
            validator.validateEntities(this.victims, "victims");
        }
    }
}
