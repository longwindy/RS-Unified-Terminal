package com.longwindy.rsunifiedterminal.client.widget;

import com.longwindy.rsunifiedterminal.client.ModTextures;
import com.longwindy.rsunifiedterminal.client.TerminalClient;
import com.longwindy.rsunifiedterminal.terminal.TerminalPage;
import com.refinedmods.refinedstorage.screen.BaseScreen;
import com.refinedmods.refinedstorage.screen.widget.sidebutton.SideButton;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.resources.language.I18n;

/**
 * Switches the terminal to one of its pages.
 */
public class PageSideButton extends SideButton {
    private final TerminalPage page;
    private final boolean current;

    public PageSideButton(BaseScreen<?> screen, TerminalPage page, TerminalPage currentPage) {
        super(screen);
        this.page = page;
        this.current = page == currentPage;
    }

    @Override
    protected void renderButtonIcon(GuiGraphics graphics, int x, int y) {
        if (current && ModTextures.exists(ModTextures.selectedPage(page))) {
            graphics.blit(ModTextures.selectedPage(page), x, y, 0, 0, 16, 16, 16, 16);
            return;
        }

        graphics.blit(ModTextures.page(page), x, y, 0, 0, 16, 16, 16, 16);

        if (current) {
            graphics.fill(x, y, x + 16, y + 16, 0x5000A0FF);
        }
    }

    @Override
    protected String getSideButtonTooltip() {
        String tooltip = I18n.get("sidebutton.rsunifiedterminal.page." + page.name().toLowerCase());
        if (current) {
            tooltip += "\n" + ChatFormatting.GRAY + I18n.get("sidebutton.rsunifiedterminal.page.current");
        }
        return tooltip;
    }

    @Override
    public void onPress() {
        if (!current) {
            TerminalClient.open(page.getId());
        }
    }
}
