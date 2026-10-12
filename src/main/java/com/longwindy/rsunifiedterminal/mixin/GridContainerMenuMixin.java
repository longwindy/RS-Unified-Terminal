package com.longwindy.rsunifiedterminal.mixin;

import com.longwindy.rsunifiedterminal.grid.UniversalGridNode;
import com.refinedmods.refinedstorage.api.network.grid.GridType;
import com.refinedmods.refinedstorage.api.network.grid.IGrid;
import com.refinedmods.refinedstorage.container.GridContainerMenu;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/**
 * Shift-clicking an item from the player inventory into the grid: in fluid view, use the fluid grid behavior
 * (fluid containers are emptied into the network) instead of inserting the container itself.
 */
@Mixin(value = GridContainerMenu.class, remap = false)
public abstract class GridContainerMenuMixin {
    // The shift-click handler set up in GridContainerMenu#initSlots.
    @Redirect(
        method = "lambda$initSlots$0",
        at = @At(
            value = "INVOKE",
            target = "Lcom/refinedmods/refinedstorage/api/network/grid/IGrid;getGridType()Lcom/refinedmods/refinedstorage/api/network/grid/GridType;"
        )
    )
    private GridType rsunifiedterminal$fluidViewGridType(IGrid grid) {
        if (grid instanceof UniversalGridNode node && node.isFluidView()) {
            return GridType.FLUID;
        }
        return grid.getGridType();
    }
}
