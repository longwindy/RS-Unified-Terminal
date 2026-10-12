package com.longwindy.rsunifiedterminal.client;

import com.longwindy.rsunifiedterminal.RSUnifiedTerminal;
import com.longwindy.rsunifiedterminal.grid.UniversalGridNode;
import com.refinedmods.refinedstorage.screen.grid.CraftingSettingsScreen;
import com.refinedmods.refinedstorage.screen.grid.GridScreen;
import com.refinedmods.refinedstorage.screen.grid.stack.IGridStack;
import com.refinedmods.refinedstorage.screen.grid.view.IGridView;
import net.minecraft.client.Minecraft;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ScreenEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.lwjgl.glfw.GLFW;

import javax.annotation.Nullable;
import java.util.List;

/**
 * Middle-clicking a resource on a grid page of the terminal opens its autocrafting request window,
 * whether or not it is currently in stock. Same behavior as the RS FastCraft mod, built in for the terminal.
 * <p>
 * Refined Storage keeps two entries per craftable resource on the client: the stored one and a craftable one,
 * linked through {@link IGridStack#getOtherId()}. While the resource is in stock only the stored entry is shown,
 * and the request window is only reachable through the hidden CTRL + SHIFT + click shortcut.
 */
@Mod.EventBusSubscriber(modid = RSUnifiedTerminal.MOD_ID, value = Dist.CLIENT)
public final class MiddleClickCraftHandler {
    private MiddleClickCraftHandler() {
    }

    @SubscribeEvent
    public static void onMouseButtonPressed(ScreenEvent.MouseButtonPressed.Pre event) {
        if (event.getButton() != GLFW.GLFW_MOUSE_BUTTON_MIDDLE
            || !(event.getScreen() instanceof GridScreen screen)
            || !(screen.getGrid() instanceof UniversalGridNode)) {
            return;
        }
        // Holding an item: leave the click to Refined Storage.
        if (!screen.getMenu().getCarried().isEmpty()) {
            return;
        }

        IGridStack hovered = getHoveredStack(screen);
        if (hovered == null) {
            return;
        }

        // From here on the click is over a grid resource, so it is always consumed. Otherwise Refined
        // Storage would treat it as a left click and pull out a full stack of a non-craftable item.
        event.setCanceled(true);

        IGridView view = screen.getView();
        if (!view.canCraft()) {
            return;
        }

        IGridStack craftable = getCraftableStack(view, hovered);
        if (craftable != null) {
            Minecraft minecraft = Minecraft.getInstance();
            minecraft.setScreen(new CraftingSettingsScreen(screen, minecraft.player, craftable));
        }
    }

    /**
     * Mirrors the private {@code GridScreen#isOverSlotWithStack()}.
     */
    @Nullable
    private static IGridStack getHoveredStack(GridScreen screen) {
        if (!screen.getGrid().isGridActive()) {
            return null;
        }
        int slot = screen.getSlotNumber();
        List<IGridStack> stacks = screen.getView().getStacks();
        if (slot < 0 || slot >= stacks.size()) {
            return null;
        }
        return stacks.get(slot);
    }

    @Nullable
    private static IGridStack getCraftableStack(IGridView view, IGridStack stack) {
        if (stack.isCraftable()) {
            return stack;
        }
        if (stack.getOtherId() != null) {
            return view.get(stack.getOtherId());
        }
        return null;
    }
}
