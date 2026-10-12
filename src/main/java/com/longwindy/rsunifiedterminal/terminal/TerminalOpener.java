package com.longwindy.rsunifiedterminal.terminal;

import com.longwindy.rsunifiedterminal.item.UniversalTerminalItem;
import com.refinedmods.refinedstorage.inventory.player.PlayerSlot;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Opens a page of the terminal from an already open terminal screen.
 * <p>
 * Opening goes through Refined Storage's network item manager like any other open (range and permission checks,
 * tracking of the open network item), but no opening energy is charged since the terminal was already open.
 */
public final class TerminalOpener {
    /** Opens the crafting monitor. */
    public static final int TARGET_CRAFTING_MONITOR = 100;
    /** Opens the last used page, used when the terminal is opened normally and when returning from the crafting monitor. */
    public static final int TARGET_LAST_PAGE = 101;

    private static final Map<UUID, Integer> PENDING = new HashMap<>();

    private TerminalOpener() {
    }

    public static void open(ServerPlayer player, PlayerSlot slot, int target) {
        ItemStack stack = slot.getStackFromSlot(player);
        if (!(stack.getItem() instanceof UniversalTerminalItem item)) {
            return;
        }

        if (target >= 0 && target < TerminalPage.values().length) {
            TerminalData.setPage(stack, TerminalPage.byId(target));
        }

        PENDING.put(player.getUUID(), target);
        try {
            item.applyNetwork(player.server, stack, network -> network.getNetworkItemManager().open(player, stack, slot), player::sendSystemMessage);
        } finally {
            PENDING.remove(player.getUUID());
        }
    }

    /**
     * @return the target requested through {@link #open}, or null when the terminal is opened normally
     */
    @Nullable
    static Integer getPendingTarget(Player player) {
        return PENDING.get(player.getUUID());
    }
}
