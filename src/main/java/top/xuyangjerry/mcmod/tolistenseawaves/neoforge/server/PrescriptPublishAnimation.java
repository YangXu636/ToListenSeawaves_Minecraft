package top.xuyangjerry.mcmod.tolistenseawaves.neoforge.server;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.server.level.ServerPlayer;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 指令发布/失败时的动作栏动画。
 * 发布：乱码逐字解码为明文 "致 [ {玩家名} ] {事件描述}"（青蓝色）
 * 失败/超时：乱码 + *_ERROR_*（红色）
 */
public class PrescriptPublishAnimation {
    private static final Map<UUID, PrescriptPublishAnimation> ACTIVE_ANIMATIONS = new ConcurrentHashMap<>();

    // 发布动画参数
    private static final int PUBLISH_TOTAL_TICKS = 45;
    private static final int PUBLISH_INITIAL_GARBLE_TICKS = 8;
    private static final int PUBLISH_REVEAL_TICKS = PUBLISH_TOTAL_TICKS - PUBLISH_INITIAL_GARBLE_TICKS;

    // 失败动画参数
    private static final int FAIL_TOTAL_TICKS = 35;
    private static final int FAIL_GARBLE_TICKS = 15;

    private static final Style HIDDEN_STYLE = Style.EMPTY.withObfuscated(true).withColor(ChatFormatting.DARK_AQUA);
    private static final Style PREFIX_STYLE = Style.EMPTY.withColor(ChatFormatting.AQUA);
    private static final Style NAME_STYLE = Style.EMPTY.withColor(ChatFormatting.AQUA).withBold(true);
    private static final Style SEPARATOR_STYLE = Style.EMPTY.withColor(ChatFormatting.AQUA);
    private static final Style DESC_STYLE = Style.EMPTY.withColor(ChatFormatting.DARK_AQUA);

    // 失败动画样式
    private static final Style FAIL_HIDDEN_STYLE = Style.EMPTY.withObfuscated(true).withColor(ChatFormatting.DARK_RED);
    private static final Style FAIL_ERROR_STYLE = Style.EMPTY.withColor(ChatFormatting.RED).withBold(true);

    private final ServerPlayer player;
    private final String fullText;
    private final int prefixEnd;
    private final int nameEnd;
    private final int separatorEnd;
    private int tick = 0;
    private final boolean isFailure;

    private PrescriptPublishAnimation(ServerPlayer player, String eventDesc) {
        this(player, eventDesc, false);
    }

    private PrescriptPublishAnimation(ServerPlayer player, String eventDesc, boolean isFailure) {
        this.player = player;
        this.isFailure = isFailure;
        String playerName = player.getName().getString();
        String prefix = "致 [ ";
        String separator = " ] ";
        this.fullText = prefix + playerName + separator + eventDesc;
        this.prefixEnd = prefix.length();
        this.nameEnd = prefixEnd + playerName.length();
        this.separatorEnd = nameEnd + separator.length();
    }

    public static void start(ServerPlayer player, String eventDesc) {
        ACTIVE_ANIMATIONS.put(player.getUUID(), new PrescriptPublishAnimation(player, eventDesc));
    }

    public static void startFailure(ServerPlayer player, String eventDesc) {
        ACTIVE_ANIMATIONS.put(player.getUUID(), new PrescriptPublishAnimation(player, eventDesc, true));
    }

    public static boolean isActive(UUID playerUUID) {
        return ACTIVE_ANIMATIONS.containsKey(playerUUID);
    }

    public static void tickPlayer(ServerPlayer player) {
        PrescriptPublishAnimation anim = ACTIVE_ANIMATIONS.get(player.getUUID());
        if (anim != null) {
            anim.doTick();
        }
    }

    public static void remove(UUID playerUUID) {
        ACTIVE_ANIMATIONS.remove(playerUUID);
    }

    private void doTick() {
        if (isFailure) {
            doFailureTick();
        } else {
            doPublishTick();
        }
    }

    private void doPublishTick() {
        if (tick >= PUBLISH_TOTAL_TICKS) {
            ACTIVE_ANIMATIONS.remove(player.getUUID());
            return;
        }

        int revealedCount;
        if (tick < PUBLISH_INITIAL_GARBLE_TICKS) {
            revealedCount = 0;
        } else {
            float progress = (float) (tick - PUBLISH_INITIAL_GARBLE_TICKS) / PUBLISH_REVEAL_TICKS;
            revealedCount = Math.min(Math.round(progress * fullText.length()), fullText.length());
        }

        player.displayClientMessage(buildPublishComponent(revealedCount), true);
        tick++;
    }

    private void doFailureTick() {
        if (tick >= FAIL_TOTAL_TICKS) {
            ACTIVE_ANIMATIONS.remove(player.getUUID());
            return;
        }

        MutableComponent msg = Component.empty();

        if (tick < FAIL_GARBLE_TICKS) {
            // 全乱码阶段
            msg.append(Component.literal(fullText).withStyle(FAIL_HIDDEN_STYLE));
            msg.append(Component.literal(" *_ERROR_*").withStyle(FAIL_ERROR_STYLE));
        } else {
            // 乱码逐渐消退，只留下 *_ERROR_*
            float fadeProgress = (float) (tick - FAIL_GARBLE_TICKS) / (FAIL_TOTAL_TICKS - FAIL_GARBLE_TICKS);
            if (fadeProgress < 0.5f) {
                msg.append(Component.literal(fullText).withStyle(FAIL_HIDDEN_STYLE));
            }
            msg.append(Component.literal(" *_ERROR_*").withStyle(FAIL_ERROR_STYLE));
        }

        player.displayClientMessage(msg, true);
        tick++;
    }

    private MutableComponent buildPublishComponent(int revealedCount) {
        MutableComponent result = Component.empty();
        appendSegment(result, 0, prefixEnd, revealedCount, PREFIX_STYLE);
        appendSegment(result, prefixEnd, nameEnd, revealedCount, NAME_STYLE);
        appendSegment(result, nameEnd, separatorEnd, revealedCount, SEPARATOR_STYLE);
        appendSegment(result, separatorEnd, fullText.length(), revealedCount, DESC_STYLE);
        return result;
    }

    private void appendSegment(MutableComponent result, int segStart, int segEnd, int revealedCount, Style normalStyle) {
        String text = fullText.substring(segStart, segEnd);
        if (revealedCount <= segStart) {
            result.append(Component.literal(text).withStyle(HIDDEN_STYLE));
        } else if (revealedCount >= segEnd) {
            result.append(Component.literal(text).withStyle(normalStyle));
        } else {
            int splitPoint = revealedCount - segStart;
            result.append(Component.literal(text.substring(0, splitPoint)).withStyle(normalStyle));
            result.append(Component.literal(text.substring(splitPoint)).withStyle(HIDDEN_STYLE));
        }
    }
}
