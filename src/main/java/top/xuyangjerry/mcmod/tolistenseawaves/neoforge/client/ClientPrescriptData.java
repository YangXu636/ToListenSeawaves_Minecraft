package top.xuyangjerry.mcmod.tolistenseawaves.neoforge.client;

import net.minecraft.client.Minecraft;
import net.minecraft.resources.Identifier;
import top.xuyangjerry.mcmod.tolistenseawaves.neoforge.init.ToListenSeawavesItems;

public class ClientPrescriptData {
    private static ClientPrescriptData INSTANCE;
    private String currentTaskId = null;
    private String currentTaskDesc = null;
    private long timeLimit = 0;
    private long remainingTicks = 0;
    private int totalCount = 0;
    private int completedCount = 0;
    public boolean animationPlaying = false;
    private boolean showTime = true;

    private ClientPrescriptData() {}

    public static ClientPrescriptData getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new ClientPrescriptData();
        }
        return INSTANCE;
    }

    public void updateTaskData(String taskId, String taskDesc, long timeLimit, long remainingTicks, int totalCount, int completedCount, boolean animationPlaying, boolean showTime) {
        this.currentTaskId = taskId;
        this.currentTaskDesc = taskDesc;
        this.timeLimit = timeLimit;
        this.remainingTicks = remainingTicks;
        this.totalCount = totalCount;
        this.completedCount = completedCount;
        this.animationPlaying = animationPlaying;
        this.showTime = showTime;
    }

    public void clear() {
        this.currentTaskId = null;
        this.currentTaskDesc = null;
        this.timeLimit = 0;
        this.remainingTicks = 0;
        this.totalCount = 0;
        this.completedCount = 0;
        this.animationPlaying = false;
        this.showTime = true;
    }

    public boolean isHoldingItemA() {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) return false;
        return mc.player.getMainHandItem().is(ToListenSeawavesItems.PRESCRIPT_DEVICE) || mc.player.getOffhandItem().is(ToListenSeawavesItems.PRESCRIPT_DEVICE);
    }

    @Override
    public String toString() {
        return "ClientPrescriptData{" +
                "currentTaskId='" + currentTaskId + '\'' +
                ", currentTaskDesc='" + currentTaskDesc + '\'' +
                ", timeLimit=" + timeLimit +
                ", remainingTicks=" + remainingTicks +
                ", totalCount=" + totalCount +
                ", completedCount=" + completedCount +
                ", animationPlaying=" + animationPlaying +
                '}';
    }

    public String getCurrentTaskDesc() { return currentTaskDesc; }
    public long getTimeLimit() { return  timeLimit; }
    public long getRemainingTicks() { return remainingTicks; }
    public int getTotalCount() { return totalCount; }
    public int getCompletedCount() { return completedCount; }
    public boolean hasActiveTask() { return currentTaskId != null; }
    public boolean isShowTime() { return showTime; }
}
