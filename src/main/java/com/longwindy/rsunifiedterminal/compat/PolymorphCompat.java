package com.longwindy.rsunifiedterminal.compat;

import com.illusivesoulworks.polymorph.common.crafting.RecipeSelection;
import com.longwindy.rsunifiedterminal.grid.UniversalGridNode;
import com.longwindy.rsunifiedterminal.mixin.GridNetworkNodeAccessor;
import com.refinedmods.refinedstorage.container.GridContainerMenu;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraftforge.fml.ModList;

/**
 * Support for Refined Polymorphism (and Polymorph): choosing between conflicting crafting recipes.
 * <p>
 * Refined Polymorphism only covers the grid node of a placed block (through its block entity recipe data, used once the node
 * has ticked) and the wireless grids of other addons. Our node never ticks, so it would always take the first matching recipe
 * and never show the recipe selection button. This does what Refined Polymorphism does for Refined Storage Addons' wireless
 * crafting grid instead: ask Polymorph for the player's recipe, which also sends the list of candidates to the client.
 * <p>
 * Polymorph classes are only referenced from methods that run when Refined Polymorphism is loaded.
 */
public final class PolymorphCompat {
    private static final String NBT_POLYMORPH_RECIPE = "polymorphRecipe";

    private PolymorphCompat() {
    }

    public static boolean isLoaded() {
        return ModList.get().isLoaded("refinedpolymorph");
    }

    /**
     * Called before the node looks up the recipe of its crafting matrix (server side).
     * Sets the recipe chosen by the player, so the node keeps it when it matches the matrix.
     */
    public static void selectRecipe(UniversalGridNode node, Player player) {
        if (!isLoaded() || !(player instanceof ServerPlayer)
            || !(player.containerMenu instanceof GridContainerMenu menu) || menu.getGrid() != node) {
            return;
        }

        CraftingRecipe recipe = Polymorph.getPlayerRecipe(menu, node, player);
        if (recipe != null) {
            ((GridNetworkNodeAccessor) node).rsunifiedterminal$setCurrentRecipe(recipe);
        }
    }

    /**
     * Stores the selected recipe on exact crafting patterns, like Refined Polymorphism does for the pattern grid block,
     * so autocrafting uses that recipe.
     */
    public static void onPatternCreated(UniversalGridNode node) {
        if (!isLoaded() || node.isProcessingPattern() || !node.isExactPattern()) {
            return;
        }

        CraftingRecipe recipe = ((GridNetworkNodeAccessor) node).rsunifiedterminal$getCurrentRecipe();
        ItemStack pattern = node.getPatterns().getStackInSlot(1);

        if (recipe != null && !pattern.isEmpty()) {
            pattern.getOrCreateTag().putString(NBT_POLYMORPH_RECIPE, recipe.getId().toString());
        }
    }

    /**
     * Holds the references to Polymorph, so they are only loaded when Refined Polymorphism (and thus Polymorph) is present.
     */
    private static final class Polymorph {
        private Polymorph() {
        }

        static CraftingRecipe getPlayerRecipe(GridContainerMenu menu, UniversalGridNode node, Player player) {
            return RecipeSelection.getPlayerRecipe(menu, RecipeType.CRAFTING, node.getCraftingMatrix(), player.level(), player).orElse(null);
        }
    }
}
