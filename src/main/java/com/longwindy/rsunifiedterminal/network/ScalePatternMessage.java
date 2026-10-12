package com.longwindy.rsunifiedterminal.network;

import com.longwindy.rsunifiedterminal.grid.PatternScaler;
import com.longwindy.rsunifiedterminal.grid.UniversalGridNode;
import com.refinedmods.refinedstorage.api.network.grid.GridType;
import com.refinedmods.refinedstorage.container.GridContainerMenu;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

/**
 * Multiplies or divides the processing pattern being edited on the pattern page, see {@link PatternScaler}.
 *
 * @param multiply  true to multiply, false to divide
 * @param maxFactor 2, or 8 when Shift is held
 */
public record ScalePatternMessage(boolean multiply, int maxFactor) {
    public static void encode(ScalePatternMessage message, FriendlyByteBuf buf) {
        buf.writeBoolean(message.multiply);
        buf.writeVarInt(message.maxFactor);
    }

    public static ScalePatternMessage decode(FriendlyByteBuf buf) {
        return new ScalePatternMessage(buf.readBoolean(), buf.readVarInt());
    }

    public static void handle(ScalePatternMessage message, Supplier<NetworkEvent.Context> ctx) {
        ServerPlayer player = ctx.get().getSender();
        if (player == null || (message.maxFactor != 2 && message.maxFactor != 8)) {
            return;
        }

        if (player.containerMenu instanceof GridContainerMenu menu
            && menu.getGrid() instanceof UniversalGridNode node
            && node.getGridType() == GridType.PATTERN
            && node.isProcessingPattern()) {
            PatternScaler.scale(node.getProcessingMatrix(), node.getProcessingMatrixFluids(), node.getAllowedTagList(), message.multiply, message.maxFactor);
        }
    }
}
