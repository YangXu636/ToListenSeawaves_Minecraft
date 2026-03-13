package top.xuyangjerry.mcmod.tolistenseawaves.neoforge.tool;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import top.xuyangjerry.mcmod.tolistenseawaves.neoforge.component.MorphStoredItemDataComponent;
import top.xuyangjerry.mcmod.tolistenseawaves.neoforge.init.ToListenSeawavesDataComponents;

import java.util.List;
import java.util.function.Function;

public class MorphItemHelper {
    public static final int MORPH_COOLDOWN_TICKS = 16;

    public static void playSwitchSound(Level level, Player player) {
        if (!level.isClientSide()) {
            level.playSound(null, player.blockPosition(),
                    SoundEvents.ARMOR_EQUIP_GENERIC.value(),
                    SoundSource.PLAYERS, 1.0F, 1.2F);
        }
    }

    public static void addItemCooldown(Player player, ItemStack itemStack) {
        if (player instanceof ServerPlayer serverPlayer) {
            serverPlayer.getCooldowns().addCooldown(itemStack, MORPH_COOLDOWN_TICKS);
        }
    }

    public static boolean isItemOnCooldown(Player player, ItemStack itemStack) {
        return player.getCooldowns().isOnCooldown(itemStack);
    }
}