package top.xuyangjerry.mcmod.tolistenseawaves.neoforge.item;

import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ToolMaterial;
import net.minecraft.world.item.component.ItemLore;

import net.minecraft.world.level.Level;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.ModifyDefaultComponentsEvent;

import top.xuyangjerry.mcmod.tolistenseawaves.neoforge.component.MorphStoredItemDataComponent;
import top.xuyangjerry.mcmod.tolistenseawaves.neoforge.init.ToListenSeawavesDataComponents;
import top.xuyangjerry.mcmod.tolistenseawaves.neoforge.init.ToListenSeawavesItems;
import top.xuyangjerry.mcmod.tolistenseawaves.neoforge.interfaces.IMorphCore;
import top.xuyangjerry.mcmod.tolistenseawaves.neoforge.interfaces.IMorphDerived;
import top.xuyangjerry.mcmod.tolistenseawaves.neoforge.tool.MorphItemHelper;

import java.util.List;

public class BastardSwordItem extends Item implements IMorphDerived {
	public BastardSwordItem(Properties properties) {
		super(properties.sword(ToolMaterial.NETHERITE, 3.0F, -2.4F)
				.component(DataComponents.LORE, new ItemLore(List.of(Component.translatable("item.to_listen_seawaves.bastard_sword.description_0").withStyle(Style.EMPTY.withItalic(false))))));	// 游戏内数值: 8伤害 1.6攻速
	}

	@SubscribeEvent
	public static void handleToolDamage(ModifyDefaultComponentsEvent event) {
		event.modify(ToListenSeawavesItems.BASTARD_SWORD.get(), builder -> builder.remove(DataComponents.MAX_DAMAGE));
	}

	@Override
	public InteractionResult use(Level level, Player player, InteractionHand hand) {
		ItemStack derivedStack = player.getItemInHand(hand);
		if (level.isClientSide()) { return InteractionResult.PASS; }
		if (player.isShiftKeyDown() && !MorphItemHelper.isItemOnCooldown(player, derivedStack)) {
			ItemStack coreStack = this.switchBackToCore(derivedStack, player, hand, level);
			player.setItemInHand(hand, coreStack);
			MorphItemHelper.addItemCooldown(player, coreStack);
			MorphItemHelper.playSwitchSound(level, player);
			return InteractionResult.SUCCESS;
		}
		return InteractionResult.PASS;
	}

	@Override
	public ItemStack switchBackToCore(ItemStack derivedStack, Player player, InteractionHand hand, Level level) {
		if (level.isClientSide()) { return null; }
		ItemStack coreStack = getAssociatedCoreItemstack(derivedStack);
		if (coreStack == null) {
			coreStack = new ItemStack(ToListenSeawavesItems.CADUCEUS.get());
		}
		else {
			derivedStack.remove(ToListenSeawavesDataComponents.STORED_CORE_ITEMS);
			derivedStack.remove(ToListenSeawavesDataComponents.ACCUMULATED_DAMAGE);
		}
		if (coreStack.getItem() instanceof IMorphCore imc){
			imc.appendDerivedItem(coreStack, derivedStack);
		}
		return coreStack;
	}

	@Override
	public ItemStack getAssociatedCoreItemstack(ItemStack derivedStack) {
		MorphStoredItemDataComponent component = derivedStack.getOrDefault(
				ToListenSeawavesDataComponents.STORED_CORE_ITEMS,
				MorphStoredItemDataComponent.EMPTY
		);
		return component.storedStacks().isEmpty() ? null : component.storedStacks().getFirst();
	}

	@Override
	public void setAssociatedCoreItem(ItemStack derivedStack, ItemStack coreStack) {
		derivedStack.set(ToListenSeawavesDataComponents.STORED_CORE_ITEMS, new MorphStoredItemDataComponent(List.of(coreStack)));
	}

	@Override
	public void postHurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {
		super.postHurtEnemy(stack, target, attacker);
		MorphItemHelper.handleMorphPostHurt(stack, target, attacker);
	}

	/*@Override
	public void appendHoverText(ItemStack itemstack, Item.TooltipContext context, TooltipDisplay tooltipDisplay, Consumer<Component> componentConsumer, TooltipFlag flag) {
		super.appendHoverText(itemstack, context, tooltipDisplay, componentConsumer, flag);
		componentConsumer.accept(Component.translatable("item.to_listen_seawaves.bastard_sword.description_0"));
	}*/
}