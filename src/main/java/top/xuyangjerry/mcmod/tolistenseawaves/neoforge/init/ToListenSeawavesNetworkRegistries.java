package top.xuyangjerry.mcmod.tolistenseawaves.neoforge.init;

import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;
import top.xuyangjerry.mcmod.tolistenseawaves.neoforge.ToListenSeawaves;
import top.xuyangjerry.mcmod.tolistenseawaves.neoforge.client.ClientPrescriptData;
import top.xuyangjerry.mcmod.tolistenseawaves.neoforge.network.PrescriptSyncPacket;

@EventBusSubscriber(modid = ToListenSeawaves.MOD_ID)
public class ToListenSeawavesNetworkRegistries {

    @SubscribeEvent
    public static void registerPackets(RegisterPayloadHandlersEvent event) {
        PayloadRegistrar registrar = event.registrar(ToListenSeawaves.MOD_ID)
                .versioned("1.0")
                .optional();

        // 注册任务同步包
        registrar.playToClient(
                PrescriptSyncPacket.TYPE,
                PrescriptSyncPacket.STREAM_CODEC,
                (packet, context) -> {
                    // 客户端处理同步包
                    context.enqueueWork(() -> {
                        ClientPrescriptData data = ClientPrescriptData.getInstance();
                        data.updateTaskData(
                                packet.prescriptId().orElse(null),
                                packet.prescriptDesc().orElse(null),
                                packet.timeLimit(),
                                packet.remainingTicks(),
                                packet.totalCount(),
                                packet.completedCount(),
                                packet.animationPlaying(),
                                packet.showTime()
                        );
                    });
                }
        );
    }
}
