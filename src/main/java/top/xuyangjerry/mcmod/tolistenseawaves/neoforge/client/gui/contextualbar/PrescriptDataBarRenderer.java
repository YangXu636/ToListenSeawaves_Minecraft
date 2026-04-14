package top.xuyangjerry.mcmod.tolistenseawaves.neoforge.client.gui.contextualbar;

import com.mojang.blaze3d.platform.Window;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.contextualbar.ContextualBarRenderer;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import org.jetbrains.annotations.NotNull;

public class PrescriptDataBarRenderer implements ContextualBarRenderer {
    public static final Identifier BLUE_BAR_BK = Identifier.withDefaultNamespace("boss_bar/blue_background");
    public static final Identifier BLUE_BAR_P = Identifier.withDefaultNamespace("boss_bar/blue_progress");
    public static final Identifier YELLOW_BAR_BK = Identifier.withDefaultNamespace("boss_bar/yellow_background");
    public static final Identifier YELLOW_BAR_P = Identifier.withDefaultNamespace("boss_bar/yellow_progress");
    private static final int SEGMENT_GAP = 1;
    private static final int BAR_GAP = -1;
    private static final int SINGLE_BAR_HEIGHT = (HEIGHT - BAR_GAP) / 2;

    private final Minecraft minecraft;
    private final String prescriptTitle;
    private final int completedCount;
    private final int totalCount;
    private final float remainingTimeRatio;

    public PrescriptDataBarRenderer(Minecraft minecraft, String prescriptTitle, int completedCount, int totalCount, float remainingTimeRatio) {
        this.minecraft = minecraft;
        this.prescriptTitle = prescriptTitle;
        this.completedCount = Mth.clamp(completedCount, 0, totalCount);
        this.totalCount = Math.max(totalCount, 1);
        this.remainingTimeRatio = Mth.clamp(remainingTimeRatio, 0.0F, 1.0F);
    }

    @Override
    public void renderBackground(@NotNull GuiGraphics guiGraphics, @NotNull DeltaTracker deltaTracker) {
        Window window = this.minecraft.getWindow();
        int barLeft = left(window);
        int barTop = top(window);

        // 1. 绘制蓝色条背景（上半部分）
        guiGraphics.blitSprite(
                RenderPipelines.GUI_TEXTURED,
                BLUE_BAR_BK,
                barLeft, barTop,
                WIDTH, SINGLE_BAR_HEIGHT
        );

        // 2. 绘制黄色条背景（下半部分，加间隔）
        int yellowBarTop = barTop + SINGLE_BAR_HEIGHT + BAR_GAP;
        guiGraphics.blitSprite(
                RenderPipelines.GUI_TEXTURED,
                YELLOW_BAR_BK,
                barLeft, yellowBarTop,
                WIDTH, SINGLE_BAR_HEIGHT
        );

        // 3. 绘制蓝色条的等分分割线（仅蓝色条需要等分）
        drawBlueSegmentSeparators(guiGraphics, barLeft, barTop);
    }

    @Override
    public void render(@NotNull GuiGraphics guiGraphics, @NotNull DeltaTracker deltaTracker) {
        Window window = this.minecraft.getWindow();
        int barLeft = left(window);
        int barTop = top(window);
        float blueProgress = (float) completedCount / totalCount;
        int blueProgressWidth = Mth.ceil(blueProgress * WIDTH);
        int yellowProgressWidth = Mth.ceil(remainingTimeRatio * WIDTH);

        if (blueProgressWidth > 0) {
            guiGraphics.blitSprite(
                    RenderPipelines.GUI_TEXTURED,
                    BLUE_BAR_P,
                    WIDTH, SINGLE_BAR_HEIGHT,
                    0, 0,
                    barLeft, barTop,
                    blueProgressWidth, SINGLE_BAR_HEIGHT
            );
        }
        int yellowBarTop = barTop + SINGLE_BAR_HEIGHT + BAR_GAP;
        if (yellowProgressWidth > 0) {
            guiGraphics.blitSprite(
                    RenderPipelines.GUI_TEXTURED,
                    YELLOW_BAR_P,
                    WIDTH, SINGLE_BAR_HEIGHT,
                    0, 0,
                    barLeft, yellowBarTop,
                    yellowProgressWidth, SINGLE_BAR_HEIGHT
            );
        }
    }

    private void drawBlueSegmentSeparators(GuiGraphics guiGraphics, int barLeft, int barTop) {
        if (totalCount <= 1) return;
        int segmentWidth = WIDTH / totalCount;
        for (int i = 1; i < totalCount; i++) {
            int separatorX = barLeft + (segmentWidth * i);
            guiGraphics.fill(
                    separatorX, barTop,
                    separatorX + SEGMENT_GAP, barTop + SINGLE_BAR_HEIGHT,
                    0xFFCCE5FF
            );
        }
    }
}
