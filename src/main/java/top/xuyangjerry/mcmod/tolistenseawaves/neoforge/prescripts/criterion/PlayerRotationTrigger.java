package top.xuyangjerry.mcmod.tolistenseawaves.neoforge.prescripts.criterion;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.advancements.criterion.ContextAwarePredicate;
import net.minecraft.advancements.criterion.CriterionValidator;
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
public class PlayerRotationTrigger extends SimplePrescriptCriterionTrigger<PlayerRotationTrigger.TriggerInstance> {
    public static final Map<ServerPlayer, DataStructures.RotationState> PLAYER_ROTATION_STATE = new HashMap<>();

    @Override
    public Codec<TriggerInstance> codec() {
        return TriggerInstance.CODEC;
    }

    public void trigger(ServerPlayer player, double yaw, double pitch, double roll) {
        this.trigger(player, instance -> instance.matches(yaw, pitch, roll));
    }

    public static record TriggerInstance(Optional<ContextAwarePredicate> player, Optional<MinMaxBounds.Doubles> yaw, Optional<MinMaxBounds.Doubles> pitch, Optional<MinMaxBounds.Doubles> roll) implements SimplePrescriptCriterionTrigger.SimplePrescriptInstance {
        public static final Codec<TriggerInstance> CODEC = RecordCodecBuilder.create(instance ->
                instance.group(
                        ContextAwarePredicate.CODEC.optionalFieldOf("player").forGetter(TriggerInstance::player),
                        MinMaxBounds.Doubles.CODEC.optionalFieldOf("yaw").forGetter(TriggerInstance::yaw),
                        MinMaxBounds.Doubles.CODEC.optionalFieldOf("pitch").forGetter(TriggerInstance::pitch),
                        MinMaxBounds.Doubles.CODEC.optionalFieldOf("roll").forGetter(TriggerInstance::roll)
                ).apply(instance, TriggerInstance::new)
        );

        public boolean matches(double yawVal, double pitchVal, double rollVal) {
            if (yaw.isPresent() && !yaw.get().matches(yawVal)) return false;
            if (pitch.isPresent() && !pitch.get().matches(pitchVal)) return false;
            if (roll.isPresent() && !roll.get().matches(rollVal)) return false;
            return true;
        }

        @Override
        public void validate(CriterionValidator validator) {
            SimplePrescriptCriterionTrigger.SimplePrescriptInstance.super.validate(validator);
        }
    }

    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Pre event) {
        if (!(event.getEntity() instanceof ServerPlayer player))
            return;
        DataStructures.RotationState state = PLAYER_ROTATION_STATE.computeIfAbsent(player, DataStructures.RotationState::new);
        state.update(player);
        PrescriptCriteriaTriggers.PLAYER_ROTATION.get().trigger(player, state.getAccumulatedYaw(), state.getAccumulatedPitch(), state.getAccumulatedRoll());
    }

    @SubscribeEvent
    public static void onLogout(PlayerEvent.PlayerLoggedOutEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            PLAYER_ROTATION_STATE.remove(player);
        }
    }
}
