package top.xuyangjerry.mcmod.tolistenseawaves.neoforge.client.screens;

import com.mojang.logging.LogUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Player;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderGuiLayerEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import org.slf4j.Logger;
import top.xuyangjerry.mcmod.tolistenseawaves.neoforge.ToListenSeawaves;
import top.xuyangjerry.mcmod.tolistenseawaves.neoforge.client.ClientPrescriptData;
import top.xuyangjerry.mcmod.tolistenseawaves.neoforge.client.gui.contextualbar.PrescriptDataBarRenderer;

import java.text.DecimalFormat;

@EventBusSubscriber(value = Dist.CLIENT, modid = ToListenSeawaves.MOD_ID)
public class PrescriptHudOverlay {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final DecimalFormat TIME_FORMAT = new DecimalFormat("#.0");

    @SubscribeEvent
    public static void onRenderExperienceBar(RenderGuiLayerEvent.Pre event) {
        if (!event.getName().equals(Identifier.withDefaultNamespace("contextual_info_bar_background"))
                && !event.getName().equals(Identifier.withDefaultNamespace("experience_level"))
                && !event.getName().equals(Identifier.withDefaultNamespace("contextual_info_bar"))) {
            return;
        }
        Minecraft mc = Minecraft.getInstance();
        ClientPrescriptData prescriptData = ClientPrescriptData.getInstance();
        if (prescriptData.isHoldingItemA() && prescriptData.hasActiveTask()) {
            event.setCanceled(true);
            if (event.getName().equals(Identifier.withDefaultNamespace("experience_level"))) {
                PrescriptDataBarRenderer renderer = new PrescriptDataBarRenderer(mc, prescriptData.getCurrentTaskDesc(), prescriptData.getCompletedCount(), prescriptData.getTotalCount(), (float) prescriptData.getRemainingTicks() / prescriptData.getTimeLimit());
                renderer.renderBackground(event.getGuiGraphics(), event.getPartialTick());
                renderer.render(event.getGuiGraphics(), event.getPartialTick());
            }
        }
    }

    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        if (event == null) {
            return;
        }
        Player player = event.getEntity();
        if (player.level().isClientSide()) {
            return;
        }
        ClientPrescriptData prescriptData = ClientPrescriptData.getInstance();
        if (prescriptData.isHoldingItemA() && prescriptData.hasActiveTask()) {
            player.displayClientMessage(Component.literal(prescriptData.getCurrentTaskDesc()), true);
        }
    }
}
