package com.longwindy.rsunifiedterminal.compat;

import com.refinedmods.refinedstorage.inventory.player.PlayerSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraftforge.fml.ModList;
import top.theillusivec4.curios.api.CuriosApi;

import javax.annotation.Nullable;

/**
 * Finds the terminal in a curio slot, the same way Refined Storage does for its own wireless items.
 * Only call {@link #find} when {@link #isLoaded()} is true.
 */
public final class CuriosCompat {
    private CuriosCompat() {
    }

    public static boolean isLoaded() {
        return ModList.get().isLoaded("curios");
    }

    @Nullable
    @SuppressWarnings("removal")
    public static PlayerSlot find(Player player, Item item) {
        return CuriosApi.getCuriosHelper()
            .findEquippedCurio(stack -> stack.is(item), player)
            .map(curio -> new PlayerSlot(curio.getMiddle(), curio.getLeft()))
            .orElse(null);
    }
}
