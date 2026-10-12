package com.longwindy.rsunifiedterminal.network;

import com.longwindy.rsunifiedterminal.grid.UniversalGridNode;
import com.refinedmods.refinedstorage.container.GridContainerMenu;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

/**
 * Grid settings changed through the side buttons and tabs of a universal terminal grid page.
 */
public record GridSettingsMessage(int viewType, int sortingDirection, int sortingType, int searchBoxMode, int size, int tabSelected, int tabPage) {
    public static void encode(GridSettingsMessage message, FriendlyByteBuf buf) {
        buf.writeVarInt(message.viewType);
        buf.writeVarInt(message.sortingDirection);
        buf.writeVarInt(message.sortingType);
        buf.writeVarInt(message.searchBoxMode);
        buf.writeVarInt(message.size);
        buf.writeInt(message.tabSelected);
        buf.writeVarInt(message.tabPage);
    }

    public static GridSettingsMessage decode(FriendlyByteBuf buf) {
        return new GridSettingsMessage(buf.readVarInt(), buf.readVarInt(), buf.readVarInt(), buf.readVarInt(), buf.readVarInt(), buf.readInt(), buf.readVarInt());
    }

    public static void handle(GridSettingsMessage message, Supplier<NetworkEvent.Context> ctx) {
        ServerPlayer player = ctx.get().getSender();
        if (player != null && player.containerMenu instanceof GridContainerMenu menu && menu.getGrid() instanceof UniversalGridNode node) {
            node.applySettings(message);
        }
    }
}
