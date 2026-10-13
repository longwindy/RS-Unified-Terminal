package com.longwindy.rsunifiedterminal.client.screen;

import com.longwindy.rsunifiedterminal.client.TerminalClient;
import com.longwindy.rsunifiedterminal.client.widget.PageSideButton;
import com.longwindy.rsunifiedterminal.craftermanager.WirelessCrafterManagerMenu;
import com.longwindy.rsunifiedterminal.craftermanager.WirelessCrafterManagerSlot;
import com.longwindy.rsunifiedterminal.network.CrafterManagerSettingsMessage;
import com.longwindy.rsunifiedterminal.network.ModNetwork;
import com.longwindy.rsunifiedterminal.terminal.TerminalData;
import com.longwindy.rsunifiedterminal.terminal.TerminalPage;
import com.mojang.blaze3d.systems.RenderSystem;
import com.refinedmods.refinedstorage.RS;
import com.refinedmods.refinedstorage.api.network.grid.IGrid;
import com.refinedmods.refinedstorage.screen.BaseScreen;
import com.refinedmods.refinedstorage.screen.IScreenInfoProvider;
import com.refinedmods.refinedstorage.screen.widget.ScrollbarWidget;
import com.refinedmods.refinedstorage.screen.widget.SearchWidget;
import com.refinedmods.refinedstorage.screen.widget.sidebutton.GridSizeSideButton;
import com.refinedmods.refinedstorage.screen.widget.sidebutton.SearchBoxModeSideButton;
import com.refinedmods.refinedstorage.util.RenderUtils;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;

import java.util.Map;

/**
 * The crafter manager page. Adapted from Refined Storage's {@code CrafterManagerScreen} (MIT):
 * the settings come from the terminal instead of the block, there is no redstone mode,
 * and the page side buttons of the terminal are added below the original ones.
 */
public class WirelessCrafterManagerScreen extends BaseScreen<WirelessCrafterManagerMenu> implements IScreenInfoProvider {
    private static final ResourceLocation TEXTURE = new ResourceLocation(RS.ID, "textures/gui/crafter_manager.png");

    private ScrollbarWidget scrollbar;
    private SearchWidget searchField;
    private int size;
    private int searchBoxMode;

    public WirelessCrafterManagerScreen(WirelessCrafterManagerMenu menu, Inventory inventory, Component title) {
        super(menu, 193, 0, inventory, title);

        this.size = TerminalData.getCrafterManagerSize(menu.getStack());
        this.searchBoxMode = TerminalData.getCrafterManagerSearchBoxMode(menu.getStack());
    }

    public static WirelessCrafterManagerScreen create(WirelessCrafterManagerMenu menu, Inventory inventory, Component title) {
        WirelessCrafterManagerScreen screen = new WirelessCrafterManagerScreen(menu, inventory, title);
        menu.setScreenInfoProvider(screen);
        return screen;
    }

    @Override
    protected void onPreInit() {
        this.imageHeight = getTopHeight() + getBottomHeight() + (getVisibleRows() * 18);
    }

    @Override
    public void onPostInit(int x, int y) {
        TerminalClient.setOpenSlot(menu.getPlayerSlot());

        addSideButton(new SearchBoxModeSideButton(this) {
            @Override
            protected int getSearchBoxMode() {
                return searchBoxMode;
            }

            @Override
            protected void setSearchBoxMode(int mode) {
                searchBoxMode = mode;
                searchField.setMode(mode);
                sendSettings();
            }
        });
        addSideButton(new GridSizeSideButton(this, () -> size, newSize -> {
            size = newSize;
            sendSettings();
            BaseScreen.executeLater(WirelessCrafterManagerScreen.class, BaseScreen::init);
        }));
        for (TerminalPage page : TerminalPage.values()) {
            addSideButton(new PageSideButton(this, page, TerminalPage.CRAFTER_MANAGER));
        }

        this.scrollbar = new ScrollbarWidget(this, 174, getTopHeight(), 12, (getVisibleRows() * 18) - 2);
        this.scrollbar.addListener((oldOffset, newOffset) -> menu.initSlots(null));

        menu.initSlots(null);

        int sx = x + 97 + 1;
        int sy = y + 6 + 1;

        if (searchField == null) {
            searchField = new SearchWidget(font, sx, sy, 88 - 6);
            searchField.setResponder(value -> {
                searchField.updateJei();

                menu.initSlots(null);
            });
            searchField.setMode(searchBoxMode);
        } else {
            searchField.setX(sx);
            searchField.setY(sy);
        }

        addRenderableWidget(searchField);
        if (searchField.isFocused()) {
            setFocused(searchField);
        }

        // Keep the search text of the page we switched from.
        String search = TerminalClient.takeSearchToRestore();
        if (search != null) {
            searchField.setValue(search);
        }

        TerminalClient.restoreCursor();
    }

    private void sendSettings() {
        ModNetwork.CHANNEL.sendToServer(new CrafterManagerSettingsMessage(size, searchBoxMode));
    }

    @Override
    public void tick(int x, int y) {
        scrollbar.setEnabled((getRows() - 1) >= getVisibleRows());
        scrollbar.setMaxOffset(getRows() - getVisibleRows());
    }

    @Override
    public void renderBackground(GuiGraphics graphics, int x, int y, int mouseX, int mouseY) {
        graphics.blit(TEXTURE, x, y, 0, 0, imageWidth, getTopHeight());

        int rows = getVisibleRows();

        int yy = y;

        for (int i = 0; i < rows; ++i) {
            yy += 18;

            int yTextureStart = getTopHeight();
            if (i > 0) {
                if (i == rows - 1) {
                    yTextureStart += 18 * 2;
                } else {
                    yTextureStart += 18;
                }
            }

            graphics.blit(TEXTURE, x, yy, 0, yTextureStart, imageWidth, 18);
        }

        yy += 18;

        graphics.blit(TEXTURE, x, yy, 0, getTopHeight() + (18 * 3), imageWidth, getBottomHeight());

        for (Slot slot : menu.slots) {
            if (slot instanceof WirelessCrafterManagerSlot && slot.isActive()) {
                graphics.blit(TEXTURE, x + slot.x - 1, y + slot.y - 1, 0, 193, 18, 18);
            }
        }

        searchField.render(graphics, 0, 0, 0);

        scrollbar.render(graphics);
    }

    @Override
    public void renderForeground(GuiGraphics graphics, int mouseX, int mouseY) {
        renderString(graphics, 7, 7, title.getString());
        renderString(graphics, 7, getYPlayerInventory() - 12, I18n.get("container.inventory"));

        for (Map.Entry<String, Integer> heading : menu.getHeadings().entrySet()) {
            int y = heading.getValue();

            if (y >= getTopHeight() - 1 && y < getTopHeight() + getVisibleRows() * 18 - 1) {
                RenderSystem.setShaderColor(1, 1, 1, 1);

                graphics.blit(TEXTURE, 7, y, 0, 174, 18 * 9, 18);
                renderString(graphics, 7 + 4, y + 6, RenderUtils.shorten(I18n.get(heading.getKey()), 25));
            }
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int clickedButton) {
        if (scrollbar.mouseClicked(mouseX, mouseY, clickedButton)) {
            return true;
        }

        return super.mouseClicked(mouseX, mouseY, clickedButton);
    }

    @Override
    public boolean charTyped(char character, int modifiers) {
        if (searchField.charTyped(character, modifiers)) {
            return true;
        }

        return super.charTyped(character, modifiers);
    }

    @Override
    public boolean keyPressed(int key, int scanCode, int modifiers) {
        if (searchField.keyPressed(key, scanCode, modifiers)) {
            return true;
        }

        return super.keyPressed(key, scanCode, modifiers);
    }

    @Override
    public void mouseMoved(double mx, double my) {
        scrollbar.mouseMoved(mx, my);

        super.mouseMoved(mx, my);
    }

    @Override
    public boolean mouseReleased(double mx, double my, int button) {
        return scrollbar.mouseReleased(mx, my, button) || super.mouseReleased(mx, my, button);
    }

    @Override
    public boolean mouseScrolled(double x, double y, double delta) {
        return this.scrollbar.mouseScrolled(x, y, delta) || super.mouseScrolled(x, y, delta);
    }

    @Override
    public int getTopHeight() {
        return 19;
    }

    @Override
    public int getBottomHeight() {
        return 99;
    }

    @Override
    public int getVisibleRows() {
        switch (size) {
            case IGrid.SIZE_STRETCH:
                int screenSpaceAvailable = height - getTopHeight() - getBottomHeight();

                return Math.max(3, Math.min((screenSpaceAvailable / 18) - 3, RS.CLIENT_CONFIG.getCrafterManager().getMaxRowsStretch()));
            case IGrid.SIZE_SMALL:
                return 3;
            case IGrid.SIZE_MEDIUM:
                return 5;
            case IGrid.SIZE_LARGE:
                return 8;
            default:
                return 3;
        }
    }

    @Override
    public int getRows() {
        return menu.getRows();
    }

    @Override
    public int getCurrentOffset() {
        return scrollbar == null ? 0 : scrollbar.getOffset();
    }

    @Override
    public String getSearchFieldText() {
        return searchField == null ? "" : searchField.getValue();
    }

    @Override
    public int getYPlayerInventory() {
        return getTopHeight() + (getVisibleRows() * 18) + 16;
    }
}
