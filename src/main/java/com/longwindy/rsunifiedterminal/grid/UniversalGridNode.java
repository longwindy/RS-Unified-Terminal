package com.longwindy.rsunifiedterminal.grid;

import com.longwindy.rsunifiedterminal.Config;
import com.longwindy.rsunifiedterminal.compat.PolymorphCompat;
import com.longwindy.rsunifiedterminal.network.GridSettingsMessage;
import com.longwindy.rsunifiedterminal.network.ModNetwork;
import com.longwindy.rsunifiedterminal.terminal.TerminalData;
import com.refinedmods.refinedstorage.api.network.INetwork;
import com.refinedmods.refinedstorage.api.network.grid.GridType;
import com.refinedmods.refinedstorage.api.network.grid.IGrid;
import com.refinedmods.refinedstorage.api.network.grid.handler.IFluidGridHandler;
import com.refinedmods.refinedstorage.api.network.grid.handler.IItemGridHandler;
import com.refinedmods.refinedstorage.api.storage.cache.IStorageCache;
import com.refinedmods.refinedstorage.api.storage.cache.IStorageCacheListener;
import com.refinedmods.refinedstorage.api.util.IStackList;
import com.refinedmods.refinedstorage.apiimpl.network.node.GridNetworkNode;
import com.refinedmods.refinedstorage.apiimpl.storage.cache.listener.FluidGridStorageCacheListener;
import com.refinedmods.refinedstorage.apiimpl.storage.cache.listener.ItemGridStorageCacheListener;
import com.refinedmods.refinedstorage.inventory.player.PlayerSlot;
import com.refinedmods.refinedstorage.item.NetworkItem;
import com.refinedmods.refinedstorage.item.WirelessGridItem;
import com.refinedmods.refinedstorage.screen.BaseScreen;
import com.refinedmods.refinedstorage.screen.grid.GridScreen;
import com.refinedmods.refinedstorage.util.NetworkUtils;
import com.refinedmods.refinedstorage.util.StackUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.TickTask;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import javax.annotation.Nullable;

/**
 * A crafting or pattern grid that lives on the Wireless Universal Terminal item instead of in the world.
 * <p>
 * Extending {@link GridNetworkNode} lets Refined Storage's own grid menu, screen, pattern creation, alternatives screen,
 * processing pattern transfer and JEI integration work unchanged, since they all cast the grid to this class.
 * Everything that normally depends on the block is redirected to the item:
 * <ul>
 *     <li>the network is resolved from the network position bound to the item, like {@code WirelessGrid};</li>
 *     <li>the node state is saved to the item instead of the level's network node data;</li>
 *     <li>grid settings use local values instead of the {@code GridBlockEntity} synchronization parameters,
 *     and pattern parameters are synchronized through {@link GridParameterSync}.</li>
 * </ul>
 * The node is never added to a network graph, so it never ticks.
 */
public class UniversalGridNode extends GridNetworkNode {
    private static final String NBT_FILTER_INVENTORY = "Inventory_2";

    private final Player player;
    private final ItemStack stack;
    private final PlayerSlot slot;
    private final boolean fluidView;
    private final GridParameterSync parameterSync = new GridParameterSync();

    private boolean loading;
    private boolean saveQueued;

    // Local grid settings: GridNetworkNode reads these from the GridBlockEntity parameters on the client.
    private int viewType;
    private int sortingDirection;
    private int sortingType;
    private int searchBoxMode;
    private int size;
    private int tabSelected;
    private int tabPage;

    public UniversalGridNode(Player player, ItemStack stack, PlayerSlot slot, GridType type) {
        // The position is only used for block entity lookups that expect a GridBlockEntity; the player's position is always loaded.
        super(player.level(), player.blockPosition(), type);

        this.player = player;
        this.stack = stack;
        this.slot = slot;
        this.fluidView = type == GridType.CRAFTING && TerminalData.isFluidView(stack);

        load();

        // Sent before the grid menu is opened, so the client screen is initialized with the right pattern settings.
        if (type == GridType.PATTERN && player instanceof ServerPlayer serverPlayer) {
            parameterSync.send(serverPlayer, this);
        }
    }

    public ItemStack getStack() {
        return stack;
    }

    public PlayerSlot getPlayerSlot() {
        return slot;
    }

    public boolean isFluidView() {
        return fluidView;
    }

    private String getPageKey() {
        return getGridType() == GridType.PATTERN ? TerminalData.NBT_PATTERN_PAGE : TerminalData.NBT_CRAFTING_PAGE;
    }

    private void load() {
        loading = true;
        try {
            CompoundTag root = stack.getTag();
            if (root != null) {
                read(root.getCompound(getPageKey()));
                StackUtils.readItems(getFilter(), 2, root.getCompound(TerminalData.NBT_FILTERS));
            }

            // The settings shared by all pages use the same keys as Refined Storage's wireless grids.
            setViewType(WirelessGridItem.getViewType(stack));
            setSortingDirection(WirelessGridItem.getSortingDirection(stack));
            setSortingType(WirelessGridItem.getSortingType(stack));
            setSearchBoxMode(WirelessGridItem.getSearchBoxMode(stack));
            setSize(WirelessGridItem.getSize(stack));
            setTabSelected(WirelessGridItem.getTabSelected(stack));
            setTabPage(WirelessGridItem.getTabPage(stack));
        } finally {
            loading = false;
        }
    }

    @Override
    public void markDirty() {
        if (loading || level.isClientSide) {
            return;
        }

        // Many changes can happen in a single tick (e.g. clearing a matrix), so save once at the end of it.
        if (!saveQueued) {
            saveQueued = true;
            MinecraftServer server = level.getServer();
            server.tell(new TickTask(server.getTickCount(), this::save));
        }
    }

    private void save() {
        saveQueued = false;

        CompoundTag page = new CompoundTag();
        write(page);
        page.remove(NBT_FILTER_INVENTORY);

        CompoundTag filters = new CompoundTag();
        StackUtils.writeItems(getFilter(), 2, filters);

        CompoundTag root = stack.getOrCreateTag();
        root.put(getPageKey(), page);
        root.put(TerminalData.NBT_FILTERS, filters);
        root.putInt(NBT_VIEW_TYPE, viewType);
        root.putInt(NBT_SORTING_DIRECTION, sortingDirection);
        root.putInt(NBT_SORTING_TYPE, sortingType);
        root.putInt(NBT_SEARCH_BOX_MODE, searchBoxMode);
        root.putInt(NBT_SIZE, size);
        root.putInt(NBT_TAB_SELECTED, tabSelected);
        root.putInt(NBT_TAB_PAGE, tabPage);

        if (getGridType() == GridType.PATTERN && player instanceof ServerPlayer serverPlayer) {
            parameterSync.send(serverPlayer, this);
        }
    }

    // region Network

    @Nullable
    @Override
    public INetwork getNetwork() {
        network = resolveNetwork();
        return network;
    }

    @Nullable
    private INetwork resolveNetwork() {
        if (level.isClientSide || !NetworkItem.isValid(stack)) {
            return null;
        }

        ResourceKey<Level> dimension = NetworkItem.getDimension(stack);
        Level nodeLevel = dimension == null ? null : level.getServer().getLevel(dimension);
        if (nodeLevel == null) {
            return null;
        }

        BlockPos nodePos = new BlockPos(NetworkItem.getX(stack), NetworkItem.getY(stack), NetworkItem.getZ(stack));
        if (!nodeLevel.isLoaded(nodePos)) {
            return null;
        }

        return NetworkUtils.getNetworkFromNode(NetworkUtils.getNodeFromBlockEntity(nodeLevel.getBlockEntity(nodePos)));
    }

    @Override
    public IStorageCacheListener createListener(ServerPlayer listeningPlayer) {
        INetwork currentNetwork = getNetwork();
        return fluidView
            ? new FluidGridStorageCacheListener(listeningPlayer, currentNetwork)
            : new ItemGridStorageCacheListener(listeningPlayer, currentNetwork);
    }

    @Nullable
    @Override
    public IStorageCache getStorageCache() {
        INetwork currentNetwork = getNetwork();
        if (currentNetwork == null) {
            return null;
        }
        return fluidView ? currentNetwork.getFluidStorageCache() : currentNetwork.getItemStorageCache();
    }

    @Nullable
    @Override
    public IItemGridHandler getItemHandler() {
        getNetwork();
        return super.getItemHandler();
    }

    @Nullable
    @Override
    public IFluidGridHandler getFluidHandler() {
        getNetwork();
        return super.getFluidHandler();
    }

    @Override
    public boolean isGridActive() {
        return true;
    }

    @Override
    public int getSlotId() {
        return slot.getSlotIdInPlayerInventory();
    }

    @Override
    public void onClosed(Player closingPlayer) {
        if (saveQueued) {
            save();
        }

        INetwork currentNetwork = getNetwork();
        if (currentNetwork != null) {
            currentNetwork.getNetworkItemManager().close(closingPlayer);
        }
    }

    private void drainEnergy(Player drainingPlayer, int energy) {
        INetwork currentNetwork = getNetwork();
        if (currentNetwork != null) {
            currentNetwork.getNetworkItemManager().drainEnergy(drainingPlayer, energy);
        }
    }

    // endregion

    // region Crafting and patterns

    @Override
    public void onCrafted(Player craftingPlayer, @Nullable IStackList<ItemStack> availableItems, @Nullable IStackList<ItemStack> usedItems) {
        getNetwork();
        super.onCrafted(craftingPlayer, availableItems, usedItems);
        drainEnergy(craftingPlayer, Config.CRAFT_USAGE.get());
    }

    @Override
    public void onCraftedShift(Player craftingPlayer) {
        getNetwork();
        super.onCraftedShift(craftingPlayer);
    }

    @Override
    public void onRecipeTransfer(Player transferringPlayer, ItemStack[][] recipe) {
        getNetwork();
        super.onRecipeTransfer(transferringPlayer, recipe);
    }

    @Override
    public void onClear(Player clearingPlayer) {
        getNetwork();
        super.onClear(clearingPlayer);
        if (getGridType() == GridType.CRAFTING) {
            drainEnergy(clearingPlayer, Config.CLEAR_USAGE.get());
        }
    }

    @Override
    public void onCreatePattern() {
        super.onCreatePattern();
        PolymorphCompat.onPatternCreated(this);
    }

    // endregion

    // region Grid settings

    @Override
    public int getViewType() {
        return viewType;
    }

    @Override
    public void setViewType(int viewType) {
        this.viewType = viewType;
        super.setViewType(viewType);
    }

    @Override
    public int getSortingDirection() {
        return sortingDirection;
    }

    @Override
    public void setSortingDirection(int sortingDirection) {
        this.sortingDirection = sortingDirection;
        super.setSortingDirection(sortingDirection);
    }

    @Override
    public int getSortingType() {
        return sortingType;
    }

    @Override
    public void setSortingType(int sortingType) {
        this.sortingType = sortingType;
        super.setSortingType(sortingType);
    }

    @Override
    public int getSearchBoxMode() {
        return searchBoxMode;
    }

    @Override
    public void setSearchBoxMode(int searchBoxMode) {
        this.searchBoxMode = searchBoxMode;
        super.setSearchBoxMode(searchBoxMode);
    }

    @Override
    public int getSize() {
        return size;
    }

    @Override
    public void setSize(int size) {
        this.size = size;
        super.setSize(size);
    }

    @Override
    public int getTabSelected() {
        return tabSelected;
    }

    @Override
    public void setTabSelected(int tabSelected) {
        this.tabSelected = tabSelected;
        super.setTabSelected(tabSelected);
    }

    @Override
    public int getTabPage() {
        return Math.min(tabPage, getTotalTabPages());
    }

    @Override
    public void setTabPage(int tabPage) {
        this.tabPage = tabPage;
        super.setTabPage(tabPage);
    }

    /**
     * Applies settings sent by the client, see {@link GridSettingsMessage}.
     */
    public void applySettings(GridSettingsMessage message) {
        if (IGrid.isValidViewType(message.viewType())) {
            setViewType(message.viewType());
        }
        if (IGrid.isValidSortingDirection(message.sortingDirection())) {
            setSortingDirection(message.sortingDirection());
        }
        if (IGrid.isValidSortingType(message.sortingType())) {
            setSortingType(message.sortingType());
        }
        if (IGrid.isValidSearchBoxMode(message.searchBoxMode())) {
            setSearchBoxMode(message.searchBoxMode());
        }
        if (IGrid.isValidSize(message.size())) {
            setSize(message.size());
        }
        setTabSelected(message.tabSelected());
        if (message.tabPage() >= 0 && message.tabPage() <= getTotalTabPages()) {
            setTabPage(message.tabPage());
        }
        markDirty();
    }

    private void sendSettings() {
        ModNetwork.CHANNEL.sendToServer(new GridSettingsMessage(viewType, sortingDirection, sortingType, searchBoxMode, size, tabSelected, tabPage));
    }

    // The callbacks below are called on the client by Refined Storage's side buttons and tabs.

    @Override
    public void onViewTypeChanged(int type) {
        setViewType(type);
        sendSettings();
        BaseScreen.executeLater(GridScreen.class, grid -> grid.getView().sort());
    }

    @Override
    public void onSortingTypeChanged(int type) {
        setSortingType(type);
        sendSettings();
        BaseScreen.executeLater(GridScreen.class, grid -> grid.getView().sort());
    }

    @Override
    public void onSortingDirectionChanged(int direction) {
        setSortingDirection(direction);
        sendSettings();
        BaseScreen.executeLater(GridScreen.class, grid -> grid.getView().sort());
    }

    @Override
    public void onSearchBoxModeChanged(int mode) {
        setSearchBoxMode(mode);
        sendSettings();
    }

    @Override
    public void onSizeChanged(int newSize) {
        setSize(newSize);
        sendSettings();
        BaseScreen.executeLater(GridScreen.class, BaseScreen::init);
    }

    @Override
    public void onTabSelectionChanged(int tab) {
        setTabSelected(tab == tabSelected ? -1 : tab);
        sendSettings();
        BaseScreen.executeLater(GridScreen.class, grid -> grid.getView().sort());
    }

    @Override
    public void onTabPageChanged(int page) {
        if (page >= 0 && page <= getTotalTabPages()) {
            setTabPage(page);
            sendSettings();
        }
    }

    // endregion
}
