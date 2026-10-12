package com.longwindy.rsunifiedterminal.compat;

import com.longwindy.rsunifiedterminal.grid.UniversalGridNode;
import com.longwindy.rsunifiedterminal.mixin.GridNetworkNodeAccessor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraftforge.fml.ModList;

/**
 * Refined Polymorphism stores the selected recipe on exact crafting patterns created in a pattern grid block,
 * so autocrafting uses that recipe. This does the same for the pattern page of the terminal.
 * <p>
 * The recipe selection itself already works: Refined Polymorphism adds it to every grid node, including ours.
 */
public final class PolymorphCompat {
    private static final String NBT_POLYMORPH_RECIPE = "polymorphRecipe";

    private PolymorphCompat() {
    }

    public static void onPatternCreated(UniversalGridNode node) {
        if (!ModList.get().isLoaded("refinedpolymorph") || node.isProcessingPattern() || !node.isExactPattern()) {
            return;
        }

        CraftingRecipe recipe = ((GridNetworkNodeAccessor) node).rsunifiedterminal$getCurrentRecipe();
        ItemStack pattern = node.getPatterns().getStackInSlot(1);

        if (recipe != null && !pattern.isEmpty()) {
            pattern.getOrCreateTag().putString(NBT_POLYMORPH_RECIPE, recipe.getId().toString());
        }
    }
}
