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
 * 指令发布/失败/完成时的动作栏动画。
 * 发布动画：文本从全部乱码（obfuscated）逐字解码为明文，格式：致 [ {玩家名} ] {事件描述}
 * 失败动画：乱码 + *_ERROR._*，红色
 * 完成动画：乱码 + *_CLEAR._*，淡蓝色
 */
public class PrescriptPublishAnimation {
    private static final Map<UUID, PrescriptPublishAnimation> ACTIVE_ANIMATIONS = new ConcurrentHashMap<>();

    private enum AnimType { PUBLISH, ERROR, CLEAR }

    // 发布动画参数
    private static final int PUBLISH_TOTAL_TICKS = 45;
    private static final int PUBLISH_GARBLE_TICKS = 8;

    // 失败动画参数
    private static final int ERROR_TOTAL_TICKS = 30;
    private static final int ERROR_GARBLE_TICKS = 10;
    private static final String ERROR_SUFFIX = "*_ERROR._*";

    // 完成动画参数
    private static final int CLEAR_TOTAL_TICKS = 30;
    private static final int CLEAR_GARBLE_TICKS = 10;
    private static final String CLEAR_SUFFIX = "*_CLEAR._*";

    private static final Style HIDDEN_STYLE = Style.EMPTY.withObfuscated(true).withColor(ChatFormatting.DARK_AQUA);
    private static final Style PREFIX_STYLE = Style.EMPTY.withColor(ChatFormatting.AQUA);
    private static final Style NAME_STYLE = Style.EMPTY.withColor(ChatFormatting.DARK_AQUA).withBold(true);
    private static final Style SEPARATOR_STYLE = Style.EMPTY.withColor(ChatFormatting.AQUA);
    private static final Style DESC_STYLE = Style.EMPTY.withColor(ChatFormatting.AQUA);

    private static final Style ERROR_HIDDEN_STYLE = Style.EMPTY.withObfuscated(true).withColor(ChatFormatting.DARK_RED);
    private static final Style ERROR_TEXT_STYLE = Style.EMPTY.withColor(ChatFormatting.RED).withBold(true);

    private static final Style CLEAR_HIDDEN_STYLE = Style.EMPTY.withObfuscated(true).withColor(ChatFormatting.AQUA);
    private static final Style CLEAR_TEXT_STYLE = Style.EMPTY.withColor(ChatFormatting.AQUA).withBold(true);

    private final ServerPlayer player;
    private final String fullText;
    private final int prefixEnd;
    private final int nameEnd;
    private final int separatorEnd;
    private final AnimType animType;
    private final int totalTicks;
    private final int garbleTicks;
    private int tick = 0;

    private PrescriptPublishAnimation(ServerPlayer player, String eventDesc, AnimType animType) {
        this.player = player;
        this.animType = animType;
        String playerName = player.getName().getString();
        String prefix = "致 [ ";
        String separator = " ] ";
        String suffix = switch (animType) {
            case ERROR -> ERROR_SUFFIX;
            case CLEAR -> CLEAR_SUFFIX;
            default -> "";
        };
        // 发布动画显示完整文本，失败/完成动画仅显示 suffix
        if (animType == AnimType.PUBLISH) {
            this.fullText = prefix + playerName + separator + eventDesc + suffix;
        } else {
            this.fullText = suffix;
        }
        this.prefixEnd = prefix.length();
        this.nameEnd = prefixEnd + playerName.length();
        this.separatorEnd = nameEnd + separator.length();
        this.totalTicks = switch (animType) {
            case ERROR -> ERROR_TOTAL_TICKS;
            case CLEAR -> CLEAR_TOTAL_TICKS;
            default -> PUBLISH_TOTAL_TICKS;
        };
        this.garbleTicks = switch (animType) {
            case ERROR -> ERROR_GARBLE_TICKS;
            case CLEAR -> CLEAR_GARBLE_TICKS;
            default -> PUBLISH_GARBLE_TICKS;
        };
    }

    public static void start(ServerPlayer player, String eventDesc) {
        ACTIVE_ANIMATIONS.put(player.getUUID(), new PrescriptPublishAnimation(player, eventDesc, AnimType.PUBLISH));
    }

    public static void startError(ServerPlayer player, String eventDesc) {
        ACTIVE_ANIMATIONS.put(player.getUUID(), new PrescriptPublishAnimation(player, eventDesc, AnimType.ERROR));
    }

    public static void startClear(ServerPlayer player, String eventDesc) {
        ACTIVE_ANIMATIONS.put(player.getUUID(), new PrescriptPublishAnimation(player, eventDesc, AnimType.CLEAR));
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
        if (tick >= totalTicks) {
            ACTIVE_ANIMATIONS.remove(player.getUUID());
            return;
        }

        switch (animType) {
            case PUBLISH -> doPublishTick();
            case ERROR -> doSuffixTick(ERROR_SUFFIX, ERROR_HIDDEN_STYLE, ERROR_TEXT_STYLE);
            case CLEAR -> doSuffixTick(CLEAR_SUFFIX, CLEAR_HIDDEN_STYLE, CLEAR_TEXT_STYLE);
        }
        tick++;
    }

    private void doPublishTick() {
        int revealTicks = totalTicks - garbleTicks;
        int revealedCount;
        if (tick < garbleTicks) {
            revealedCount = 0;
        } else if (tick >= totalTicks - 1) {
            revealedCount = fullText.length(); // 最后一帧确保全部解码
        } else {
            float progress = (float) (tick - garbleTicks) / revealTicks;
            revealedCount = Math.min(Math.round(progress * fullText.length()), fullText.length());
        }
        player.displayClientMessage(buildPublishComponent(revealedCount), true);
    }

    private void doSuffixTick(String suffix, Style hiddenStyle, Style textStyle) {
        int revealTicks = totalTicks - garbleTicks;
        int suffixLen = suffix.length();
        int textBeforeSuffix = fullText.length() - suffixLen;
        int revealedSuffixCount;
        if (tick < garbleTicks) {
            revealedSuffixCount = 0;
        } else if (tick >= totalTicks - 1) {
            revealedSuffixCount = suffixLen; // 最后一帧确保全部解码
        } else {
            float progress = (float) (tick - garbleTicks) / revealTicks;
            revealedSuffixCount = Math.min(Math.round(progress * suffixLen), suffixLen);
        }

        MutableComponent result = Component.empty();
        // 前半段始终乱码
        result.append(Component.literal(fullText.substring(0, textBeforeSuffix)).withStyle(hiddenStyle));
        // 后半段（suffix）逐字解码
        if (revealedSuffixCount <= 0) {
            result.append(Component.literal(suffix).withStyle(hiddenStyle));
        } else if (revealedSuffixCount >= suffixLen) {
            result.append(Component.literal(suffix).withStyle(textStyle));
        } else {
            result.append(Component.literal(suffix.substring(0, revealedSuffixCount)).withStyle(textStyle));
            result.append(Component.literal(suffix.substring(revealedSuffixCount)).withStyle(hiddenStyle));
        }
        player.displayClientMessage(result, true);
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
