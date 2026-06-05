package top.xuyangjerry.mcmod.tolistenseawaves.neoforge.prescripts.criterion;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.advancements.criterion.ContextAwarePredicate;
import net.minecraft.advancements.criterion.CriterionValidator;
import net.minecraft.advancements.criterion.MinMaxBounds;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import top.xuyangjerry.mcmod.tolistenseawaves.neoforge.ToListenSeawaves;
import top.xuyangjerry.mcmod.tolistenseawaves.neoforge.prescripts.PrescriptCriteriaTriggers;
import top.xuyangjerry.mcmod.tolistenseawaves.neoforge.prescripts.SimplePrescriptCriterionTrigger;

import java.util.Optional;

@EventBusSubscriber(modid = ToListenSeawaves.MOD_ID)
public class AnyBlockInteractionTrigger extends SimplePrescriptCriterionTrigger<AnyBlockInteractionTrigger.TriggerInstance> {
    @Override
    public Codec<TriggerInstance> codec() {
        return TriggerInstance.CODEC;
    }

    public void trigger(ServerPlayer player, BlockPos pos, ItemStack stack) {
        ServerLevel serverlevel = player.level();
        BlockState blockstate = serverlevel.getBlockState(pos);
        LootParams lootparams = (new LootParams.Builder(serverlevel))
                .withParameter(LootContextParams.ORIGIN, pos.getCenter())
                .withParameter(LootContextParams.THIS_ENTITY, player)
                .withParameter(LootContextParams.BLOCK_STATE, blockstate)
                .withParameter(LootContextParams.TOOL, stack)
                .create(LootContextParamSets.ADVANCEMENT_LOCATION);
        LootContext lootcontext = (new LootContext.Builder(lootparams)).create(Optional.empty());
        this.trigger(player, (instance) -> instance.matches(lootcontext));
    }

    public static record TriggerInstance(Optional<ContextAwarePredicate> player, Optional<ContextAwarePredicate> location) implements SimplePrescriptCriterionTrigger.SimplePrescriptInstance {
        public static final Codec<TriggerInstance> CODEC = RecordCodecBuilder.create(instance ->
                instance.group(
                        ContextAwarePredicate.CODEC.optionalFieldOf("player").forGetter(TriggerInstance::player),
                        ContextAwarePredicate.CODEC.optionalFieldOf("location").forGetter(TriggerInstance::location)
                ).apply(instance, TriggerInstance::new)
        );

        public boolean matches(LootContext context) {
            return this.location.isEmpty() || (this.location.get()).matches(context);
        }

        public void validate(CriterionValidator validator) {
            SimplePrescriptInstance.super.validate(validator);
            this.location.ifPresent((cap) -> validator.validate(cap, LootContextParamSets.ADVANCEMENT_LOCATION, "location"));
        }
    }

    @SubscribeEvent
    public static void onPlayerInteraction(PlayerInteractEvent.RightClickBlock event) {
        if (event.getLevel().isClientSide() || !(event.getEntity() instanceof ServerPlayer serverPlayer)
                || event.getCancellationResult().consumesAction()) {
            return;
        }
        BlockPos interactedBlockPos = event.getPos();
        ItemStack usedItemStack = event.getItemStack();
        PrescriptCriteriaTriggers.ANY_BLOCK_USE.get().trigger(serverPlayer, interactedBlockPos, usedItemStack);
    }
}
