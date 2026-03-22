package top.xuyangjerry.mcmod.tolistenseawaves.neoforge.init;

import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.Registries;
import net.neoforged.neoforge.registries.DeferredRegister;
import top.xuyangjerry.mcmod.tolistenseawaves.neoforge.ToListenSeawaves;
import top.xuyangjerry.mcmod.tolistenseawaves.neoforge.component.AreaAttackBonusDataComponent;
import top.xuyangjerry.mcmod.tolistenseawaves.neoforge.component.MorphStoredItemDataComponent;
import top.xuyangjerry.mcmod.tolistenseawaves.neoforge.component.PlayerPrescriptDataComponent;

import java.util.function.Supplier;

public class ToListenSeawavesDataComponents {
    public static final DeferredRegister.DataComponents DATA_COMPONENTS =
            DeferredRegister.createDataComponents(Registries.DATA_COMPONENT_TYPE, ToListenSeawaves.MOD_ID);

    public static final Supplier<DataComponentType<MorphStoredItemDataComponent>> STORED_CORE_ITEMS =
            DATA_COMPONENTS.registerComponentType(
                    "stored_core_items",
                    builder -> builder
                            .persistent(MorphStoredItemDataComponent.CODEC) // 磁盘持久化
                            .networkSynchronized(MorphStoredItemDataComponent.STREAM_CODEC) // 网络同步
                            .cacheEncoding()
            );

    public static final Supplier<DataComponentType<MorphStoredItemDataComponent>> STORED_DERIVED_ITEMS =
            DATA_COMPONENTS.registerComponentType(
                    "stored_derived_items",
                    builder -> builder
                            .persistent(MorphStoredItemDataComponent.CODEC)
                            .networkSynchronized(MorphStoredItemDataComponent.STREAM_CODEC)
                            .cacheEncoding()
            );

    public static final Supplier<DataComponentType<AreaAttackBonusDataComponent>> AREA_ATTACK_BONUS =
            DATA_COMPONENTS.registerComponentType(
                    "add_attack_bonus",
                    builder -> builder
                            .persistent(AreaAttackBonusDataComponent.CODEC)
                            .networkSynchronized(AreaAttackBonusDataComponent.STREAM_CODEC)
                            .cacheEncoding()
            );

    public static final Supplier<DataComponentType<PlayerPrescriptDataComponent>> PLAYER_PRESCRIPT =
            DATA_COMPONENTS.registerComponentType(
                    "prescript_data",
                    builder -> builder
                            .persistent(PlayerPrescriptDataComponent.CODEC)
                            .networkSynchronized(PlayerPrescriptDataComponent.STREAM_CODEC)
                            .cacheEncoding()
            );
}
