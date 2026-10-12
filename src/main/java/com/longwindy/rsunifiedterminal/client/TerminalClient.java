package com.longwindy.rsunifiedterminal.client;

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
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import org.lwjgl.glfw.GLFW;

import javax.annotation.Nullable;

/**
 * Client side state of the open terminal: which slot it is in, and the cursor position to restore after switching pages.
 */
public final class TerminalClient {
    @Nullable
    private static PlayerSlot openSlot;
    @Nullable
    private static double[] cursorToRestore;

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
            rememberCursor();
            ModNetwork.CHANNEL.sendToServer(new OpenTerminalMessage(openSlot, target));
        }
    }

    public static void toggleFluidView() {
        if (openSlot != null) {
            rememberCursor();
            ModNetwork.CHANNEL.sendToServer(new ToggleFluidViewMessage(openSlot));
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
