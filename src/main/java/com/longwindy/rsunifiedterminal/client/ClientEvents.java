package com.longwindy.rsunifiedterminal.client;

import com.longwindy.rsunifiedterminal.RSUnifiedTerminal;
import com.longwindy.rsunifiedterminal.grid.UniversalGridNode;
import com.longwindy.rsunifiedterminal.network.CreatePatternMessage;
import com.longwindy.rsunifiedterminal.network.ModNetwork;
import com.refinedmods.refinedstorage.api.network.grid.GridType;
import com.refinedmods.refinedstorage.screen.grid.GridScreen;
import com.refinedmods.refinedstorage.util.RenderUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.sounds.SoundEvents;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ScreenEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.lwjgl.glfw.GLFW;

/**
 * Client events for Refined Storage's screens when they show the universal terminal.
 * The terminal controls themselves are added by {@link TerminalScreenControls}, called from the screen mixins.
 * The screens themselves stay Refined Storage's, so everything that integrates with them (JEI, Polymorph, other addons) keeps working.
 */
@Mod.EventBusSubscriber(modid = RSUnifiedTerminal.MOD_ID, value = Dist.CLIENT)
public final class ClientEvents {
    private ClientEvents() {
    }

    /**
     * Refined Storage creates patterns through a message that finds the pattern grid by its block position,
     * so the "create pattern" button of the pattern page is handled here instead.
     */
    @SubscribeEvent
    public static void onMouseClicked(ScreenEvent.MouseButtonPressed.Pre event) {
        if (event.getButton() != GLFW.GLFW_MOUSE_BUTTON_LEFT
            || !(event.getScreen() instanceof GridScreen screen)
            || !(screen.getGrid() instanceof UniversalGridNode node)
            || node.getGridType() != GridType.PATTERN) {
            return;
        }

        double mouseX = event.getMouseX() - screen.getGuiLeft();
        double mouseY = event.getMouseY() - screen.getGuiTop();
        int buttonY = screen.getTopHeight() + (screen.getVisibleRows() * 18) + 22;

        if (RenderUtils.inBounds(172, buttonY, 16, 16, mouseX, mouseY)) {
            if (node.canCreatePattern()) {
                Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0F));
                ModNetwork.CHANNEL.sendToServer(new CreatePatternMessage());
            }
            event.setCanceled(true);
        }
    }
}
