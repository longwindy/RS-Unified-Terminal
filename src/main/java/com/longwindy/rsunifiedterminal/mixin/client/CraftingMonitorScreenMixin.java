package com.longwindy.rsunifiedterminal.mixin.client;

import com.longwindy.rsunifiedterminal.client.TerminalScreenControls;
import com.longwindy.rsunifiedterminal.client.TerminalScreens;
import com.longwindy.rsunifiedterminal.client.TerminalWidgetHost;
import com.refinedmods.refinedstorage.container.CraftingMonitorContainerMenu;
import com.refinedmods.refinedstorage.screen.BaseScreen;
import com.refinedmods.refinedstorage.screen.CraftingMonitorScreen;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

// No lambdas or method references in here: they would reference this mixin class, which can't be loaded at runtime.
@Mixin(CraftingMonitorScreen.class)
public abstract class CraftingMonitorScreenMixin extends BaseScreen<CraftingMonitorContainerMenu> implements TerminalWidgetHost {
    private CraftingMonitorScreenMixin(CraftingMonitorContainerMenu menu, int width, int height, Inventory inventory, Component title) {
        super(menu, width, height, inventory, title);
    }

    /**
     * Adds the "back to terminal" button every time the screen is (re)built. The task tabs rebuild the screen by calling
     * {@code BaseScreen#init()} directly when the first task starts or the last one ends, which fires no Forge screen event.
     */
    @Inject(method = "onPostInit", at = @At("TAIL"), remap = false)
    private void rsunifiedterminal$addTerminalControls(int x, int y, CallbackInfo ci) {
        CraftingMonitorScreen screen = (CraftingMonitorScreen) (Object) this;
        if (TerminalScreens.isTerminalCraftingMonitor(screen)) {
            TerminalScreenControls.addToCraftingMonitor(screen);
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
}
