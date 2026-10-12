package com.longwindy.rsunifiedterminal.client.widget;

import com.longwindy.rsunifiedterminal.client.ModTextures;
import com.longwindy.rsunifiedterminal.network.ModNetwork;
import com.longwindy.rsunifiedterminal.network.ScalePatternMessage;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import org.lwjgl.glfw.GLFW;

/**
 * Between the inputs and outputs of a processing pattern: left click multiplies every amount, right click divides it.
 * Shows "x2", or "x8" while Shift is held.
 */
public class ScalePatternButton extends AbstractWidget {
    public static final int SIZE = 16;

    private static final int TEXT_COLOR = 0x404040;

    public ScalePatternButton(int x, int y) {
        super(x, y, SIZE, SIZE, Component.empty());
        setTooltip(Tooltip.create(Component.translatable("gui.rsunifiedterminal.scale_pattern")));
    }

    private static int getFactor() {
        return Screen.hasShiftDown() ? 8 : 2;
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (!active || !visible || !isMouseOver(mouseX, mouseY)
            || (button != GLFW.GLFW_MOUSE_BUTTON_LEFT && button != GLFW.GLFW_MOUSE_BUTTON_RIGHT)) {
            return false;
        }

        playDownSound(Minecraft.getInstance().getSoundManager());
        ModNetwork.CHANNEL.sendToServer(new ScalePatternMessage(button == GLFW.GLFW_MOUSE_BUTTON_LEFT, getFactor()));
        return true;
    }

    @Override
    protected void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        long window = Minecraft.getInstance().getWindow().getWindow();
        boolean pressed = isHovered() && (GLFW.glfwGetMouseButton(window, GLFW.GLFW_MOUSE_BUTTON_LEFT) == GLFW.GLFW_PRESS
            || GLFW.glfwGetMouseButton(window, GLFW.GLFW_MOUSE_BUTTON_RIGHT) == GLFW.GLFW_PRESS);

        ResourceLocation texture = ModTextures.SCALE_PATTERN_BUTTON;
        if (pressed && ModTextures.exists(ModTextures.SCALE_PATTERN_BUTTON_PRESSED)) {
            texture = ModTextures.SCALE_PATTERN_BUTTON_PRESSED;
        } else if (isHovered() && !pressed && ModTextures.exists(ModTextures.SCALE_PATTERN_BUTTON_HOVER)) {
            texture = ModTextures.SCALE_PATTERN_BUTTON_HOVER;
        }
        graphics.blit(texture, getX(), getY(), 0, 0, SIZE, SIZE, SIZE, SIZE);

        // The hover and pressed textures are optional: tint the normal one instead.
        if (texture == ModTextures.SCALE_PATTERN_BUTTON && isHovered()) {
            graphics.fill(getX(), getY(), getX() + SIZE, getY() + SIZE, pressed ? 0x50000000 : 0x60FFFFFF);
        }

        Font font = Minecraft.getInstance().font;
        String text = "x" + getFactor();
        // Font#width includes the 1 px spacing after the last glyph, and glyphs are 7 px tall.
        int textX = getX() + (SIZE - (font.width(text) - 1)) / 2;
        int textY = getY() + (SIZE - 7) / 2;
        graphics.drawString(font, text, textX, textY, TEXT_COLOR, false);
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput output) {
        defaultButtonNarrationText(output);
    }
}
