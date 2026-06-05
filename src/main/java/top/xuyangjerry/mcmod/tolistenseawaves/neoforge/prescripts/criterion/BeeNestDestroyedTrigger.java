package top.xuyangjerry.mcmod.tolistenseawaves.neoforge.prescripts.criterion;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.advancements.criterion.ContextAwarePredicate;
import net.minecraft.advancements.criterion.EntityPredicate;
import net.minecraft.advancements.criterion.ItemPredicate;
import net.minecraft.advancements.criterion.MinMaxBounds;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import top.xuyangjerry.mcmod.tolistenseawaves.neoforge.prescripts.PrescriptCriteriaTriggers;
import top.xuyangjerry.mcmod.tolistenseawaves.neoforge.prescripts.PrescriptCriterion;
import top.xuyangjerry.mcmod.tolistenseawaves.neoforge.prescripts.SimplePrescriptCriterionTrigger;

import java.util.Optional;

public class BeeNestDestroyedTrigger extends SimplePrescriptCriterionTrigger<BeeNestDestroyedTrigger.TriggerInstance> {

    public BeeNestDestroyedTrigger() {}

    @Override
    public Codec<TriggerInstance> codec() {
        return TriggerInstance.CODEC;
    }

    public void trigger(ServerPlayer player, BlockState state, ItemStack stack, int numBees) {
        this.trigger(player, (instance) -> instance.matches(state, stack, numBees));
    }

    public static record TriggerInstance(Optional<ContextAwarePredicate> player, Optional<Holder<Block>> block, Optional<ItemPredicate> item, MinMaxBounds.Ints beesInside) implements SimplePrescriptCriterionTrigger.SimplePrescriptInstance{
        public static final Codec<TriggerInstance> CODEC = RecordCodecBuilder.create(
                (instance) -> instance.group(
                        EntityPredicate.ADVANCEMENT_CODEC.optionalFieldOf("player").forGetter(TriggerInstance::player),
                        BuiltInRegistries.BLOCK.holderByNameCodec().optionalFieldOf("block").forGetter(TriggerInstance::block),
                        ItemPredicate.CODEC.optionalFieldOf("item").forGetter(TriggerInstance::item),
                        MinMaxBounds.Ints.CODEC.optionalFieldOf("num_bees_inside", MinMaxBounds.Ints.ANY).forGetter(TriggerInstance::beesInside)
                ).apply(instance, TriggerInstance::new));

        public static PrescriptCriterion<TriggerInstance> destroyedBeeNest(Block block, ItemPredicate.Builder item, MinMaxBounds.Ints numBees) {
            return PrescriptCriteriaTriggers.BEE_NEST_DESTROYED.get().createCriterion(new TriggerInstance(Optional.empty(), Optional.of(block.builtInRegistryHolder()), Optional.of(item.build()), numBees));
        }

        public boolean matches(BlockState state, ItemStack stack, int numBees) {
            if (this.block.isPresent() && !state.is(this.block.get())) {
                return false;
            }
            return (this.item.isEmpty() || this.item.get().test(stack)) && this.beesInside.matches(numBees);
        }
    }
}
