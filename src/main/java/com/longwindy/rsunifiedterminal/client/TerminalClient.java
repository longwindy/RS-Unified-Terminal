package com.longwindy.rsunifiedterminal.client;

import com.longwindy.rsunifiedterminal.ClientConfig;
import com.longwindy.rsunifiedterminal.client.screen.WirelessCrafterManagerScreen;
import com.longwindy.rsunifiedterminal.grid.GridParameterSync;
import com.longwindy.rsunifiedterminal.grid.UniversalGridNode;
import com.longwindy.rsunifiedterminal.network.GridParameterMessage;
import com.longwindy.rsunifiedterminal.network.ModNetwork;
import com.longwindy.rsunifiedterminal.network.OpenTerminalMessage;
import com.longwindy.rsunifiedterminal.network.ToggleFluidViewMessage;
import com.refinedmods.refinedstorage.blockentity.data.BlockEntitySynchronizationParameter;
import com.refinedmods.refinedstorage.blockentity.grid.GridBlockEntity;
import com.refinedmods.refinedstorage.container.GridContainerMenu;
import com.refinedmods.refinedstorage.inventory.player.PlayerSlot;
import com.refinedmods.refinedstorage.screen.grid.GridScreen;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.player.LocalPlayer;
import org.lwjgl.glfw.GLFW;

import javax.annotation.Nullable;

/**
 * Client side state of the open terminal: which slot it is in, and what to carry over when switching pages
 * (cursor position and search text).
 */
public final class TerminalClient {
    @Nullable
    private static PlayerSlot openSlot;
    @Nullable
    private static double[] cursorToRestore;
    @Nullable
    private static String searchToRestore;
    private static long switchStartedAt;

    // A switch that hasn't reopened a page after this long has failed (out of range, no energy, ...).
    private static final long SWITCH_TIMEOUT_MS = 3000;

    private TerminalClient() {
    }

    /**
     * Called when one of the terminal screens is shown, so its buttons know which item they belong to.
     */
    public static void setOpenSlot(PlayerSlot slot) {
        openSlot = slot;
    }

    public static void open(int target) {
        if (openSlot != null) {
            beginSwitch();
            ModNetwork.CHANNEL.sendToServer(new OpenTerminalMessage(openSlot, target));
        }
    }

    public static void toggleFluidView() {
        if (openSlot != null) {
            beginSwitch();
            ModNetwork.CHANNEL.sendToServer(new ToggleFluidViewMessage(openSlot));
        }
    }

    private static void beginSwitch() {
        rememberCursor();
        rememberSearch();
        switchStartedAt = Util.getMillis();
    }

    private static boolean isSwitching() {
        return Util.getMillis() - switchStartedAt < SWITCH_TIMEOUT_MS;
    }

    // Refined Storage clears the grid search query whenever a grid screen closes, which includes every page switch.
    // Its rememberSearchQuery client setting (keep the query after closing a grid) is left to Refined Storage:
    // it applies to the crafting and pattern pages, which are its grid screen, but not to the crafter manager page.
    private static void rememberSearch() {
        if (!ClientConfig.KEEP_SEARCH_ON_PAGE_SWITCH.get()) {
            searchToRestore = null;
            return;
        }

        String search = getSearchText(Minecraft.getInstance().screen);
        // The crafting monitor has no search box: keep the text from the page it was opened from.
        if (search != null) {
            searchToRestore = search;
        }
    }

    @Nullable
    private static String getSearchText(@Nullable Screen screen) {
        if (screen instanceof GridScreen gridScreen) {
            return gridScreen.getSearchFieldText();
        }
        if (screen instanceof WirelessCrafterManagerScreen crafterManagerScreen) {
            return crafterManagerScreen.getSearchFieldText();
        }
        return null;
    }

    /**
     * Called when a page with a search box is shown.
     *
     * @return the search text of the page we switched from, or null if the terminal was opened normally
     */
    @Nullable
    public static String takeSearchToRestore() {
        String search = searchToRestore;
        searchToRestore = null;
        switchStartedAt = 0;
        return search;
    }

    /**
     * Called when a page without a search box (the crafting monitor) is shown: the switch is done,
     * but the search text is kept for the page we go back to.
     */
    public static void onSwitchCompleted() {
        switchStartedAt = 0;
    }

    /**
     * Called when a terminal screen closes. Unless a page switch is replacing it, the terminal itself is being closed,
     * so the search text is forgotten.
     */
    public static void onTerminalScreenClosing() {
        if (!isSwitching()) {
            searchToRestore = null;
        }
    }

    // Reopening a menu closes the screen in between, which recenters the cursor.
    private static void rememberCursor() {
        long window = Minecraft.getInstance().getWindow().getWindow();
        double[] x = new double[1];
        double[] y = new double[1];
        GLFW.glfwGetCursorPos(window, x, y);
        cursorToRestore = new double[]{x[0], y[0]};
    }

    public static void restoreCursor() {
        if (cursorToRestore != null) {
            GLFW.glfwSetCursorPos(Minecraft.getInstance().getWindow().getWindow(), cursorToRestore[0], cursorToRestore[1]);
            cursorToRestore = null;
        }
    }

    /**
     * Reroutes a block entity parameter change made by Refined Storage's grid screens to the terminal.
     *
     * @return true if the open grid is a universal terminal page, in which case Refined Storage must not send its own message
     */
    public static boolean handleParameter(BlockEntitySynchronizationParameter<?, ?> parameter, Object value) {
        LocalPlayer player = Minecraft.getInstance().player;
        if (player == null || !(player.containerMenu instanceof GridContainerMenu menu) || !(menu.getGrid() instanceof UniversalGridNode)) {
            return false;
        }

        BlockEntitySynchronizationParameter<?, GridBlockEntity> supported = GridParameterSync.byId(parameter.getId());
        if (supported != null) {
            ModNetwork.CHANNEL.sendToServer(new GridParameterMessage(supported, value));
        }

        // Unsupported parameters (like the redstone mode of a block) have no meaning for the terminal.
        return true;
    }
}
