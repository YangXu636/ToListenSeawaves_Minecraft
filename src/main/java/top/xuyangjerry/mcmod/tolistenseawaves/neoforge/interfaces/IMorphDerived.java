package top.xuyangjerry.mcmod.tolistenseawaves.neoforge.interfaces;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public interface IMorphDerived {
    /**
     * 切回关联的核心物品
     * @param derivedStack 衍生物品栈
     * @param player 玩家
     * @param hand 交互手
     * @param level 世界
     * @return 切回后的核心物品栈
     */
    ItemStack switchBackToCore(ItemStack derivedStack, Player player, InteractionHand hand, Level level);

    /**
     * 获取当前衍生物品关联的核心物品栈
     * @param derivedStack 衍生物品栈
     * @return 关联的核心物品栈（空则返回ItemStack.EMPTY）
     */
    ItemStack getAssociatedCoreItemstack(ItemStack derivedStack);

    /**
     * 设置衍生物品关联的核心物品
     * @param derivedStack 衍生物品栈
     * @param coreStack 核心物品栈
     */
    void setAssociatedCoreItem(ItemStack derivedStack, ItemStack coreStack);
}
