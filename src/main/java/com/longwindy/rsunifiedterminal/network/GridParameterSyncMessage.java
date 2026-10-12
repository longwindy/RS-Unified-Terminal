package com.longwindy.rsunifiedterminal.network;

import com.refinedmods.refinedstorage.blockentity.data.BlockEntitySynchronizationParameter;
import com.refinedmods.refinedstorage.blockentity.grid.GridBlockEntity;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import javax.annotation.Nullable;
import java.util.function.Supplier;

/**
 * Sets the client value of a pattern grid parameter, as Refined Storage does for an open grid block entity.
 */
public record GridParameterSyncMessage(@Nullable BlockEntitySynchronizationParameter<?, GridBlockEntity> parameter, @Nullable Object value) {
    public static void encode(GridParameterSyncMessage message, FriendlyByteBuf buf) {
        ParameterCodec.write(buf, message.parameter, message.value);
    }

    public static GridParameterSyncMessage decode(FriendlyByteBuf buf) {
        ParameterCodec.Decoded decoded = ParameterCodec.read(buf);
        return new GridParameterSyncMessage(decoded.parameter(), decoded.value());
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    public static void handle(GridParameterSyncMessage message, Supplier<NetworkEvent.Context> ctx) {
        if (message.parameter != null) {
            ((BlockEntitySynchronizationParameter) message.parameter).setValue(false, message.value);
        }
    }
}
