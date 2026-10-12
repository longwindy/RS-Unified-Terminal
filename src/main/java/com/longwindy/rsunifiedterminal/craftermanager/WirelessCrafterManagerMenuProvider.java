package com.longwindy.rsunifiedterminal.craftermanager;

import com.refinedmods.refinedstorage.api.network.INetwork;
import com.refinedmods.refinedstorage.inventory.player.PlayerSlot;
import com.refinedmods.refinedstorage.screen.EmptyScreenInfoProvider;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.IItemHandlerModifiable;
import net.minecraftforge.network.NetworkHooks;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Opens the crafter manager page. The client receives the slot of the terminal and the crafter categories with their slot count,
 * like Refined Storage's crafter manager.
 */
public class WirelessCrafterManagerMenuProvider implements MenuProvider {
    private final ItemStack stack;
    private final PlayerSlot slot;
    private final INetwork network;

    private WirelessCrafterManagerMenuProvider(ItemStack stack, PlayerSlot slot, INetwork network) {
        this.stack = stack;
        this.slot = slot;
        this.network = network;
    }

    public static void open(ServerPlayer player, INetwork network, ItemStack stack, PlayerSlot slot) {
        NetworkHooks.openScreen(player, new WirelessCrafterManagerMenuProvider(stack, slot, network), buf -> {
            slot.writePlayerSlot(buf);

            Map<Component, List<IItemHandlerModifiable>> containerData = network.getCraftingManager().getNamedContainers();
            buf.writeInt(containerData.size());
            for (Map.Entry<Component, List<IItemHandlerModifiable>> entry : containerData.entrySet()) {
                buf.writeComponent(entry.getKey());

                int slots = 0;
                for (IItemHandlerModifiable handler : entry.getValue()) {
                    slots += handler.getSlots();
                }
                buf.writeInt(slots);
            }
        });
    }

    /**
     * Client side menu factory, reading what {@link #open} writes.
     */
    public static WirelessCrafterManagerMenu create(int windowId, Inventory inventory, FriendlyByteBuf buf) {
        PlayerSlot slot = new PlayerSlot(buf);

        Map<String, Integer> data = new LinkedHashMap<>();
        int size = buf.readInt();
        for (int i = 0; i < size; ++i) {
            data.put(buf.readComponent().getString(), buf.readInt());
        }

        WirelessCrafterManagerMenu menu = new WirelessCrafterManagerMenu(inventory.player, windowId, slot.getStackFromSlot(inventory.player), slot, null);
        menu.setScreenInfoProvider(new EmptyScreenInfoProvider());
        menu.initSlots(data);
        return menu;
    }

    @Override
    public Component getDisplayName() {
        return Component.translatable("gui.refinedstorage.crafter_manager");
    }

    @Override
    public AbstractContainerMenu createMenu(int windowId, Inventory inventory, Player player) {
        WirelessCrafterManagerMenu menu = new WirelessCrafterManagerMenu(player, windowId, stack, slot, network);
        menu.setScreenInfoProvider(new EmptyScreenInfoProvider());
        menu.initSlotsServer();
        return menu;
    }
}
