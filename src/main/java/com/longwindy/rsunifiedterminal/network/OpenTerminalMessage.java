package com.longwindy.rsunifiedterminal.network;

import com.longwindy.rsunifiedterminal.terminal.TerminalOpener;
import com.refinedmods.refinedstorage.inventory.player.PlayerSlot;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

/**
 * Asks the server to switch the open terminal to another page or to the crafting monitor.
 *
 * @param target a page id or one of the {@link TerminalOpener} targets
 */
public record OpenTerminalMessage(PlayerSlot slot, int target) {
    public static void encode(OpenTerminalMessage message, FriendlyByteBuf buf) {
        message.slot.writePlayerSlot(buf);
        buf.writeVarInt(message.target);
    }

    public static OpenTerminalMessage decode(FriendlyByteBuf buf) {
        return new OpenTerminalMessage(new PlayerSlot(buf), buf.readVarInt());
    }

    public static void handle(OpenTerminalMessage message, Supplier<NetworkEvent.Context> ctx) {
        ServerPlayer player = ctx.get().getSender();
        if (player != null) {
            TerminalOpener.open(player, message.slot, message.target);
        }
    }
}
