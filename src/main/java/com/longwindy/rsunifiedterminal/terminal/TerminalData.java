package com.longwindy.rsunifiedterminal.terminal;

import com.refinedmods.refinedstorage.api.network.grid.IGrid;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;

/**
 * Terminal state stored on the item stack, next to the keys used by Refined Storage itself
 * (network position, energy, and the grid settings shared by all grid pages).
 */
public final class TerminalData {
    /** Saved grid node of the crafting page. */
    public static final String NBT_CRAFTING_PAGE = "UnifiedCraftingPage";
    /** Saved grid node of the pattern page, including its blank pattern input and pattern output slots. */
    public static final String NBT_PATTERN_PAGE = "UnifiedPatternPage";
    /** Filter slots, shared by both grid pages so the filter items exist only once. */
    public static final String NBT_FILTERS = "UnifiedGridFilters";

    private static final String NBT_PAGE = "UnifiedPage";
    private static final String NBT_FLUID_VIEW = "UnifiedFluidView";
    private static final String NBT_CRAFTER_MANAGER_SIZE = "UnifiedCrafterManagerSize";
    private static final String NBT_CRAFTER_MANAGER_SEARCH_BOX_MODE = "UnifiedCrafterManagerSearchBoxMode";

    private TerminalData() {
    }

    public static TerminalPage getPage(ItemStack stack) {
        return TerminalPage.byId(stack.hasTag() ? stack.getTag().getInt(NBT_PAGE) : 0);
    }

    public static void setPage(ItemStack stack, TerminalPage page) {
        stack.getOrCreateTag().putInt(NBT_PAGE, page.getId());
    }

    public static boolean isFluidView(ItemStack stack) {
        return stack.hasTag() && stack.getTag().getBoolean(NBT_FLUID_VIEW);
    }

    public static void setFluidView(ItemStack stack, boolean fluidView) {
        stack.getOrCreateTag().putBoolean(NBT_FLUID_VIEW, fluidView);
    }

    public static int getCrafterManagerSize(ItemStack stack) {
        CompoundTag tag = stack.getTag();
        return tag != null && tag.contains(NBT_CRAFTER_MANAGER_SIZE) ? tag.getInt(NBT_CRAFTER_MANAGER_SIZE) : IGrid.SIZE_STRETCH;
    }

    public static int getCrafterManagerSearchBoxMode(ItemStack stack) {
        CompoundTag tag = stack.getTag();
        return tag != null && tag.contains(NBT_CRAFTER_MANAGER_SEARCH_BOX_MODE) ? tag.getInt(NBT_CRAFTER_MANAGER_SEARCH_BOX_MODE) : IGrid.SEARCH_BOX_MODE_NORMAL;
    }

    public static void setCrafterManagerSettings(ItemStack stack, int size, int searchBoxMode) {
        CompoundTag tag = stack.getOrCreateTag();
        if (IGrid.isValidSize(size)) {
            tag.putInt(NBT_CRAFTER_MANAGER_SIZE, size);
        }
        if (IGrid.isValidSearchBoxMode(searchBoxMode)) {
            tag.putInt(NBT_CRAFTER_MANAGER_SEARCH_BOX_MODE, searchBoxMode);
        }
    }
}
