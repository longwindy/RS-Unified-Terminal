package com.longwindy.rsunifiedterminal.mixin.client;

import com.longwindy.rsunifiedterminal.client.TerminalScreenControls;
import com.longwindy.rsunifiedterminal.client.TerminalWidgetHost;
import com.longwindy.rsunifiedterminal.grid.UniversalGridNode;
import com.refinedmods.refinedstorage.api.network.grid.GridType;
import com.refinedmods.refinedstorage.api.network.grid.IGrid;
import com.refinedmods.refinedstorage.container.GridContainerMenu;
import com.refinedmods.refinedstorage.screen.BaseScreen;
import com.refinedmods.refinedstorage.screen.grid.GridScreen;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

// No lambdas or method references in here: they would reference this mixin class, which can't be loaded at runtime.
@Mixin(GridScreen.class)
public abstract class GridScreenMixin extends BaseScreen<GridContainerMenu> implements TerminalWidgetHost {
    private GridScreenMixin(GridContainerMenu menu, int width, int height, Inventory inventory, Component title) {
        super(menu, width, height, inventory, title);
    }

    /**
     * Adds the terminal controls every time the screen is (re)built. Refined Storage rebuilds it by calling
     * {@code BaseScreen#init()} directly (size change, processing pattern toggle, ...), which fires no Forge screen event.
     */
    @Inject(method = "onPostInit", at = @At("TAIL"), remap = false)
    private void rsunifiedterminal$addTerminalControls(int x, int y, CallbackInfo ci) {
        GridScreen screen = (GridScreen) (Object) this;
        if (screen.getGrid() instanceof UniversalGridNode node) {
            TerminalScreenControls.addToGrid(screen, node);
        }
    }

    @Override
    public void rsunifiedterminal$addWidget(AbstractWidget widget) {
        addRenderableWidget(widget);
    }

    @Override
    public void rsunifiedterminal$removeWidget(GuiEventListener widget) {
        removeWidget(widget);
    }

    /**
     * In fluid view the crafting page keeps its crafting matrix (so its grid type stays CRAFTING),
     * but clicking and scrolling in the grid must use the fluid messages, which Refined Storage only does for fluid grids.
     */
    @Redirect(
        method = {"mouseClicked", "mouseScrolled"},
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
