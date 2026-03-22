package top.xuyangjerry.mcmod.tolistenseawaves.neoforge.init;

import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;

import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

import top.xuyangjerry.mcmod.tolistenseawaves.neoforge.ToListenSeawaves;

public class ToListenSeawavesTabs {
    public static final DeferredRegister<CreativeModeTab> TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, ToListenSeawaves.MOD_ID);

    public static final DeferredHolder<CreativeModeTab, @org.jetbrains.annotations.NotNull CreativeModeTab> TLS_TAB = TABS.register("tls_tab",
            () -> CreativeModeTab.builder().title(Component.translatable("item_group.to_listen_seawaves.tls_tab")).icon(() -> new ItemStack(Blocks.AIR)).displayItems((parameters, tabData) -> {
                tabData.accept(ToListenSeawavesItems.CADUCEUS.get());
                tabData.accept(ToListenSeawavesItems.BASTARD_SWORD.get());
                tabData.accept(ToListenSeawavesItems.HAMMER.get());
                tabData.accept(ToListenSeawavesItems.HAND_AXE.get());
                tabData.accept(ToListenSeawavesItems.BROAD_SWORD.get());
                tabData.accept(ToListenSeawavesItems.RAPIER.get());
                tabData.accept(ToListenSeawavesItems.TRIDENT.get());
            }).withSearchBar().build());
}
