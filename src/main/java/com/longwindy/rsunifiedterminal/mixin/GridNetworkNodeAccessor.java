package com.longwindy.rsunifiedterminal.mixin;

import com.refinedmods.refinedstorage.apiimpl.network.node.GridNetworkNode;
import net.minecraft.world.item.crafting.CraftingRecipe;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import javax.annotation.Nullable;

@Mixin(value = GridNetworkNode.class, remap = false)
public interface GridNetworkNodeAccessor {
    @Nullable
    @Accessor("currentRecipe")
    CraftingRecipe rsunifiedterminal$getCurrentRecipe();
}
