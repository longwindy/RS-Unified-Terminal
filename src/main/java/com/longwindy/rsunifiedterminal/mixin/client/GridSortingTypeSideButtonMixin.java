package com.longwindy.rsunifiedterminal.mixin.client;

import com.longwindy.rsunifiedterminal.grid.UniversalGridNode;
import com.refinedmods.refinedstorage.api.network.grid.GridType;
import com.refinedmods.refinedstorage.api.network.grid.IGrid;
import com.refinedmods.refinedstorage.screen.widget.sidebutton.GridSortingTypeSideButton;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/**
 * In fluid view, cycle through the sorting types of a fluid grid (no ID or Inventory Tweaks sorting).
 */
@Mixin(GridSortingTypeSideButton.class)
public abstract class GridSortingTypeSideButtonMixin {
    @Redirect(
        method = "onPress",
        at = @At(
            value = "INVOKE",
            target = "Lcom/refinedmods/refinedstorage/api/network/grid/IGrid;getGridType()Lcom/refinedmods/refinedstorage/api/network/grid/GridType;",
            remap = false
        )
    )
    private GridType rsunifiedterminal$fluidViewGridType(IGrid grid) {
        if (grid instanceof UniversalGridNode node && node.isFluidView()) {
            return GridType.FLUID;
        }
        return grid.getGridType();
    }
}
