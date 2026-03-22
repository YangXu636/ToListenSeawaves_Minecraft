package top.xuyangjerry.mcmod.tolistenseawaves.neoforge;

import com.mojang.brigadier.context.CommandContextBuilder;
import com.mojang.brigadier.context.ParsedArgument;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.registries.DataPackRegistryEvent;
import net.neoforged.neoforge.registries.NewRegistryEvent;
import org.slf4j.Logger;

import com.mojang.logging.LogUtils;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.event.server.ServerStartingEvent;
import net.neoforged.neoforge.event.CommandEvent;
import top.xuyangjerry.mcmod.tolistenseawaves.neoforge.init.*;
import top.xuyangjerry.mcmod.tolistenseawaves.neoforge.prescripts.Prescript;
import top.xuyangjerry.mcmod.tolistenseawaves.neoforge.prescripts.PrescriptCriteriaTriggers;
import top.xuyangjerry.mcmod.tolistenseawaves.neoforge.server.ServerPrescriptManager;

import java.util.List;
import java.util.Map;

@Mod(ToListenSeawaves.MOD_ID)
public class ToListenSeawaves {
    public static final String MOD_ID = "to_listen_seawaves";
    public static final Logger LOGGER = LogUtils.getLogger();

    // public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(MODID);

    // public static final DeferredBlock<Block> EXAMPLE_BLOCK = BLOCKS.registerSimpleBlock("example_block", p -> p.mapColor(MapColor.STONE));

    // public static final DeferredItem<BlockItem> EXAMPLE_BLOCK_ITEM = ITEMS.registerSimpleBlockItem("example_block", EXAMPLE_BLOCK);


    /*public static final DeferredHolder<CreativeModeTab, CreativeModeTab> EXAMPLE_TAB = CREATIVE_MODE_TABS.register("example_tab", () -> CreativeModeTab.builder()
            .title(Component.translatable("itemGroup.examplemod"))
            .withTabsBefore(CreativeModeTabs.COMBAT)
            .icon(() -> EXAMPLE_ITEM.get().getDefaultInstance())
            .displayItems((parameters, output) -> {
                output.accept(EXAMPLE_ITEM.get());
            }).build());*/

    public ToListenSeawaves(IEventBus modEventBus, ModContainer modContainer) {
        modEventBus.addListener(this::commonSetup);
        modEventBus.addListener(ToListenSeawavesRegistries::registerCustomRegistries);

        NeoForge.EVENT_BUS.register(this);
        NeoForge.EVENT_BUS.register(ServerPrescriptManager.class);

        ToListenSeawavesItems.ITEMS.register(modEventBus);
        ToListenSeawavesTabs.TABS.register(modEventBus);
        ToListenSeawavesDataComponents.DATA_COMPONENTS.register(modEventBus);
        ToListenSeawavesMobEffects.MOB_EFFECTS.register(modEventBus);
        ToListenSeawavesAttachmentType.ATTACHMENT_TYPES.register(modEventBus);
        PrescriptCriteriaTriggers.PRESCRIPT_TRIGGERS.register(modEventBus);

        modEventBus.addListener(this::addCreative);
        modEventBus.addListener(ToListenSeawavesRegistries::registerDatapackRegistries);
        // Register our mod's ModConfigSpec so that FML can create and load the config file for us
        modContainer.registerConfig(ModConfig.Type.COMMON, Config.SPEC);
    }

    private void commonSetup(FMLCommonSetupEvent event) {
        LOGGER.info("HELLO FROM COMMON SETUP");

        if (Config.LOG_DIRT_BLOCK.getAsBoolean()) {
            LOGGER.info("DIRT BLOCK >> {}", BuiltInRegistries.BLOCK.getKey(Blocks.DIRT));
        }

        LOGGER.info("{}{}", Config.MAGIC_NUMBER_INTRODUCTION.get(), Config.MAGIC_NUMBER.getAsInt());

        Config.ITEM_STRINGS.get().forEach((item) -> LOGGER.info("ITEM >> {}", item));
    }

    private void addCreative(BuildCreativeModeTabContentsEvent event) {
        /*if (event.getTabKey() == CreativeModeTabs.BUILDING_BLOCKS) {
            event.accept(EXAMPLE_BLOCK_ITEM);
        }*/
    }

    @SubscribeEvent
    public void onServerStarting(ServerStartingEvent event) {
        // Do something when the server starts
        LOGGER.info("HELLO from server starting");
    }

    @SubscribeEvent
    public void onCommand(CommandEvent event) {
        CommandContextBuilder<CommandSourceStack> contextBuilder = event.getParseResults().getContext();
        CommandSourceStack source = contextBuilder.getSource();

        if (!(source.getEntity() instanceof ServerPlayer player)) {
            return;
        }
        Map<String, ParsedArgument<CommandSourceStack, ?>> arguments = contextBuilder.getArguments();
        if (!arguments.isEmpty()) {
            StringBuilder argMsg = new StringBuilder("✅ 解析后的参数（CommandContextBuilder）：\n");
            for (Map.Entry<String, ParsedArgument<CommandSourceStack, ?>> entry : arguments.entrySet()) {
                argMsg.append("- ").append(entry.getKey()).append(" = ").append(entry.getValue().getResult()).append("\n");
            }
            player.sendSystemMessage(Component.literal(argMsg.toString()));
        }
        String fullCommand = event.getParseResults().getReader().getString().trim();
        String[] commandParts = splitCommand(fullCommand);
        String commandName = commandParts.length > 0 ? commandParts[0] : "";
        String[] commandArgs = commandParts.length > 1 ? java.util.Arrays.copyOfRange(commandParts, 1, commandParts.length) : new String[0];
        player.sendSystemMessage(Component.literal(
                "📌 字符串拆分结果：\n" +
                        "指令名：" + commandName + "\n" +
                        "参数列表：" + java.util.Arrays.toString(commandArgs)
        ));
    }

    private String[] splitCommand(String fullCommand) {
        List<String> parts = new java.util.ArrayList<>();
        boolean inQuotes = false;
        StringBuilder currentPart = new StringBuilder();
        for (char c : fullCommand.toCharArray()) {
            if (c == '"') {
                inQuotes = !inQuotes;
            } else if (c == ' ' && !inQuotes) {
                if (!currentPart.isEmpty()) {
                    parts.add(currentPart.toString());
                    currentPart.setLength(0);
                }
            } else {
                currentPart.append(c);
            }
        }
        if (!currentPart.isEmpty()) {
            parts.add(currentPart.toString());
        }
        return parts.toArray(new String[0]);
    }
}
