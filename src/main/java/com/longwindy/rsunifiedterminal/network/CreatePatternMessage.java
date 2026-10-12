package com.longwindy.rsunifiedterminal.network;

import com.longwindy.rsunifiedterminal.grid.UniversalGridNode;
import com.refinedmods.refinedstorage.api.network.grid.GridType;
import com.refinedmods.refinedstorage.container.GridContainerMenu;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

/**
 * Creates a pattern on the pattern page. Refined Storage's own message looks up the pattern grid by block position.
 */
public record CreatePatternMessage() {
    public static void encode(CreatePatternMessage message, FriendlyByteBuf buf) {
        // No payload
    }

    public static CreatePatternMessage decode(FriendlyByteBuf buf) {
        return new CreatePatternMessage();
    }

    public static void handle(CreatePatternMessage message, Supplier<NetworkEvent.Context> ctx) {
        ServerPlayer player = ctx.get().getSender();
        if (player != null && player.containerMenu instanceof GridContainerMenu menu
            && menu.getGrid() instanceof UniversalGridNode node && node.getGridType() == GridType.PATTERN) {
            node.onCreatePattern();
        }
    }
}
