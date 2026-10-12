package com.longwindy.rsunifiedterminal.client;

import com.longwindy.rsunifiedterminal.RSUnifiedTerminal;
import com.longwindy.rsunifiedterminal.compat.CuriosCompat;
import com.longwindy.rsunifiedterminal.registry.ModItems;
import com.mojang.blaze3d.platform.InputConstants;
import com.refinedmods.refinedstorage.RS;
import com.refinedmods.refinedstorage.inventory.player.PlayerSlot;
import com.refinedmods.refinedstorage.network.OpenNetworkItemMessage;
import com.refinedmods.refinedstorage.screen.KeyInputListener;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.client.settings.KeyConflictContext;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import javax.annotation.Nullable;

/**
 * "Open Wireless Universal Terminal" hotkey, listed with Refined Storage's keys and unbound by default.
 * Looks in the inventory first, then in the curio slots, preferring the normal terminal over the creative one.
 */
@Mod.EventBusSubscriber(modid = RSUnifiedTerminal.MOD_ID, value = Dist.CLIENT)
public final class TerminalHotkey {
    public static final KeyMapping OPEN_TERMINAL = new KeyMapping(
        "key.rsunifiedterminal.open_terminal",
        KeyConflictContext.IN_GAME,
        InputConstants.UNKNOWN,
        RS.NAME
    );

    private TerminalHotkey() {
    }

    @SubscribeEvent
    public static void onKeyInput(InputEvent.Key event) {
        while (OPEN_TERMINAL.consumeClick()) {
            open();
        }
    }

    private static void open() {
        Player player = Minecraft.getInstance().player;
        if (player == null) {
            return;
        }

        PlayerSlot slot = find(player, ModItems.WIRELESS_UNIVERSAL_TERMINAL.get());
        if (slot == null) {
            slot = find(player, ModItems.CREATIVE_WIRELESS_UNIVERSAL_TERMINAL.get());
        }

        if (slot == null) {
            KeyInputListener.sendError(Component.translatable(
                "misc.refinedstorage.network_item.shortcut_not_found",
                Component.translatable(ModItems.WIRELESS_UNIVERSAL_TERMINAL.get().getDescriptionId())
            ));
            return;
        }

        RS.NETWORK_HANDLER.sendToServer(new OpenNetworkItemMessage(slot));
    }

    @Nullable
    private static PlayerSlot find(Player player, Item item) {
        Inventory inventory = player.getInventory();
        for (int i = 0; i < inventory.getContainerSize(); ++i) {
            if (inventory.getItem(i).is(item)) {
                return new PlayerSlot(i);
            }
        }
        return CuriosCompat.isLoaded() ? CuriosCompat.find(player, item) : null;
    }
}
