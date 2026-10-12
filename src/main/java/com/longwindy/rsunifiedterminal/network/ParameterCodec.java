package com.longwindy.rsunifiedterminal.network;

import com.longwindy.rsunifiedterminal.grid.GridParameterSync;
import com.refinedmods.refinedstorage.blockentity.data.BlockEntitySynchronizationParameter;
import com.refinedmods.refinedstorage.blockentity.grid.GridBlockEntity;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.syncher.EntityDataSerializer;

import javax.annotation.Nullable;

/**
 * Encodes a grid parameter value with the serializer of the parameter itself.
 */
final class ParameterCodec {
    private ParameterCodec() {
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    static void write(FriendlyByteBuf buf, BlockEntitySynchronizationParameter<?, GridBlockEntity> parameter, Object value) {
        buf.writeResourceLocation(parameter.getId());
        ((EntityDataSerializer) parameter.getSerializer()).write(buf, value);
    }

    static Decoded read(FriendlyByteBuf buf) {
        BlockEntitySynchronizationParameter<?, GridBlockEntity> parameter = GridParameterSync.byId(buf.readResourceLocation());
        if (parameter == null) {
            // Unknown parameter: the rest of the payload can not be decoded, skip it.
            buf.skipBytes(buf.readableBytes());
            return new Decoded(null, null);
        }
        return new Decoded(parameter, parameter.getSerializer().read(buf));
    }

    record Decoded(@Nullable BlockEntitySynchronizationParameter<?, GridBlockEntity> parameter, @Nullable Object value) {
    }
}
