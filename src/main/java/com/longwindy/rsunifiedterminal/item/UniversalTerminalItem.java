package com.longwindy.rsunifiedterminal.item;

import com.longwindy.rsunifiedterminal.Config;
import com.longwindy.rsunifiedterminal.terminal.UniversalTerminalNetworkItem;
import com.refinedmods.refinedstorage.api.network.item.INetworkItem;
import com.refinedmods.refinedstorage.api.network.item.INetworkItemManager;
import com.refinedmods.refinedstorage.inventory.player.PlayerSlot;
import com.refinedmods.refinedstorage.item.NetworkItem;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import javax.annotation.Nonnull;

/**
 * The Wireless Universal Terminal. Uses the same network binding and energy handling as Refined Storage's wireless items.
 */
public class UniversalTerminalItem extends NetworkItem {
    private final Type type;

    public UniversalTerminalItem(Type type) {
        super(new Item.Properties().stacksTo(1), type == Type.CREATIVE, Config.CAPACITY::get);
        this.type = type;
    }

    public Type getType() {
        return type;
    }

    @Override
    @Nonnull
    public INetworkItem provide(INetworkItemManager handler, Player player, ItemStack stack, PlayerSlot slot) {
        return new UniversalTerminalNetworkItem(handler, player, stack, slot);
    }

    public enum Type {
        NORMAL,
        CREATIVE
    }
}
