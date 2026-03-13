package top.xuyangjerry.mcmod.tolistenseawaves.neoforge.interfaces;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import java.util.List;

public interface IMorphCore {
    /**
     * 切换到随机衍生物品
     * @param coreStack 核心物品栈
     * @param player 玩家
     * @param hand 交互手
     * @param level 世界
     * @return 切换后的衍生物品栈
     */
    ItemStack switchToDerived(ItemStack coreStack, Player player, InteractionHand hand, Level level);

    /**
     * 获取核心物品存储的所有衍生物品
     * @param coreStack 核心物品栈
     * @return 存储的衍生物品列表
     */
    List<ItemStack> getStoredDerivedItems(ItemStack coreStack);

    /**
     * 追加衍生物品到核心物品的存储列表（去重）
     * @param coreStack 核心物品栈
     * @param derivedStack 要追加的衍生物品栈
     */
    void appendDerivedItem(ItemStack coreStack, ItemStack derivedStack);
}
