package com.longwindy.rsunifiedterminal.terminal;

import com.longwindy.rsunifiedterminal.Config;
import com.longwindy.rsunifiedterminal.craftermanager.WirelessCrafterManagerMenuProvider;
import com.longwindy.rsunifiedterminal.grid.UniversalGridFactory;
import com.longwindy.rsunifiedterminal.item.UniversalTerminalItem;
import com.refinedmods.refinedstorage.RSContainerMenus;
import com.refinedmods.refinedstorage.api.network.INetwork;
import com.refinedmods.refinedstorage.api.network.item.INetworkItem;
import com.refinedmods.refinedstorage.api.network.item.INetworkItemManager;
import com.refinedmods.refinedstorage.api.network.security.Permission;
import com.refinedmods.refinedstorage.apiimpl.API;
import com.refinedmods.refinedstorage.blockentity.craftingmonitor.WirelessCraftingMonitor;
import com.refinedmods.refinedstorage.container.factory.CraftingMonitorMenuProvider;
import com.refinedmods.refinedstorage.inventory.player.PlayerSlot;
import com.refinedmods.refinedstorage.util.LevelUtils;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.energy.IEnergyStorage;
import net.minecraftforge.network.NetworkHooks;

/**
 * The open terminal, tracked by Refined Storage's network item manager while one of its pages is shown.
 */
public class UniversalTerminalNetworkItem implements INetworkItem {
    private final INetworkItemManager handler;
    private final Player player;
    private final ItemStack stack;
    private final PlayerSlot slot;

    public UniversalTerminalNetworkItem(INetworkItemManager handler, Player player, ItemStack stack, PlayerSlot slot) {
        this.handler = handler;
        this.player = player;
        this.stack = stack;
        this.slot = slot;
    }

    @Override
    public Player getPlayer() {
        return player;
    }

    @Override
    public boolean onOpen(INetwork network) {
        Integer pendingTarget = TerminalOpener.getPendingTarget(player);
        boolean switching = pendingTarget != null;
        int target = switching ? pendingTarget : TerminalOpener.TARGET_LAST_PAGE;

        if (usesEnergy()) {
            IEnergyStorage energy = stack.getCapability(ForgeCapabilities.ENERGY).orElse(null);
            int required = switching ? 0 : Config.OPEN_USAGE.get();
            if (energy != null && energy.getEnergyStored() <= required) {
                sendOutOfEnergyMessage();
                return false;
            }
        }

        if (!network.getSecurityManager().hasPermission(Permission.MODIFY, player)) {
            LevelUtils.sendNoPermissionMessage(player);
            return false;
        }

        if (target == TerminalOpener.TARGET_CRAFTING_MONITOR) {
            if (!network.getSecurityManager().hasPermission(Permission.AUTOCRAFTING, player)) {
                LevelUtils.sendNoPermissionMessage(player);
                return false;
            }
            openCraftingMonitor();
        } else {
            TerminalPage page = target == TerminalOpener.TARGET_LAST_PAGE ? TerminalData.getPage(stack) : TerminalPage.byId(target);
            if (page == TerminalPage.CRAFTER_MANAGER) {
                WirelessCrafterManagerMenuProvider.open((ServerPlayer) player, network, stack, slot);
            } else {
                API.instance().getGridManager().openGrid(UniversalGridFactory.ID, (ServerPlayer) player, stack, slot);
            }
        }

        if (!switching) {
            drainEnergy(Config.OPEN_USAGE.get());
        }

        return true;
    }

    private void openCraftingMonitor() {
        WirelessCraftingMonitor craftingMonitor = new WirelessCraftingMonitor(stack, player.getServer(), slot);

        NetworkHooks.openScreen(
            (ServerPlayer) player,
            new CraftingMonitorMenuProvider(RSContainerMenus.WIRELESS_CRAFTING_MONITOR.get(), craftingMonitor, null),
            slot::writePlayerSlot
        );
    }

    @Override
    public void drainEnergy(int energy) {
        if (!usesEnergy()) {
            return;
        }

        stack.getCapability(ForgeCapabilities.ENERGY).ifPresent(energyStorage -> {
            energyStorage.extractEnergy(energy, false);

            if (energyStorage.getEnergyStored() <= 0) {
                handler.close(player);
                player.closeContainer();
                sendOutOfEnergyMessage();
            }
        });
    }

    private boolean usesEnergy() {
        return Config.USE_ENERGY.get() && ((UniversalTerminalItem) stack.getItem()).getType() != UniversalTerminalItem.Type.CREATIVE;
    }

    private void sendOutOfEnergyMessage() {
        player.sendSystemMessage(Component.translatable("misc.refinedstorage.network_item.out_of_energy", stack.getItem().getDescription()));
    }
}
