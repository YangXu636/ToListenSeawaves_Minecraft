package top.xuyangjerry.mcmod.tolistenseawaves.neoforge.item;

import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.Item;

public class PrescriptDeviceItem extends Item {
	public PrescriptDeviceItem(Properties properties) {
		super(properties.rarity(Rarity.EPIC).stacksTo(1).fireResistant());
	}
}