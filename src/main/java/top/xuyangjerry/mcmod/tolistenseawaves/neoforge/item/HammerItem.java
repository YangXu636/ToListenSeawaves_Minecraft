package top.xuyangjerry.mcmod.tolistenseawaves.neoforge.item;

import net.minecraft.core.RegistryAccess;
import net.minecraft.core.component.DataComponents;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSources;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.MaceItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.world.item.component.Tool;
import net.minecraft.world.item.component.Weapon;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;
import top.xuyangjerry.mcmod.tolistenseawaves.neoforge.component.AreaAttackBonusDataComponent;
import top.xuyangjerry.mcmod.tolistenseawaves.neoforge.component.MorphStoredItemDataComponent;
import top.xuyangjerry.mcmod.tolistenseawaves.neoforge.init.ToListenSeawavesDataComponents;
import top.xuyangjerry.mcmod.tolistenseawaves.neoforge.init.ToListenSeawavesItems;
import top.xuyangjerry.mcmod.tolistenseawaves.neoforge.interfaces.IMorphCore;
import top.xuyangjerry.mcmod.tolistenseawaves.neoforge.interfaces.IMorphDerived;
import top.xuyangjerry.mcmod.tolistenseawaves.neoforge.tool.MorphItemHelper;

import java.util.List;

import static net.minecraft.world.item.Items.BREEZE_ROD;

public class HammerItem extends MaceItem implements IMorphDerived {
	private static final int MAX_CHARGE_TICKS = 40;
	private final boolean canCalDamageBonus = false;

	public HammerItem(Item.Properties properties) {
		super(properties.rarity(Rarity.EPIC).durability(510).component(DataComponents.TOOL, HammerItem.createToolProperties()).repairable(BREEZE_ROD).attributes(HammerItem.createAttributes()).component(DataComponents.WEAPON, new Weapon(1)));
	}

	public static @NotNull ItemAttributeModifiers createAttributes() {
		return ItemAttributeModifiers.builder().add(Attributes.ATTACK_DAMAGE, new AttributeModifier(BASE_ATTACK_DAMAGE_ID, 11.0, AttributeModifier.Operation.ADD_VALUE), EquipmentSlotGroup.MAINHAND).add(Attributes.ATTACK_SPEED, new AttributeModifier(BASE_ATTACK_SPEED_ID, -3.7, AttributeModifier.Operation.ADD_VALUE), EquipmentSlotGroup.MAINHAND).build();
	}

	public static Tool createToolProperties() {
		return new Tool(List.of(), 1.0F, 2, false);
	}

	@Override
	public @NotNull InteractionResult use(Level level, Player player, @NotNull InteractionHand hand) {
		super.use(level, player, hand);
		ItemStack derivedStack = player.getItemInHand(hand);
		if (level.isClientSide()) { return InteractionResult.PASS; }
		if (player.isShiftKeyDown() && !MorphItemHelper.isItemOnCooldown(player, derivedStack)) {
			ItemStack coreStack = this.switchBackToCore(derivedStack, player, hand, level);
			player.setItemInHand(hand, coreStack);
			MorphItemHelper.addItemCooldown(player, coreStack);
			MorphItemHelper.playSwitchSound(level, player);
			return InteractionResult.SUCCESS;
		} else if (!player.isShiftKeyDown()) {
			player.startUsingItem(hand);
			level.playSound(null, player.blockPosition(),
					SoundEvents.CROSSBOW_LOADING_MIDDLE.value(), SoundSource.PLAYERS, 0.5F, 1.0F);
		}
		return InteractionResult.PASS;
	}

	@Override
	public void onUseTick(Level level, LivingEntity livingEntity, ItemStack stack, int remainingUseDuration) {
		System.out.println(remainingUseDuration);
		super.onUseTick(level, livingEntity, stack, remainingUseDuration);
        if (!level.isClientSide() || !(livingEntity instanceof Player player)) { return; }
		int chargedTicks = MAX_CHARGE_TICKS - remainingUseDuration;
		int CHARGE_STEP_TICKS = MAX_CHARGE_TICKS / 2;
		if (chargedTicks % CHARGE_STEP_TICKS == 0 && chargedTicks > 0) {
			level.playSound(player, player.blockPosition(),
					SoundEvents.ARMOR_EQUIP_IRON.value(), SoundSource.PLAYERS, 0.3F, 1.2F);
		}
		if (chargedTicks == MAX_CHARGE_TICKS) {
			level.playSound(player, player.blockPosition(),
					SoundEvents.TOTEM_USE, SoundSource.PLAYERS, 0.5F, 0.8F);
		}
    }

	@Override
	public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity livingEntity) {
		ItemStack ois = super.finishUsingItem(stack, level, livingEntity);
		if (!level.isClientSide() && livingEntity instanceof Player player) {
			int chargedTicks = MAX_CHARGE_TICKS - player.getUseItemRemainingTicks();
			float chargedSeconds = chargedTicks / 20.0F;
			int attackBonus = Mth.clamp((int) (chargedSeconds * 5),
					0, 10);
			stack.set(ToListenSeawavesDataComponents.AREA_ATTACK_BONUS, new AreaAttackBonusDataComponent(attackBonus));
		}
		return ois;
	}

	@Override
	public void postHurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {
		super.postHurtEnemy(stack, target, attacker);
		Level level = attacker.level();
        if (level.isClientSide()) { return; }
		AreaAttackBonusDataComponent bonusComponent = stack.getOrDefault(
				ToListenSeawavesDataComponents.AREA_ATTACK_BONUS,
				AreaAttackBonusDataComponent.EMPTY
        );
        int attackBonus = bonusComponent.bonus();
        if (attackBonus > 0 && level instanceof ServerLevel serverLevel) {
			target.hurtServer(serverLevel, new DamageSources(RegistryAccess.EMPTY).playerAttack((Player) attacker), attackBonus);
            level.playSound(null, target.blockPosition(),
                    SoundEvents.PLAYER_ATTACK_CRIT, SoundSource.PLAYERS, 0.5F, 1.5F);
            stack.set(ToListenSeawavesDataComponents.AREA_ATTACK_BONUS, AreaAttackBonusDataComponent.EMPTY);
        }
    }

	@Override
	public boolean useOnRelease(@NotNull ItemStack stack) {
		AreaAttackBonusDataComponent bonusComponent = stack.getOrDefault(
				ToListenSeawavesDataComponents.AREA_ATTACK_BONUS,
				AreaAttackBonusDataComponent.EMPTY
		);
		int attackBonus = bonusComponent.bonus();
		if (attackBonus >= 5){
		}
		stack.set(ToListenSeawavesDataComponents.AREA_ATTACK_BONUS, AreaAttackBonusDataComponent.EMPTY);
		return super.useOnRelease(stack);
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
}