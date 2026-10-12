package com.longwindy.rsunifiedterminal.grid;

import com.refinedmods.refinedstorage.apiimpl.autocrafting.AllowedTagList;
import com.refinedmods.refinedstorage.inventory.fluid.FluidInventory;
import com.refinedmods.refinedstorage.inventory.item.BaseItemHandler;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Multiplies or divides every input and output of a processing pattern being edited, keeping their ratio.
 * <ul>
 *     <li>Multiplying uses the largest factor up to the requested one (8, 4, 2) for which every slot stays within its limit:
 *     64 items and the item's max stack size, or the fluid capacity of the slot (64 buckets).</li>
 *     <li>Dividing uses the largest factor up to the requested one for which every item count and fluid amount (in mB) is divisible,
 *     so items never drop below 1.</li>
 * </ul>
 */
public final class PatternScaler {
    private PatternScaler() {
    }

    /**
     * @param multiply  true to multiply, false to divide
     * @param maxFactor 2 or 8
     * @return the factor that was applied, or 0 if nothing changed
     */
    public static int scale(BaseItemHandler items, FluidInventory fluids, AllowedTagList allowedTags, boolean multiply, int maxFactor) {
        if (isEmpty(items, fluids)) {
            return 0;
        }

        for (int factor = maxFactor; factor >= 2; factor /= 2) {
            if (multiply ? canMultiply(items, fluids, factor) : canDivide(items, fluids, factor)) {
                apply(items, fluids, allowedTags, multiply, factor);
                return factor;
            }
        }

        return 0;
    }

    private static boolean isEmpty(BaseItemHandler items, FluidInventory fluids) {
        for (int i = 0; i < items.getSlots(); ++i) {
            if (!items.getStackInSlot(i).isEmpty()) {
                return false;
            }
        }
        for (int i = 0; i < fluids.getSlots(); ++i) {
            if (!fluids.getFluid(i).isEmpty()) {
                return false;
            }
        }
        return true;
    }

    private static boolean canMultiply(BaseItemHandler items, FluidInventory fluids, int factor) {
        for (int i = 0; i < items.getSlots(); ++i) {
            ItemStack stack = items.getStackInSlot(i);
            if (!stack.isEmpty()) {
                int limit = Math.min(items.getSlotLimit(i), stack.getMaxStackSize());
                if ((long) stack.getCount() * factor > limit) {
                    return false;
                }
            }
        }
        for (int i = 0; i < fluids.getSlots(); ++i) {
            FluidStack fluid = fluids.getFluid(i);
            if (!fluid.isEmpty() && (long) fluid.getAmount() * factor > fluids.getMaxAmount()) {
                return false;
            }
        }
        return true;
    }

    private static boolean canDivide(BaseItemHandler items, FluidInventory fluids, int factor) {
        for (int i = 0; i < items.getSlots(); ++i) {
            ItemStack stack = items.getStackInSlot(i);
            if (!stack.isEmpty() && stack.getCount() % factor != 0) {
                return false;
            }
        }
        for (int i = 0; i < fluids.getSlots(); ++i) {
            FluidStack fluid = fluids.getFluid(i);
            if (!fluid.isEmpty() && fluid.getAmount() % factor != 0) {
                return false;
            }
        }
        return true;
    }

    private static void apply(BaseItemHandler items, FluidInventory fluids, AllowedTagList allowedTags, boolean multiply, int factor) {
        // Changing a slot clears its allowed tags (see GridNetworkNode), so keep them across the change.
        List<Set<ResourceLocation>> itemTags = copy(allowedTags.getAllowedItemTags());
        List<Set<ResourceLocation>> fluidTags = copy(allowedTags.getAllowedFluidTags());

        for (int i = 0; i < items.getSlots(); ++i) {
            ItemStack stack = items.getStackInSlot(i);
            if (!stack.isEmpty()) {
                items.setStackInSlot(i, stack.copyWithCount(multiply ? stack.getCount() * factor : stack.getCount() / factor));
            }
        }
        for (int i = 0; i < fluids.getSlots(); ++i) {
            FluidStack fluid = fluids.getFluid(i);
            if (!fluid.isEmpty()) {
                FluidStack scaled = fluid.copy();
                scaled.setAmount(multiply ? fluid.getAmount() * factor : fluid.getAmount() / factor);
                fluids.setFluid(i, scaled);
            }
        }

        allowedTags.setAllowedItemTags(itemTags);
        allowedTags.setAllowedFluidTags(fluidTags);
    }

    private static List<Set<ResourceLocation>> copy(List<Set<ResourceLocation>> tags) {
        List<Set<ResourceLocation>> copy = new ArrayList<>(tags.size());
        for (Set<ResourceLocation> set : tags) {
            copy.add(new HashSet<>(set));
        }
        return copy;
    }
}
