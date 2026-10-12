package com.longwindy.rsunifiedterminal.network;

import com.longwindy.rsunifiedterminal.grid.GridParameterSync;
import com.longwindy.rsunifiedterminal.grid.UniversalGridNode;
import com.refinedmods.refinedstorage.blockentity.data.BlockEntitySynchronizationParameter;
import com.refinedmods.refinedstorage.blockentity.grid.GridBlockEntity;
import com.refinedmods.refinedstorage.container.GridContainerMenu;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import javax.annotation.Nullable;
import java.util.function.Supplier;

/**
 * A pattern grid parameter changed on the client (processing / exact mode, processing type, allowed tags).
 * Replaces Refined Storage's block entity parameter update, which needs a grid block entity.
 */
public record GridParameterMessage(@Nullable BlockEntitySynchronizationParameter<?, GridBlockEntity> parameter, @Nullable Object value) {
    public static void encode(GridParameterMessage message, FriendlyByteBuf buf) {
        ParameterCodec.write(buf, message.parameter, message.value);
    }

    public static GridParameterMessage decode(FriendlyByteBuf buf) {
        ParameterCodec.Decoded decoded = ParameterCodec.read(buf);
        return new GridParameterMessage(decoded.parameter(), decoded.value());
    }

    public static void handle(GridParameterMessage message, Supplier<NetworkEvent.Context> ctx) {
        ServerPlayer player = ctx.get().getSender();
        if (message.parameter != null && player != null
            && player.containerMenu instanceof GridContainerMenu menu && menu.getGrid() instanceof UniversalGridNode node) {
            GridParameterSync.apply(message.parameter, node, message.value);
        }
    }
}
