package com.longwindy.rsunifiedterminal.network;

import com.longwindy.rsunifiedterminal.craftermanager.WirelessCrafterManagerMenu;
import com.longwindy.rsunifiedterminal.terminal.TerminalData;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

/**
 * Size and search box mode of the crafter manager page.
 */
public record CrafterManagerSettingsMessage(int size, int searchBoxMode) {
    public static void encode(CrafterManagerSettingsMessage message, FriendlyByteBuf buf) {
        buf.writeVarInt(message.size);
        buf.writeVarInt(message.searchBoxMode);
    }

    public static CrafterManagerSettingsMessage decode(FriendlyByteBuf buf) {
        return new CrafterManagerSettingsMessage(buf.readVarInt(), buf.readVarInt());
    }

    public static void handle(CrafterManagerSettingsMessage message, Supplier<NetworkEvent.Context> ctx) {
        ServerPlayer player = ctx.get().getSender();
        if (player != null && player.containerMenu instanceof WirelessCrafterManagerMenu menu) {
            TerminalData.setCrafterManagerSettings(menu.getStack(), message.size, message.searchBoxMode);
        }
    }
}
