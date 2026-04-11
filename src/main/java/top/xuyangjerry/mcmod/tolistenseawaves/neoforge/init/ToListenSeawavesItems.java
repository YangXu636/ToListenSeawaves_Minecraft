package top.xuyangjerry.mcmod.tolistenseawaves.neoforge.init;

import net.minecraft.world.item.Item;

import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

import top.xuyangjerry.mcmod.tolistenseawaves.neoforge.ToListenSeawaves;
import top.xuyangjerry.mcmod.tolistenseawaves.neoforge.item.*;

public class ToListenSeawavesItems {
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(ToListenSeawaves.MOD_ID);

    public static final DeferredItem<Item> CADUCEUS = ITEMS.registerItem("caduceus", CaduceusItem::new, Item.Properties::new); //p -> p.food(new FoodProperties.Builder().alwaysEdible().nutrition(1).saturationModifier(2f).build())
    public static final DeferredItem<Item> BASTARD_SWORD = ITEMS.registerItem("bastard_sword", BastardSwordItem::new, Item.Properties::new);
    public static final DeferredItem<Item> HAMMER = ITEMS.registerItem("hammer", HammerItem::new, Item.Properties::new);
    public static final DeferredItem<Item> HAND_AXE = ITEMS.registerItem("hand_axe", HandAxeItem::new, Item.Properties::new);
    public static final DeferredItem<Item> BROAD_SWORD = ITEMS.registerItem("broad_sword", BroadSwordItem::new, Item.Properties::new);
    public static final DeferredItem<Item> RAPIER = ITEMS.registerItem("rapier", RapierItem::new, Item.Properties::new);
    public static final DeferredItem<Item> TRIDENT = ITEMS.registerItem("trident", TridentItem::new, Item.Properties::new);
    public static final DeferredItem<Item> PRESCRIPT_DEVICE = ITEMS.registerItem("prescript_device", PrescriptDeviceItem::new, Item.Properties::new);
}
