package top.xuyangjerry.mcmod.tolistenseawaves.neoforge.server;

import com.mojang.logging.LogUtils;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.PlayerList;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.level.LevelEvent;
import net.neoforged.neoforge.event.tick.LevelTickEvent;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import top.xuyangjerry.mcmod.tolistenseawaves.neoforge.ToListenSeawaves;
import top.xuyangjerry.mcmod.tolistenseawaves.neoforge.init.ToListenSeawavesDataComponents;
import top.xuyangjerry.mcmod.tolistenseawaves.neoforge.network.PrescriptSyncPacket;
import top.xuyangjerry.mcmod.tolistenseawaves.neoforge.prescripts.PrescriptHolder;
import top.xuyangjerry.mcmod.tolistenseawaves.neoforge.tool.XyTools;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@EventBusSubscriber(modid = ToListenSeawaves.MOD_ID)
public class PrescriptPublisher {
    public static final int BASE_CD_TICKS = 120;
    private static final Logger LOGGER = LogUtils.getLogger();
    private static PrescriptPublisher INSTANCE;
    private final Map<UUID, PlayerPrescripts> playerPrescripts = new ConcurrentHashMap<>();

    private PrescriptPublisher() {}

    public static PrescriptPublisher getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new PrescriptPublisher();
        }
        return INSTANCE;
    }

    public static PlayerPrescripts getPrescript(ServerPlayer sPlayer) {
        PrescriptPublisher pp = getInstance();
        UUID uuid = sPlayer.getUUID();
        if (!pp.playerPrescripts.containsKey(uuid)) {
            pp.playerPrescripts.putIfAbsent(uuid, new PlayerPrescripts(ServerPrescriptManager.getInstance(), sPlayer));
        }
        return pp.playerPrescripts.get(uuid);
    }

    private void updatePlayerPrescriptTick(ServerPlayer player) {
        PlayerPrescripts prescripts = getPrescript(player);
        prescripts.updateTick();

        if (!prescripts.isCdExpired()) {
            return;
        }

        if (prescripts.getCurrentPrescript() == null) {
            PrescriptHolder newPrescript = pickValidPrescript(player);
            if (newPrescript != null && assignPrescriptToPlayer(player, newPrescript)) {
                LOGGER.info("发布指令({}) 致[{}], {}", newPrescript.id().toString(), player.getName().getString(), newPrescript.value().display().description().getString());
            } else {
                LOGGER.warn("未知错误: 无法发布指令");
            }
        }
        syncPrescriptDataToClient(player, prescripts);

        if (!prescripts.IsExpired() && prescripts.IsCompleted()) {
            PrescriptHolder completedPrescript = prescripts.getCurrentPrescript();
            int cd = BASE_CD_TICKS + prescripts.getRandomCd(completedPrescript.value().cd());
            prescripts.clearCurrentPrescript();
            prescripts.remainingCdTicks = cd;
            sendPrescriptCompleteMessage(player, completedPrescript);
            PrescriptSyncPacket.sendToPlayer(player, PrescriptSyncPacket.EMPTY);
        } else if (prescripts.IsExpired()) {
            PrescriptHolder expiredPrescript = prescripts.getCurrentPrescript();
            int cd = expiredPrescript != null ? BASE_CD_TICKS + prescripts.getRandomCd(expiredPrescript.value().cd()) : BASE_CD_TICKS;
            prescripts.revoke();
            prescripts.remainingCdTicks = cd;
            sendPrescriptExpireMessage(player, expiredPrescript);
            PrescriptSyncPacket.sendToPlayer(player, PrescriptSyncPacket.EMPTY);
        }
    }

    private PrescriptHolder pickValidPrescript(ServerPlayer player) {
        Collection<PrescriptHolder> allPrescripts = ServerPrescriptManager.getAllPrescripts();
        if (allPrescripts.isEmpty()) {
            LOGGER.warn("No prescripts available to assign to player {}", player.getName().getString());
            return null;
        }

        // 筛选符合发布条件的Prescript
        List<PrescriptHolder> validPrescripts = new ArrayList<>();
        for (PrescriptHolder holder : allPrescripts) {
            if (holder.value().publishConditions().canIPublish()) {
                validPrescripts.add(holder);
            }
        }

        if (validPrescripts.isEmpty()) {
            LOGGER.warn("No valid prescripts for player {} (all failed publish conditions)", player.getName().getString());
            return null;
        }
        LOGGER.info("可发布指令：{}", StringUtils.join(validPrescripts.stream().map(x -> x.id().toString()).toArray(), ", "));
        // 随机抽取一个（如果需要加权随机，可使用XyTools）
        // 方式1：简单随机
        //return validPrescripts.get(new Random().nextInt(validPrescripts.size()));

        // 方式2：加权随机（如果需要按权重抽取，示例）
        // List<Float> probabilities = validPrescripts.stream().map(h -> 1.0f).toList(); // 等权重，可自定义
        return XyTools.GetMemberRandom(validPrescripts);
    }

    private void syncPrescriptDataToClient(ServerPlayer player, PlayerPrescripts prescripts) {
        PrescriptHolder holder = prescripts.getCurrentPrescript();
        if (holder == null) return;

        long remainingTicks = holder.value().timeLimitTicks() - prescripts.getOrStartProgress(holder).getTicks();
        remainingTicks = Math.max(0, remainingTicks);

        String desc = holder.value().display().description().getString();

        PrescriptSyncPacket packet = new PrescriptSyncPacket(
                Optional.of(holder.id().toString()),
                Optional.of(desc),
                holder.value().timeLimitTicks(),
                remainingTicks,
                prescripts.getTotalCount(),
                prescripts.getCompleteCount(),
                PrescriptPublishAnimation.isActive(player.getUUID()),
                holder.value().display().showTime()
        );
        PrescriptSyncPacket.sendToPlayer(player, packet);
    }

    public boolean assignPrescriptToPlayer(ServerPlayer player, PrescriptHolder prescriptHolder) {
        if (player == null || prescriptHolder == null) return false;
        PlayerPrescripts playerPrescripts = getPrescript(player);

        playerPrescripts.stopListening();

        playerPrescripts.getOrStartProgress(prescriptHolder);
        sendPrescriptPublishMessage(player, prescriptHolder);

        playerPrescripts.registerCurrentPrescriptListeners();
        playerPrescripts.saveToDataComponent();

        syncPrescriptDataToClient(player, playerPrescripts);

        return true;
    }

    private void sendPrescriptPublishMessage(ServerPlayer player, PrescriptHolder holder) {
        String eventDesc = holder.value().display().description().getString();
        PrescriptPublishAnimation.start(player, eventDesc);
    }

    public static void sendPrescriptCompleteMessage(ServerPlayer player, PrescriptHolder prescript) {
        String eventDesc = prescript != null ? prescript.value().display().description().getString() : "";
        PrescriptPublishAnimation.startClear(player, eventDesc);
    }

    public static void sendPrescriptFailMessage(ServerPlayer player, PrescriptHolder currentPrescript) {
        String eventDesc = currentPrescript != null ? currentPrescript.value().display().description().getString() : "";
        PrescriptPublishAnimation.startError(player, eventDesc);
    }

    private void sendPrescriptExpireMessage(ServerPlayer player, PrescriptHolder currentPrescript) {
        String eventDesc = currentPrescript != null ? currentPrescript.value().display().description().getString() : "";
        PrescriptPublishAnimation.startError(player, eventDesc);
    }

    @SubscribeEvent
    public static void onWorldTick(LevelTickEvent.Post event) {
        if (event.getLevel().isClientSide()) {
            return;
        }
        Objects.requireNonNull(event.getLevel().getServer()).getPlayerList().getPlayers().forEach(player -> {
            PrescriptPublishAnimation.tickPlayer(player);
            PrescriptPublisher.getInstance().updatePlayerPrescriptTick(player);
        });
    }

    @SubscribeEvent
    public static void onWorldUnload(LevelEvent.Unload event) {
        event.getLevel().players().forEach(player -> {
            if (!(player instanceof ServerPlayer serverPlayer)) { return; }
            PlayerPrescripts prescripts = getPrescript(serverPlayer);
            if (prescripts != null) {
                prescripts.saveToDataComponent();
                LOGGER.info("Saved prescript data for player {} on world unload", player.getName().getString());
            }
        });
    }

    @SubscribeEvent
    public static void onPlayerLogin(PlayerEvent.PlayerLoggedInEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) {
            return;
        }
        PlayerPrescripts prescripts = getPrescript(player);
        if (prescripts != null) {
            prescripts.loadFromDataComponent();
            LOGGER.info("Loaded prescript data for player {} on login", player.getName().getString());
        }
    }

    @SubscribeEvent
    public static void onPlayerLogout(PlayerEvent.PlayerLoggedOutEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) {
            return;
        }
        PrescriptPublishAnimation.remove(player.getUUID());
        PlayerPrescripts prescripts = getPrescript(player);
        if (prescripts != null) {
            prescripts.saveToDataComponent();
            LOGGER.info("Saved prescript data for player {} on logout", player.getName().getString());
        }
    }
}
