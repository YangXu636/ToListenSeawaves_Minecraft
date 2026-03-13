package top.xuyangjerry.mcmod.tolistenseawaves.neoforge.item;

import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;
import top.xuyangjerry.mcmod.tolistenseawaves.neoforge.component.MorphStoredItemDataComponent;
import top.xuyangjerry.mcmod.tolistenseawaves.neoforge.init.ToListenSeawavesDataComponents;
import top.xuyangjerry.mcmod.tolistenseawaves.neoforge.init.ToListenSeawavesItems;
import top.xuyangjerry.mcmod.tolistenseawaves.neoforge.init.ToListenSeawavesMobEffects;
import top.xuyangjerry.mcmod.tolistenseawaves.neoforge.interfaces.IMorphCore;
import top.xuyangjerry.mcmod.tolistenseawaves.neoforge.interfaces.IMorphDerived;
import top.xuyangjerry.mcmod.tolistenseawaves.neoforge.tool.MorphItemHelper;
import top.xuyangjerry.mcmod.tolistenseawaves.neoforge.tool.XyTools;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class CaduceusItem extends Item implements IMorphCore {
	public CaduceusItem(Properties properties) {
		super(properties.stacksTo(1));
	}

	@Override
	public @NotNull InteractionResult use(Level level, Player player, @NotNull InteractionHand hand) {
		ItemStack coreStack = player.getItemInHand(hand);
		if (level.isClientSide()) { return InteractionResult.PASS; }
		if (!player.isShiftKeyDown() && !MorphItemHelper.isItemOnCooldown(player, coreStack)) {
			ItemStack derivedStack = this.switchToDerived(coreStack, player, hand, level);
			player.setItemInHand(hand, derivedStack);
			MorphItemHelper.addItemCooldown(player, derivedStack);
			MorphItemHelper.playSwitchSound(level, player);
			return InteractionResult.SUCCESS;
		}
		return InteractionResult.PASS;
	}

	private int getKarmaObstacleResistanceLevel(Player player) {
		MobEffectInstance koResist = player.getEffect(ToListenSeawavesMobEffects.KARMA_OBSTACLE);
		int level = koResist == null ? 0 : (koResist.getAmplifier() + 1);
		return Mth.clamp(level, 0, 20);
	}

	public static Item getRandomDerivedItem(Level level, int koResistLevel) {
		List<Item> derivedItems = List.of(
				ToListenSeawavesItems.BASTARD_SWORD.get(),
				ToListenSeawavesItems.BASTARD_SWORD.get(),
				ToListenSeawavesItems.HAMMER.get(),
				ToListenSeawavesItems.HAND_AXE.get(),
				ToListenSeawavesItems.BROAD_SWORD.get(),
				ToListenSeawavesItems.RAPIER.get(),
				ToListenSeawavesItems.TRIDENT.get()
		);
		float spec = Math.min(koResistLevel * 5.0f, 100.0f);
		float remainingChance = 100.0f - spec;
		float aver = remainingChance / (derivedItems.size() - 1);
		ArrayList<Float> prolist = new ArrayList<>();
		prolist.add(spec);
		prolist.addAll(Collections.nCopies(derivedItems.size() - 1, aver));
		return XyTools.GetMemberWithProbability(derivedItems, prolist);
	}

	@Override
	public ItemStack switchToDerived(ItemStack coreStack, Player player, InteractionHand hand, Level level) {
		if (level.isClientSide()) { return null; }
		Item derivedItem = getRandomDerivedItem(level, getKarmaObstacleResistanceLevel(player));
		List<ItemStack> storedDI = getStoredDerivedItems(coreStack).stream().filter(x -> x.is(derivedItem)).toList();
		ItemStack result = storedDI.isEmpty() ? new ItemStack(derivedItem, 1) : storedDI.getFirst();
		if (!storedDI.isEmpty()) {
			MorphStoredItemDataComponent msiDc = coreStack.getOrDefault(
					ToListenSeawavesDataComponents.STORED_DERIVED_ITEMS,
					MorphStoredItemDataComponent.EMPTY
			);
			coreStack.set(ToListenSeawavesDataComponents.STORED_DERIVED_ITEMS, msiDc.remove(result));
		}
		if (derivedItem instanceof IMorphDerived imd){
			imd.setAssociatedCoreItem(result, coreStack);
		}
		return result;
	}

	@Override
	public List<ItemStack> getStoredDerivedItems(ItemStack coreStack) {
		MorphStoredItemDataComponent component = coreStack.getOrDefault(
				ToListenSeawavesDataComponents.STORED_DERIVED_ITEMS,
				MorphStoredItemDataComponent.EMPTY
		);
		return component.storedStacks();
	}

	@Override
	public void appendDerivedItem(ItemStack coreStack, ItemStack derivedStack) {
		MorphStoredItemDataComponent component = coreStack.getOrDefault(
				ToListenSeawavesDataComponents.STORED_DERIVED_ITEMS,
				MorphStoredItemDataComponent.EMPTY
		);
		coreStack.set(ToListenSeawavesDataComponents.STORED_DERIVED_ITEMS, component.add(derivedStack));
	}
}