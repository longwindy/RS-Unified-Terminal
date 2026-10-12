package com.longwindy.rsunifiedterminal.mixin.client.jei;

import com.longwindy.rsunifiedterminal.client.TerminalScreens;
import mezz.jei.gui.ghost.GhostIngredientQuickMoveManager;
import mezz.jei.gui.input.UserInput;
import net.minecraft.client.gui.screens.Screen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * JEI binds both "cheat 1 stack" and "quick move ghost item" to Shift + left click, and quick move is checked first.
 * While simulating the click, quick move claims it as soon as the screen has a ghost ingredient handler,
 * which every Refined Storage screen has. Refined Storage's handler can't quick move though, so the click does nothing
 * and the cheat never happens. Quick move is skipped on the terminal screens so cheating works there.
 * <p>
 * Only applied when JEI is loaded, see {@code MixinPlugin}. {@code require = 0} keeps other JEI versions from crashing.
 */
@Mixin(value = GhostIngredientQuickMoveManager.class, remap = false)
public abstract class GhostIngredientQuickMoveManagerMixin {
    @Inject(method = "quickMove", at = @At("HEAD"), cancellable = true, require = 0)
    private void rsunifiedterminal$skipOnTerminalScreens(Screen screen, UserInput input, CallbackInfoReturnable<Boolean> cir) {
        if (TerminalScreens.isTerminalScreen(screen)) {
            cir.setReturnValue(false);
        }
    }
}
