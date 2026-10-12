package com.longwindy.rsunifiedterminal.client;

import com.longwindy.rsunifiedterminal.client.widget.CraftingMonitorButton;
import com.longwindy.rsunifiedterminal.client.widget.FluidViewSideButton;
import com.longwindy.rsunifiedterminal.client.widget.PageSideButton;
import com.longwindy.rsunifiedterminal.client.widget.ScalePatternButton;
import com.longwindy.rsunifiedterminal.grid.UniversalGridNode;
import com.longwindy.rsunifiedterminal.terminal.TerminalOpener;
import com.longwindy.rsunifiedterminal.terminal.TerminalPage;
import com.refinedmods.refinedstorage.api.network.grid.GridType;
import com.refinedmods.refinedstorage.screen.CraftingMonitorScreen;
import com.refinedmods.refinedstorage.screen.grid.GridScreen;
import com.refinedmods.refinedstorage.screen.widget.sidebutton.RedstoneModeSideButton;
import com.refinedmods.refinedstorage.screen.widget.sidebutton.SideButton;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Adds the terminal controls to Refined Storage's grid and crafting monitor screens when they show the universal terminal.
 * Called at the end of every {@code onPostInit} of those screens, which also runs when Refined Storage rebuilds them
 * (size change, processing pattern toggle, first crafting task started, ...), unlike Forge's screen init event.
 */
public final class TerminalScreenControls {
    // Top right corner, above the scrollbar (see 贴图/2_终端界面/合成监控按钮位置标注).
    private static final int MONITOR_BUTTON_X = 174;
    private static final int CRAFTING_MONITOR_BACK_BUTTON_X = 235;
    private static final int MONITOR_BUTTON_Y = 5;
    // Gap between the processing inputs (ending at x = 60) and outputs (starting at x = 92), first matrix row.
    private static final int PROCESSING_GAP_X = 61;
    private static final int PROCESSING_GAP_WIDTH = 31;

    private TerminalScreenControls() {
    }

    public static void addToGrid(GridScreen screen, UniversalGridNode node) {
        TerminalWidgetHost host = (TerminalWidgetHost) screen;
        TerminalClient.setOpenSlot(node.getPlayerSlot());

        TerminalPage currentPage = node.getGridType() == GridType.PATTERN ? TerminalPage.PATTERN : TerminalPage.CRAFTING;

        List<SideButton> sideButtons = screen.getSideButtons();

        // Refined Storage adds a redstone mode button to every grid node, which means nothing for an item.
        Iterator<SideButton> iterator = sideButtons.iterator();
        while (iterator.hasNext()) {
            SideButton button = iterator.next();
            if (button instanceof RedstoneModeSideButton) {
                iterator.remove();
                host.rsunifiedterminal$removeWidget(button);
            }
        }

        List<SideButton> added = new ArrayList<>();
        if (currentPage == TerminalPage.CRAFTING) {
            added.add(new FluidViewSideButton(screen, node.isFluidView()));
        }
        for (TerminalPage page : TerminalPage.values()) {
            added.add(new PageSideButton(screen, page, currentPage));
        }
        for (SideButton button : added) {
            sideButtons.add(button);
            host.rsunifiedterminal$addWidget(button);
        }

        // Lay out the column again, as BaseScreen#addSideButton would.
        int y = screen.getGuiTop() + 6;
        for (SideButton button : sideButtons) {
            button.setX(screen.getGuiLeft() - button.getWidth() - 2);
            button.setY(y);
            y += button.getHeight() + 2;
        }

        host.rsunifiedterminal$addWidget(new CraftingMonitorButton(
            screen.getGuiLeft() + MONITOR_BUTTON_X,
            screen.getGuiTop() + MONITOR_BUTTON_Y,
            Component.translatable("gui.rsunifiedterminal.crafting_monitor"),
            () -> TerminalClient.open(TerminalOpener.TARGET_CRAFTING_MONITOR)
        ));

        if (currentPage == TerminalPage.PATTERN && node.isProcessingPattern()) {
            int matrixTop = screen.getTopHeight() + screen.getVisibleRows() * 18 + 4;
            host.rsunifiedterminal$addWidget(new ScalePatternButton(
                screen.getGuiLeft() + PROCESSING_GAP_X + (PROCESSING_GAP_WIDTH - ScalePatternButton.SIZE) / 2,
                screen.getGuiTop() + matrixTop + (18 - ScalePatternButton.SIZE) / 2
            ));
        }

        TerminalClient.restoreCursor();
    }

    public static void addToCraftingMonitor(CraftingMonitorScreen screen) {
        ((TerminalWidgetHost) screen).rsunifiedterminal$addWidget(new CraftingMonitorButton(
            screen.getGuiLeft() + CRAFTING_MONITOR_BACK_BUTTON_X,
            screen.getGuiTop() + MONITOR_BUTTON_Y,
            Component.translatable("gui.rsunifiedterminal.back_to_terminal"),
            () -> TerminalClient.open(TerminalOpener.TARGET_LAST_PAGE)
        ));

        TerminalClient.restoreCursor();
    }
}
