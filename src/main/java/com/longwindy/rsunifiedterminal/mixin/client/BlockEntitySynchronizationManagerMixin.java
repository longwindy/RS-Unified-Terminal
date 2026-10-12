package com.longwindy.rsunifiedterminal.mixin.client;

import com.longwindy.rsunifiedterminal.client.TerminalClient;
import com.refinedmods.refinedstorage.blockentity.data.BlockEntitySynchronizationManager;
import com.refinedmods.refinedstorage.blockentity.data.BlockEntitySynchronizationParameter;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Refined Storage's pattern grid screen changes its settings through block entity parameters,
 * which the server applies to the block entity of the open menu. The terminal has none, so send them to the terminal instead.
 */
@Mixin(value = BlockEntitySynchronizationManager.class, remap = false)
public abstract class BlockEntitySynchronizationManagerMixin {
    @Inject(method = "setParameter", at = @At("HEAD"), cancellable = true)
    private static void rsunifiedterminal$redirectToTerminal(BlockEntitySynchronizationParameter<?, ?> parameter, Object value, CallbackInfo ci) {
        if (TerminalClient.handleParameter(parameter, value)) {
            ci.cancel();
        }
    }
}
