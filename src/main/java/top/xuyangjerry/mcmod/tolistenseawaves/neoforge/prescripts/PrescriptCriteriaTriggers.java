package top.xuyangjerry.mcmod.tolistenseawaves.neoforge.prescripts;

import com.mojang.serialization.Codec;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import org.jetbrains.annotations.NotNull;
import top.xuyangjerry.mcmod.tolistenseawaves.neoforge.ToListenSeawaves;
import top.xuyangjerry.mcmod.tolistenseawaves.neoforge.init.ToListenSeawavesRegistries;
import top.xuyangjerry.mcmod.tolistenseawaves.neoforge.prescripts.criterion.*;

public class PrescriptCriteriaTriggers {
    public static final DeferredRegister<PrescriptCriterionTrigger<?>> PRESCRIPT_TRIGGERS =
            DeferredRegister.create(ToListenSeawavesRegistries.PRESCRIPT_TRIGGER_REGISTRY, ToListenSeawaves.MOD_ID);

    public static final Codec<PrescriptCriterionTrigger<?>> CODEC;
    public static final DeferredHolder<PrescriptCriterionTrigger<?>, @NotNull ImpossibleTrigger> IMPOSSIBLE = PRESCRIPT_TRIGGERS.register("impossible", ImpossibleTrigger::new);
    public static final DeferredHolder<PrescriptCriterionTrigger<?>, @NotNull SendMessageTrigger> SEND_MESSAGE = PRESCRIPT_TRIGGERS.register("send_message", SendMessageTrigger::new);
    public static final DeferredHolder<PrescriptCriterionTrigger<?>, @NotNull AnyBlockInteractionTrigger> ANY_BLOCK_USE = PRESCRIPT_TRIGGERS.register("any_block_use", AnyBlockInteractionTrigger::new);
    public static final DeferredHolder<PrescriptCriterionTrigger<?>, @NotNull PlayerRotationTrigger> PLAYER_ROTATION = PRESCRIPT_TRIGGERS.register("player_rotation", PlayerRotationTrigger::new);
    public static final DeferredHolder<PrescriptCriterionTrigger<?>, @NotNull PlayerMovementTrigger> PLAYER_MOVEMENT = PRESCRIPT_TRIGGERS.register("player_movement", PlayerMovementTrigger::new);
    public static final DeferredHolder<PrescriptCriterionTrigger<?>, @NotNull PlayerHurtEntityTrigger> PLAYER_HURT_ENTITY = PRESCRIPT_TRIGGERS.register("player_hurt_entity", PlayerHurtEntityTrigger::new);
    public static final DeferredHolder<PrescriptCriterionTrigger<?>, @NotNull BeeNestDestroyedTrigger> BEE_NEST_DESTROYED = PRESCRIPT_TRIGGERS.register("bee_nest_destroyed", BeeNestDestroyedTrigger::new);
    public static final DeferredHolder<PrescriptCriterionTrigger<?>, @NotNull BredAnimalsTrigger> BRED_ANIMALS = PRESCRIPT_TRIGGERS.register("bred_animals", BredAnimalsTrigger::new);
    public static final DeferredHolder<PrescriptCriterionTrigger<?>, @NotNull BrewedPotionTrigger> BREWED_POTION = PRESCRIPT_TRIGGERS.register("brewed_potion", BrewedPotionTrigger::new);
    public static final DeferredHolder<PrescriptCriterionTrigger<?>, @NotNull ChangeDimensionTrigger> CHANGED_DIMENSION = PRESCRIPT_TRIGGERS.register("changed_dimension", ChangeDimensionTrigger::new);
    public static final DeferredHolder<PrescriptCriterionTrigger<?>, @NotNull ChanneledLightningTrigger> CHANNELED_LIGHTNING = PRESCRIPT_TRIGGERS.register("channeled_lightning", ChanneledLightningTrigger::new);

    public PrescriptCriteriaTriggers() {
    }

    static {
        CODEC = Codec.lazyInitialized(() ->
                PRESCRIPT_TRIGGERS.getRegistry().get().byNameCodec()
        );
    }
}
