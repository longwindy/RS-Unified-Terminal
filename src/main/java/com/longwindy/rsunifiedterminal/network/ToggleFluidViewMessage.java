package com.longwindy.rsunifiedterminal.network;

import com.longwindy.rsunifiedterminal.item.UniversalTerminalItem;
import com.longwindy.rsunifiedterminal.terminal.TerminalData;
import com.longwindy.rsunifiedterminal.terminal.TerminalOpener;
import com.longwindy.rsunifiedterminal.terminal.TerminalPage;
import com.refinedmods.refinedstorage.inventory.player.PlayerSlot;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

/**
 * Switches the crafting page between showing items and showing fluids.
 * The page is reopened, since the server side storage listener depends on the resource type.
 */
public record ToggleFluidViewMessage(PlayerSlot slot) {
    public static void encode(ToggleFluidViewMessage message, FriendlyByteBuf buf) {
        message.slot.writePlayerSlot(buf);
    }

    public static ToggleFluidViewMessage decode(FriendlyByteBuf buf) {
        return new ToggleFluidViewMessage(new PlayerSlot(buf));
    }

    public static void handle(ToggleFluidViewMessage message, Supplier<NetworkEvent.Context> ctx) {
        ServerPlayer player = ctx.get().getSender();
        if (player == null) {
            return;
        }

        ItemStack stack = message.slot.getStackFromSlot(player);
        if (stack.getItem() instanceof UniversalTerminalItem) {
            TerminalData.setFluidView(stack, !TerminalData.isFluidView(stack));
            TerminalOpener.open(player, message.slot, TerminalPage.CRAFTING.getId());
        }
    }
}
