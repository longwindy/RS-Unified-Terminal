package com.longwindy.rsunifiedterminal.client.widget;

import com.longwindy.rsunifiedterminal.client.ModTextures;
import com.longwindy.rsunifiedterminal.client.TerminalClient;
import com.refinedmods.refinedstorage.screen.BaseScreen;
import com.refinedmods.refinedstorage.screen.widget.sidebutton.SideButton;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.resources.language.I18n;

/**
 * Switches the crafting page between showing the items and the fluids of the network.
 */
public class FluidViewSideButton extends SideButton {
    private final boolean fluidView;

    public FluidViewSideButton(BaseScreen<?> screen, boolean fluidView) {
        super(screen);
        this.fluidView = fluidView;
    }

    @Override
    protected void renderButtonIcon(GuiGraphics graphics, int x, int y) {
        graphics.blit(fluidView ? ModTextures.SIDE_BUTTON_VIEW_FLUIDS : ModTextures.SIDE_BUTTON_VIEW_ITEMS, x, y, 0, 0, 16, 16, 16, 16);
    }

    @Override
    protected String getSideButtonTooltip() {
        return I18n.get("sidebutton.rsunifiedterminal.view") + "\n" + ChatFormatting.GRAY
            + I18n.get(fluidView ? "sidebutton.rsunifiedterminal.view.fluids" : "sidebutton.rsunifiedterminal.view.items");
    }

    @Override
    public void onPress() {
        TerminalClient.toggleFluidView();
    }
}
