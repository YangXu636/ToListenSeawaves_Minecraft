package top.xuyangjerry.mcmod.tolistenseawaves.neoforge.prescripts.criterion;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.advancements.criterion.ContextAwarePredicate;
import net.minecraft.advancements.criterion.CriterionValidator;
import net.minecraft.advancements.criterion.DamagePredicate;
import net.minecraft.advancements.criterion.EntityPredicate;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.storage.loot.LootContext;
import top.xuyangjerry.mcmod.tolistenseawaves.neoforge.prescripts.PrescriptCriteriaTriggers;
import top.xuyangjerry.mcmod.tolistenseawaves.neoforge.prescripts.PrescriptCriterion;
import top.xuyangjerry.mcmod.tolistenseawaves.neoforge.prescripts.SimplePrescriptCriterionTrigger;

import java.util.Optional;

public class PlayerHurtEntityTrigger extends SimplePrescriptCriterionTrigger<PlayerHurtEntityTrigger.TriggerInstance> {

    @Override
    public Codec<TriggerInstance> codec() {
        return TriggerInstance.CODEC;
    }

    public void trigger(ServerPlayer player, Entity entity, DamageSource source, float amountDealt, float amountTaken, boolean blocked) {
        LootContext lootcontext = EntityPredicate.createContext(player, entity);
        this.trigger(player, (instance) -> instance.matches(player, lootcontext, source, amountDealt, amountTaken, blocked));
    }

    public static record TriggerInstance(Optional<ContextAwarePredicate> player, Optional<DamagePredicate> damage, Optional<ContextAwarePredicate> entity) implements SimplePrescriptCriterionTrigger.SimplePrescriptInstance {
        public static final Codec<TriggerInstance> CODEC = RecordCodecBuilder.create(instance ->
                instance.group(
                        EntityPredicate.ADVANCEMENT_CODEC.optionalFieldOf("player").forGetter(TriggerInstance::player),
                        DamagePredicate.CODEC.optionalFieldOf("damage").forGetter(TriggerInstance::damage),
                        EntityPredicate.ADVANCEMENT_CODEC.optionalFieldOf("entity").forGetter(TriggerInstance::entity)
                ).apply(instance, TriggerInstance::new)
        );

        public static PrescriptCriterion<TriggerInstance> playerHurtEntity() {
            return PrescriptCriteriaTriggers.PLAYER_HURT_ENTITY.get().createCriterion(new TriggerInstance(Optional.empty(), Optional.empty(), Optional.empty()));
        }

        public static PrescriptCriterion<TriggerInstance> playerHurtEntityWithDamage(Optional<DamagePredicate> damage) {
            return PrescriptCriteriaTriggers.PLAYER_HURT_ENTITY.get().createCriterion(new TriggerInstance(Optional.empty(), damage, Optional.empty()));
        }

        public static PrescriptCriterion<TriggerInstance> playerHurtEntityWithDamage(DamagePredicate.Builder damage) {
            return PrescriptCriteriaTriggers.PLAYER_HURT_ENTITY.get().createCriterion(new TriggerInstance(Optional.empty(), Optional.of(damage.build()), Optional.empty()));
        }

        public static PrescriptCriterion<TriggerInstance> playerHurtEntity(Optional<EntityPredicate> entity) {
            return PrescriptCriteriaTriggers.PLAYER_HURT_ENTITY.get().createCriterion(new TriggerInstance(Optional.empty(), Optional.empty(), EntityPredicate.wrap(entity)));
        }

        public static PrescriptCriterion<TriggerInstance> playerHurtEntity(Optional<DamagePredicate> damage, Optional<EntityPredicate> entity) {
            return PrescriptCriteriaTriggers.PLAYER_HURT_ENTITY.get().createCriterion(new TriggerInstance(Optional.empty(), damage, EntityPredicate.wrap(entity)));
        }

        public static PrescriptCriterion<TriggerInstance> playerHurtEntity(DamagePredicate.Builder damage, Optional<EntityPredicate> entity) {
            return PrescriptCriteriaTriggers.PLAYER_HURT_ENTITY.get().createCriterion(new TriggerInstance(Optional.empty(), Optional.of(damage.build()), EntityPredicate.wrap(entity)));
        }

        public boolean matches(ServerPlayer player, LootContext context, DamageSource damage, float dealt, float taken, boolean blocked) {
            return (this.damage.isEmpty() || this.damage.get().matches(player, damage, dealt, taken, blocked)) && (this.entity.isEmpty() || this.entity.get().matches(context));
        }

        public void validate(CriterionValidator validator) {
            SimplePrescriptInstance.super.validate(validator);
            validator.validateEntity(this.entity, "entity");
        }
    }
}
