package com.longwindy.rsunifiedterminal.client;

import com.longwindy.rsunifiedterminal.client.screen.WirelessCrafterManagerScreen;
import com.longwindy.rsunifiedterminal.grid.UniversalGridNode;
import com.longwindy.rsunifiedterminal.item.UniversalTerminalItem;
import com.refinedmods.refinedstorage.blockentity.craftingmonitor.WirelessCraftingMonitor;
import com.refinedmods.refinedstorage.screen.CraftingMonitorScreen;
import com.refinedmods.refinedstorage.screen.grid.GridScreen;
import net.minecraft.client.gui.screens.Screen;

public final class TerminalScreens {
    private TerminalScreens() {
    }

    /**
     * @return true if the screen shows one of the pages of the universal terminal, or its crafting monitor
     */
    public static boolean isTerminalScreen(Screen screen) {
        if (screen instanceof GridScreen gridScreen) {
            return gridScreen.getGrid() instanceof UniversalGridNode;
        }
        if (screen instanceof WirelessCrafterManagerScreen) {
            return true;
        }
        return isTerminalCraftingMonitor(screen);
    }

    public static boolean isTerminalCraftingMonitor(Screen screen) {
        return screen instanceof CraftingMonitorScreen craftingMonitorScreen
            && craftingMonitorScreen.getMenu().getCraftingMonitor() instanceof WirelessCraftingMonitor craftingMonitor
            && craftingMonitor.getStack().getItem() instanceof UniversalTerminalItem;
    }
}
