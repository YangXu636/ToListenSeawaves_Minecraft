package top.xuyangjerry.mcmod.tolistenseawaves.neoforge.server;

import com.mojang.logging.LogUtils;
import net.minecraft.advancements.*;
import net.minecraft.advancements.criterion.MinMaxBounds;
import net.minecraft.resources.Identifier;
import net.minecraft.server.ServerAdvancementManager;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.common.util.FakePlayer;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import top.xuyangjerry.mcmod.tolistenseawaves.neoforge.component.PlayerPrescriptDataComponent;
import top.xuyangjerry.mcmod.tolistenseawaves.neoforge.init.ToListenSeawavesAttachmentType;
import top.xuyangjerry.mcmod.tolistenseawaves.neoforge.prescripts.*;

import java.util.*;

public class PlayerPrescripts {
    private static final Logger LOGGER = LogUtils.getLogger();
    private ServerPlayer player;
    private long completeCount = 0;
    @Nullable private PrescriptHolder currentPrescript;
    private PrescriptProgress currentProgress = new PrescriptProgress();
    public long remainingCdTicks = 0;
    private boolean progressChanged = false;
    private boolean isFirstPacket = true;

    public PlayerPrescripts(ServerPrescriptManager manager, ServerPlayer player) {
        this.player = player;
        loadFromDataComponent();
        checkForAutomaticTriggers(manager);
        registerCurrentPrescriptListeners();
    }

    public void setPlayer(ServerPlayer player) {
        this.player = player;
    }

    public PlayerPrescriptDataComponent getCurrentPrescript(ServerPlayer player) {
        if (player == null || !player.hasData(ToListenSeawavesAttachmentType.PRESCRIPT)) {
            return PlayerPrescriptDataComponent.EMPTY;
        }
        return player.getData(ToListenSeawavesAttachmentType.PRESCRIPT);
    }

    public boolean setCurrentPrescript(ServerPlayer player, PlayerPrescriptDataComponent dataComponent) {
        try {
            player.setData(ToListenSeawavesAttachmentType.PRESCRIPT, dataComponent);
            return true;
        } catch (Exception e) {
            LOGGER.warn(e.toString());
            return false;
        }
    }

    public int getRandomCd(MinMaxBounds.Ints bounds) {
        int min = bounds.min().orElse(0);
        int max = bounds.max().orElse(min);
        return min == max ? min : player.getRandom().nextInt(min, max + 1);
    }

    public void updateTick() {
        if (ServerPrescriptManager.getInstance() != null && this.currentPrescript != null) {
            this.currentProgress.updateTick();
        }
        if (remainingCdTicks > 0) {
            remainingCdTicks--;
        }
    }

    public boolean IsExpired() {
        if (this.currentPrescript == null) {
            return true;
        }
        return this.currentPrescript.value().timeLimitTicks() > 0 && this.currentProgress.getTicks() > this.currentPrescript.value().timeLimitTicks();
    }

    public boolean isCdExpired() {
        return this.remainingCdTicks <= 0;
    }

    public boolean IsCompleted() {
        if (this.currentPrescript == null) {
            return true;
        }
        return this.currentProgress.isDone();
    }

    public void loadFromDataComponent() {
        PlayerPrescriptDataComponent data = this.getCurrentPrescript(player);
        if (data == null) {
            this.completeCount = 0;
            this.currentPrescript = null;
            this.currentProgress = new PrescriptProgress();
            this.remainingCdTicks = 0;
            return;
        }
        if (data.currentId().isEmpty()) {
            this.remainingCdTicks = data.remainingCdTicks();
            return;
        }
        this.currentPrescript = ServerPrescriptManager.get(data.currentId().get());
        if (this.currentPrescript != null) {
            this.completeCount = data.complete();
            this.currentProgress = data.currentProgress();
            this.currentProgress.update(this.currentPrescript.value().requirements());
            this.remainingCdTicks = data.remainingCdTicks();
        } else {
            LOGGER.warn("Ignored invalid current prescript '{}' for player {}", data.currentId(), player.getName().getString());
            this.completeCount = 0;
            this.currentPrescript = null;
            this.currentProgress = new PrescriptProgress();
            this.remainingCdTicks = 0;
        }
    }

    public void saveToDataComponent() {
        Optional<Identifier> op_id = currentPrescript != null ? Optional.ofNullable(currentPrescript.id()) : Optional.empty();
        this.setCurrentPrescript(player, new PlayerPrescriptDataComponent(this.completeCount, op_id, currentProgress, this.remainingCdTicks));
    }

    // 停止监听当前Prescript的触发器
    public void stopListening() {
        if (currentPrescript == null) return;
        for (PrescriptCriterionTrigger<?> trigger : PrescriptCriteriaTriggers.PRESCRIPT_TRIGGERS.getRegistry().get()) {
            trigger.removePlayerListeners(this);
        }
    }

    // 重新加载当前Prescript（用于资源重载）
    public void reload(ServerPrescriptManager manager) {
        saveToDataComponent();
        stopListening();
        this.currentPrescript = null;
        this.currentProgress = new PrescriptProgress();
        this.progressChanged = false;
        this.isFirstPacket = true;
        loadFromDataComponent();
        checkForAutomaticTriggers(manager);
        registerCurrentPrescriptListeners();
    }

    // 检查无条件Prescript自动完成
    private void checkForAutomaticTriggers(ServerPrescriptManager manager) {
        if (currentPrescript == null) return;
        Prescript prescript = currentPrescript.value();
        if (prescript.criteria().isEmpty()) {
            award(currentPrescript, "");
            prescript.rewards().grant(this.player);
        }
    }

    // 授予Prescript进度（核心方法）
    public boolean award(PrescriptHolder prescript, String criterionKey) {
        if (player instanceof FakePlayer) return false;

        //this.remainingCdTicks =

        if (this.currentPrescript != null && !this.currentPrescript.equals(prescript)) {
            stopListening();
            this.currentProgress = new PrescriptProgress();
        }
        this.currentPrescript = prescript;

        boolean flag = false;
        boolean wasDone = currentProgress.isDone();
        if (currentProgress.grantProgress(criterionKey)) {
            unregisterCurrentPrescriptListeners();
            this.progressChanged = true;
            flag = true;
            if (!wasDone && currentProgress.isDone()) {
                prescript.value().rewards().grant(this.player);
                PrescriptPublisher.sendPrescriptCompleteMessage(this.player, prescript);
                this.completeCount++;
            }
        }
        if (!wasDone && currentProgress.isDone()) {
            this.progressChanged = true;
        }
        saveToDataComponent();
        return flag;
    }

    // 撤销Prescript进度
    public boolean revoke(PrescriptHolder prescript, String criterionKey) {
        if (!prescript.equals(this.currentPrescript)) return false;

        boolean flag = false;
        boolean wasDone = currentProgress.isDone();
        if (currentProgress.revokeProgress(criterionKey)) {
            registerCurrentPrescriptListeners();
            this.progressChanged = true;
            flag = true;
        }

        if (wasDone && !currentProgress.isDone()) {
            this.progressChanged = true;
        }
        saveToDataComponent();
        return flag;
    }

    public void revoke() {
        if (this.currentPrescript == null) return ;

        PrescriptPublisher.sendPrescriptFailMessage(this.player, this.currentPrescript);
        this.currentProgress = new PrescriptProgress();
        stopListening();
        this.progressChanged = true;
        saveToDataComponent();
    }

    // 注册当前Prescript的触发器监听
    void registerCurrentPrescriptListeners() {
        if (currentPrescript == null || currentProgress.isDone()) return;

        Prescript prescript = currentPrescript.value();
        for (Map.Entry<String, PrescriptCriterion<?>> entry : prescript.criteria().entrySet()) {
            String criterionKey = entry.getKey();
            CriterionProgress criterionProgress = currentProgress.getCriterion(criterionKey);
            if (criterionProgress != null && !criterionProgress.isDone()) {
                registerListener(currentPrescript, criterionKey, entry.getValue());
            }
        }
    }

    // 注销当前Prescript的触发器监听
    private void unregisterCurrentPrescriptListeners() {
        if (currentPrescript == null) return;

        Prescript prescript = currentPrescript.value();
        for (Map.Entry<String, PrescriptCriterion<?>> entry : prescript.criteria().entrySet()) {
            String criterionKey = entry.getKey();
            CriterionProgress criterionProgress = currentProgress.getCriterion(criterionKey);
            if (criterionProgress != null && (criterionProgress.isDone() || currentProgress.isDone())) {
                removeListener(currentPrescript, criterionKey, entry.getValue());
            }
        }
    }

    // 注册单个触发器监听
    private <T extends CriterionTriggerInstance> void registerListener(
            PrescriptHolder prescript, String criterionKey, PrescriptCriterion<T> criterion
    ) {
        criterion.trigger().addPlayerListener(this, new PrescriptCriterionTrigger.Listener<>(
                criterion.triggerInstance(), prescript, criterionKey
        ));
    }

    // 移除单个触发器监听
    private <T extends net.minecraft.advancements.CriterionTriggerInstance> void removeListener(
            PrescriptHolder prescript, String criterionKey, PrescriptCriterion<T> criterion
    ) {
        criterion.trigger().removePlayerListener(this, new PrescriptCriterionTrigger.Listener<>(
                criterion.triggerInstance(), prescript, criterionKey
        ));
    }

    // 同步进度到客户端（精简版）
    /*public void flushDirty(ServerPlayer player, boolean showAdvancements) {
        if (isFirstPacket || progressChanged) {
            Map<Identifier, PrescriptProgress> progressMap = new HashMap<>();
            if (currentPrescript != null && progressChanged) {
                progressMap.put(currentPrescript.id(), currentProgress);
            }

            // 发送精简的进度同步包
            *//*player.connection.send(new ClientboundUpdateAdvancementsPacket(
                    isFirstPacket, Collections.emptySet(), Collections.emptySet(), progressMap, showAdvancements
            ));*//*

            this.isFirstPacket = false;
            this.progressChanged = false;
        }
    }*/

    // 获取或初始化当前Prescript进度（单例逻辑）
    public PrescriptProgress getOrStartProgress(PrescriptHolder prescript) {
        if (!prescript.equals(this.currentPrescript)) {
            this.currentPrescript = prescript;
            this.currentProgress = new PrescriptProgress();
            this.currentProgress.update(prescript.value().requirements());
            this.progressChanged = true;
            saveToDataComponent();
        }
        return this.currentProgress;
    }

    //private void markForVisibilityUpdate(PrescriptHolder holder) {}
    //private void updateTreeVisibility(Object node, Set<?> set, Set<?> set1) {}

    public void clearCurrentPrescript() {
        stopListening();
        this.currentPrescript = null;
        this.currentProgress = new PrescriptProgress();
        this.progressChanged = true;
        saveToDataComponent();
    }

    @Nullable
    public PrescriptHolder getCurrentPrescript() {
        return currentPrescript;
    }

    public int getCompleteCount() {
        return (int)completeCount;
    }

    public int getTotalCount() {
        if (this.currentPrescript == null) {
            return 0;
        }
        return this.currentProgress.getTotalCount();
    }
}