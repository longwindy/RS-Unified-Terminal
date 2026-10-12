package com.longwindy.rsunifiedterminal.grid;

import com.longwindy.rsunifiedterminal.RSUnifiedTerminal;
import com.longwindy.rsunifiedterminal.terminal.TerminalData;
import com.longwindy.rsunifiedterminal.terminal.TerminalPage;
import com.refinedmods.refinedstorage.api.network.grid.GridFactoryType;
import com.refinedmods.refinedstorage.api.network.grid.GridType;
import com.refinedmods.refinedstorage.api.network.grid.IGrid;
import com.refinedmods.refinedstorage.api.network.grid.IGridFactory;
import com.refinedmods.refinedstorage.inventory.player.PlayerSlot;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;

import javax.annotation.Nullable;

/**
 * Creates the grid of the crafting or pattern page, on the server when opening and on the client from the opened stack.
 */
public class UniversalGridFactory implements IGridFactory {
    public static final ResourceLocation ID = new ResourceLocation(RSUnifiedTerminal.MOD_ID, "universal_grid");

    @Nullable
    @Override
    public IGrid createFromStack(Player player, ItemStack stack, PlayerSlot slot) {
        GridType type = TerminalData.getPage(stack) == TerminalPage.PATTERN ? GridType.PATTERN : GridType.CRAFTING;
        return new UniversalGridNode(player, stack, slot, type);
    }

    @Nullable
    @Override
    public IGrid createFromBlock(Player player, BlockPos pos) {
        return null;
    }

    @Nullable
    @Override
    public BlockEntity getRelevantBlockEntity(Level level, BlockPos pos) {
        return null;
    }

    @Override
    public GridFactoryType getType() {
        return GridFactoryType.STACK;
    }
}
