package top.xuyangjerry.mcmod.tolistenseawaves.neoforge.tool;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import top.xuyangjerry.mcmod.tolistenseawaves.neoforge.component.AccumulatedDamageComponent;
import top.xuyangjerry.mcmod.tolistenseawaves.neoforge.component.MorphStoredItemDataComponent;
import top.xuyangjerry.mcmod.tolistenseawaves.neoforge.init.ToListenSeawavesDataComponents;
import top.xuyangjerry.mcmod.tolistenseawaves.neoforge.interfaces.IMorphDerived;

import java.util.List;

public class MorphItemHelper {
    public static final int MORPH_COOLDOWN_TICKS = 16;
    public static final float SWITCH_BACK_CHANCE = 0.05f;
    public static final float ACCUMULATED_DAMAGE_THRESHOLD = 35.0f;

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

    /**
     * 处理衍生武器攻击后的逻辑：5%概率切回基础形态，或累积35伤害后切回
     */
    public static void handleMorphPostHurt(ItemStack derivedStack, LivingEntity target, LivingEntity attacker) {
        if (attacker.level().isClientSide()) return;
        if (!(attacker instanceof ServerPlayer player)) return;
        if (!(derivedStack.getItem() instanceof IMorphDerived)) return;

        // 5%概率切回基础形态
        if (attacker.getRandom().nextFloat() < SWITCH_BACK_CHANCE) {
            forceSwitchBackToCore(derivedStack, player);
            return;
        }

        // 累积伤害判定
        float attackDamage = (float) attacker.getAttributeValue(Attributes.ATTACK_DAMAGE) - 1.0f;
        float currentAccumulated = derivedStack.getOrDefault(
                ToListenSeawavesDataComponents.ACCUMULATED_DAMAGE,
                AccumulatedDamageComponent.EMPTY
        ).damage();
        float newAccumulated = currentAccumulated + Math.max(0, attackDamage);

        if (newAccumulated >= ACCUMULATED_DAMAGE_THRESHOLD) {
            forceSwitchBackToCore(derivedStack, player);
        } else {
            derivedStack.set(ToListenSeawavesDataComponents.ACCUMULATED_DAMAGE, new AccumulatedDamageComponent(newAccumulated));
        }
    }

    /**
     * 强制将衍生武器切回基础形态
     */
    public static void forceSwitchBackToCore(ItemStack derivedStack, ServerPlayer player) {
        if (!(derivedStack.getItem() instanceof IMorphDerived derived)) return;

        InteractionHand hand = null;
        if (player.getMainHandItem() == derivedStack) hand = InteractionHand.MAIN_HAND;
        else if (player.getOffhandItem() == derivedStack) hand = InteractionHand.OFF_HAND;
        if (hand == null) return;

        // 清除累积伤害
        derivedStack.remove(ToListenSeawavesDataComponents.ACCUMULATED_DAMAGE);

        ItemStack coreStack = derived.switchBackToCore(derivedStack, player, hand, player.level());
        if (coreStack != null) {
            player.setItemInHand(hand, coreStack);
            addItemCooldown(player, coreStack);
            playSwitchSound(player.level(), player);
        }
    }
}