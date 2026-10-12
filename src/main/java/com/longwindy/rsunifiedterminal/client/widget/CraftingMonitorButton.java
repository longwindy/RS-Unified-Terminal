package com.longwindy.rsunifiedterminal.client.widget;

import com.longwindy.rsunifiedterminal.client.ModTextures;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractButton;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import org.lwjgl.glfw.GLFW;

/**
 * The 12x12 button in the top right corner, above the scrollbar: opens the crafting monitor from a grid page,
 * and goes back to the terminal from the crafting monitor.
 */
public class CraftingMonitorButton extends AbstractButton {
    public static final int SIZE = 12;

    private final Runnable action;

    public CraftingMonitorButton(int x, int y, Component tooltip, Runnable action) {
        super(x, y, SIZE, SIZE, tooltip);
        this.action = action;
        setTooltip(Tooltip.create(tooltip));
    }

    @Override
    public void onPress() {
        action.run();
    }

    @Override
    protected void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        if (!isHovered()) {
            draw(graphics, ModTextures.CRAFTING_MONITOR_BUTTON);
            return;
        }

        boolean pressed = GLFW.glfwGetMouseButton(Minecraft.getInstance().getWindow().getWindow(), GLFW.GLFW_MOUSE_BUTTON_LEFT) == GLFW.GLFW_PRESS;
        ResourceLocation texture = pressed ? ModTextures.CRAFTING_MONITOR_BUTTON_PRESSED : ModTextures.CRAFTING_MONITOR_BUTTON_HOVER;

        if (ModTextures.exists(texture)) {
            draw(graphics, texture);
        } else {
            // The hover and pressed textures are optional: tint the normal one instead.
            draw(graphics, ModTextures.CRAFTING_MONITOR_BUTTON);
            graphics.fill(getX(), getY(), getX() + SIZE, getY() + SIZE, pressed ? 0x50000000 : 0x60FFFFFF);
        }
    }

    private void draw(GuiGraphics graphics, ResourceLocation texture) {
        graphics.blit(texture, getX(), getY(), 0, 0, SIZE, SIZE, SIZE, SIZE);
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput output) {
        defaultButtonNarrationText(output);
    }
}
