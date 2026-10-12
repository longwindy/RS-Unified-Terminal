package com.longwindy.rsunifiedterminal.grid;

import com.longwindy.rsunifiedterminal.network.GridParameterSyncMessage;
import com.longwindy.rsunifiedterminal.network.ModNetwork;
import com.refinedmods.refinedstorage.blockentity.config.IType;
import com.refinedmods.refinedstorage.blockentity.data.BlockEntitySynchronizationParameter;
import com.refinedmods.refinedstorage.blockentity.grid.GridBlockEntity;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * Bridges the {@link GridBlockEntity} synchronization parameters of a pattern grid to a {@link UniversalGridNode}.
 * <p>
 * Refined Storage's grid screens read these parameters directly on the client and change them through
 * {@code BlockEntitySynchronizationManager#setParameter}, which only works for a grid block entity.
 * For the universal terminal those changes are rerouted to the node (see {@link #apply}),
 * and the node's values are pushed back to the client parameters (see {@link #send}).
 */
public final class GridParameterSync {
    public static final List<BlockEntitySynchronizationParameter<?, GridBlockEntity>> PARAMETERS = List.of(
        GridBlockEntity.PROCESSING_PATTERN,
        GridBlockEntity.EXACT_PATTERN,
        GridBlockEntity.PROCESSING_TYPE,
        GridBlockEntity.ALLOWED_ITEM_TAGS,
        GridBlockEntity.ALLOWED_FLUID_TAGS
    );

    private final Map<ResourceLocation, Object> lastSent = new HashMap<>();

    @Nullable
    public static BlockEntitySynchronizationParameter<?, GridBlockEntity> byId(ResourceLocation id) {
        for (BlockEntitySynchronizationParameter<?, GridBlockEntity> parameter : PARAMETERS) {
            if (parameter.getId().equals(id)) {
                return parameter;
            }
        }
        return null;
    }

    /**
     * Sends the parameters whose value changed since the last call.
     */
    public void send(ServerPlayer player, UniversalGridNode node) {
        for (BlockEntitySynchronizationParameter<?, GridBlockEntity> parameter : PARAMETERS) {
            Object value = copy(valueOf(parameter, node));
            if (!Objects.equals(lastSent.get(parameter.getId()), value)) {
                lastSent.put(parameter.getId(), value);
                ModNetwork.sendTo(player, new GridParameterSyncMessage(parameter, value));
            }
        }
    }

    private static Object valueOf(BlockEntitySynchronizationParameter<?, GridBlockEntity> parameter, UniversalGridNode node) {
        if (parameter == GridBlockEntity.PROCESSING_PATTERN) {
            return node.isProcessingPattern();
        } else if (parameter == GridBlockEntity.EXACT_PATTERN) {
            return node.isExactPattern();
        } else if (parameter == GridBlockEntity.PROCESSING_TYPE) {
            return node.getType();
        } else if (parameter == GridBlockEntity.ALLOWED_ITEM_TAGS) {
            return node.getAllowedTagList().getAllowedItemTags();
        } else if (parameter == GridBlockEntity.ALLOWED_FLUID_TAGS) {
            return node.getAllowedTagList().getAllowedFluidTags();
        }
        throw new IllegalArgumentException("Unsupported parameter " + parameter.getId());
    }

    /**
     * Applies a parameter change requested by the client, mirroring the consumers declared in {@link GridBlockEntity}.
     */
    @SuppressWarnings("unchecked")
    public static void apply(BlockEntitySynchronizationParameter<?, GridBlockEntity> parameter, UniversalGridNode node, Object value) {
        if (parameter == GridBlockEntity.PROCESSING_PATTERN) {
            node.setProcessingPattern((Boolean) value);
            node.clearMatrix();
        } else if (parameter == GridBlockEntity.EXACT_PATTERN) {
            node.setExactPattern((Boolean) value);
        } else if (parameter == GridBlockEntity.PROCESSING_TYPE) {
            int type = (Integer) value;
            if (type == IType.ITEMS || type == IType.FLUIDS) {
                node.setType(type);
            }
        } else if (parameter == GridBlockEntity.ALLOWED_ITEM_TAGS) {
            node.getAllowedTagList().setAllowedItemTags((List<Set<ResourceLocation>>) value);
        } else if (parameter == GridBlockEntity.ALLOWED_FLUID_TAGS) {
            node.getAllowedTagList().setAllowedFluidTags((List<Set<ResourceLocation>>) value);
        }
        node.markDirty();
    }

    // The allowed tag lists are mutable, so keep a snapshot to detect changes.
    private static Object copy(Object value) {
        if (value instanceof List<?> list) {
            List<Object> copy = new ArrayList<>(list.size());
            for (Object element : list) {
                copy.add(element instanceof Set<?> set ? new HashSet<>(set) : element);
            }
            return copy;
        }
        return value;
    }
}
