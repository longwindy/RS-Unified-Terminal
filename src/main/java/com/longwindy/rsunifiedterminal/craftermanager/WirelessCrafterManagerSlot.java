package com.longwindy.rsunifiedterminal.craftermanager;

import com.refinedmods.refinedstorage.container.slot.BaseSlot;
import com.refinedmods.refinedstorage.screen.IScreenInfoProvider;
import net.minecraftforge.items.IItemHandler;

/**
 * Adapted from Refined Storage's {@code CrafterManagerSlot} (MIT): only shown while scrolled into the visible area.
 */
public class WirelessCrafterManagerSlot extends BaseSlot {
    private final boolean visible;
    private final IScreenInfoProvider display;

    public WirelessCrafterManagerSlot(IItemHandler itemHandler, int inventoryIndex, int x, int y, boolean visible, IScreenInfoProvider display) {
        super(itemHandler, inventoryIndex, x, y);

        this.visible = visible;
        this.display = display;
    }

    @Override
    public boolean isActive() {
        return y >= display.getTopHeight() && y < display.getTopHeight() + 18 * display.getVisibleRows() && visible;
    }
}
