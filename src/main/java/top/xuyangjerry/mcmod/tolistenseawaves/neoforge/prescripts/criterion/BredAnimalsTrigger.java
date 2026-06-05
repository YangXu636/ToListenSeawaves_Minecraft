package top.xuyangjerry.mcmod.tolistenseawaves.neoforge.prescripts.criterion;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.advancements.criterion.ContextAwarePredicate;
import net.minecraft.advancements.criterion.CriterionValidator;
import net.minecraft.advancements.criterion.EntityPredicate;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.level.storage.loot.LootContext;
import org.jspecify.annotations.Nullable;
import top.xuyangjerry.mcmod.tolistenseawaves.neoforge.prescripts.SimplePrescriptCriterionTrigger;

import java.util.Optional;
import java.util.function.Supplier;

public class BredAnimalsTrigger extends SimplePrescriptCriterionTrigger<BredAnimalsTrigger.TriggerInstance> {

    @Override
    public Codec<TriggerInstance> codec() {
        return TriggerInstance.CODEC;
    }

    public void trigger(ServerPlayer player, Animal parent, Animal partner, @Nullable AgeableMob child) {
        LootContext lootcontext = EntityPredicate.createContext(player, parent);
        LootContext lootcontext1 = EntityPredicate.createContext(player, partner);
        LootContext lootcontext2 = child != null ? EntityPredicate.createContext(player, child) : null;
        this.trigger(player, (instance) -> instance.matches(lootcontext, lootcontext1, lootcontext2));
    }

    public static record TriggerInstance(Optional<ContextAwarePredicate> player, Optional<ContextAwarePredicate> parent, Optional<ContextAwarePredicate> partner, Optional<ContextAwarePredicate> child) implements SimplePrescriptCriterionTrigger.SimplePrescriptInstance {
        public static final Codec<TriggerInstance> CODEC = RecordCodecBuilder.create(
                (instance) -> instance.group(
                        EntityPredicate.ADVANCEMENT_CODEC.optionalFieldOf("player").forGetter(TriggerInstance::player),
                        EntityPredicate.ADVANCEMENT_CODEC.optionalFieldOf("parent").forGetter(TriggerInstance::parent),
                        EntityPredicate.ADVANCEMENT_CODEC.optionalFieldOf("partner").forGetter(TriggerInstance::partner),
                        EntityPredicate.ADVANCEMENT_CODEC.optionalFieldOf("child").forGetter(TriggerInstance::child)
                ).apply(instance, TriggerInstance::new));

        public boolean matches(LootContext parentContext, LootContext partnerContext, @Nullable LootContext childContext) {
            return (this.child.isEmpty() || (childContext != null && this.child.get().matches(childContext)))
                    && (matches(() -> this.parent, parentContext) && matches(() -> this.partner, partnerContext)
                        || matches(() -> this.parent, partnerContext) && matches(() -> this.partner, parentContext)
                    );
        }

        private static boolean matches(Supplier<Optional<ContextAwarePredicate> > predicate, LootContext context) {
            return predicate.get().isEmpty() || predicate.get().get().matches(context);
        }

        public void validate(CriterionValidator validator) {
            SimplePrescriptInstance.super.validate(validator);
            validator.validateEntity(this.parent, "parent");
            validator.validateEntity(this.partner, "partner");
            validator.validateEntity(this.child, "child");
        }
    }
}
