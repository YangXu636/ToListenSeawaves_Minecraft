package top.xuyangjerry.mcmod.tolistenseawaves.neoforge.prescripts.criterion;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.advancements.criterion.ContextAwarePredicate;
import net.minecraft.advancements.criterion.MinMaxBounds;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import top.xuyangjerry.mcmod.tolistenseawaves.neoforge.ToListenSeawaves;
import top.xuyangjerry.mcmod.tolistenseawaves.neoforge.prescripts.PrescriptCriteriaTriggers;
import top.xuyangjerry.mcmod.tolistenseawaves.neoforge.prescripts.SimplePrescriptCriterionTrigger;
import top.xuyangjerry.mcmod.tolistenseawaves.neoforge.tool.DataStructures;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@EventBusSubscriber(modid = ToListenSeawaves.MOD_ID)
public class PlayerMovementTrigger extends SimplePrescriptCriterionTrigger<PlayerMovementTrigger.TriggerInstance> {
    public static final Map<ServerPlayer, DataStructures.MovementState> PLAYER_MOVEMENT_STATE = new HashMap<>();

    @Override
    public Codec<TriggerInstance> codec() {
        return TriggerInstance.CODEC;
    }

    public void trigger(ServerPlayer player,
                        double worldX, double worldY, double worldZ,
                        double relX, double relY, double relZ) {
        this.trigger(player, instance -> instance.matches(worldX, worldY, worldZ, relX, relY, relZ));
    }

    public static record TriggerInstance(
            Optional<ContextAwarePredicate> player,
            // 世界坐标范围
            Optional<MinMaxBounds.Doubles> world_x,
            Optional<MinMaxBounds.Doubles> world_y,
            Optional<MinMaxBounds.Doubles> world_z,
            // 相对坐标范围（自身：前、右、上）
            Optional<MinMaxBounds.Doubles> relative_x,
            Optional<MinMaxBounds.Doubles> relative_y,
            Optional<MinMaxBounds.Doubles> relative_z
    ) implements SimplePrescriptCriterionTrigger.SimplePrescriptInstance {
        public static final Codec<TriggerInstance> CODEC = RecordCodecBuilder.create(instance ->
                instance.group(
                        ContextAwarePredicate.CODEC.optionalFieldOf("player").forGetter(TriggerInstance::player),
                        MinMaxBounds.Doubles.CODEC.optionalFieldOf("world_x").forGetter(TriggerInstance::world_x),
                        MinMaxBounds.Doubles.CODEC.optionalFieldOf("world_y").forGetter(TriggerInstance::world_y),
                        MinMaxBounds.Doubles.CODEC.optionalFieldOf("world_z").forGetter(TriggerInstance::world_z),
                        MinMaxBounds.Doubles.CODEC.optionalFieldOf("relative_x").forGetter(TriggerInstance::relative_x),
                        MinMaxBounds.Doubles.CODEC.optionalFieldOf("relative_y").forGetter(TriggerInstance::relative_y),
                        MinMaxBounds.Doubles.CODEC.optionalFieldOf("relative_z").forGetter(TriggerInstance::relative_z)
                ).apply(instance, TriggerInstance::new)
        );

        public boolean matches(double wX, double wY, double wZ, double rX, double rY, double rZ) {
            // 校验世界坐标
            if (world_x.isPresent() && !world_x.get().matches(wX)) return false;
            if (world_y.isPresent() && !world_y.get().matches(wY)) return false;
            if (world_z.isPresent() && !world_z.get().matches(wZ)) return false;
            // 校验相对坐标
            if (relative_x.isPresent() && !relative_x.get().matches(rX)) return false;
            if (relative_y.isPresent() && !relative_y.get().matches(rY)) return false;
            if (relative_z.isPresent() && !relative_z.get().matches(rZ)) return false;

            return true;
        }
    }

    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Pre event) {
        if (!(event.getEntity() instanceof ServerPlayer player))
            return;
        DataStructures.MovementState state = PLAYER_MOVEMENT_STATE.computeIfAbsent(player, DataStructures.MovementState::new);
        state.update(player);
        PrescriptCriteriaTriggers.PLAYER_MOVEMENT.get().trigger(
                player,
                state.getAccumulatedWorldX(), state.getAccumulatedWorldY(), state.getAccumulatedWorldZ(),
                state.getAccumulatedRelativeX(), state.getAccumulatedRelativeY(), state.getAccumulatedRelativeZ()
        );
    }

    @SubscribeEvent
    public static void onLogout(PlayerEvent.PlayerLoggedOutEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            PLAYER_MOVEMENT_STATE.remove(player);
        }
    }
}
