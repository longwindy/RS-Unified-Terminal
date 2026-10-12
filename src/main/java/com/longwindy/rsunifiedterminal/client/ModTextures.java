package com.longwindy.rsunifiedterminal.client;

import com.longwindy.rsunifiedterminal.RSUnifiedTerminal;
import com.longwindy.rsunifiedterminal.terminal.TerminalPage;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;

import java.util.HashMap;
import java.util.Map;

/**
 * GUI textures, see 贴图/绘制清单.md for their sizes and where they are drawn.
 */
public final class ModTextures {
    public static final ResourceLocation CRAFTING_MONITOR_BUTTON = gui("crafting_monitor_button");
    public static final ResourceLocation CRAFTING_MONITOR_BUTTON_HOVER = gui("crafting_monitor_button_hover");
    public static final ResourceLocation CRAFTING_MONITOR_BUTTON_PRESSED = gui("crafting_monitor_button_pressed");

    public static final ResourceLocation SCALE_PATTERN_BUTTON = gui("scale_pattern_button");
    public static final ResourceLocation SCALE_PATTERN_BUTTON_HOVER = gui("scale_pattern_button_hover");
    public static final ResourceLocation SCALE_PATTERN_BUTTON_PRESSED = gui("scale_pattern_button_pressed");

    public static final ResourceLocation SIDE_BUTTON_VIEW_ITEMS = gui("side_button_view_items");
    public static final ResourceLocation SIDE_BUTTON_VIEW_FLUIDS = gui("side_button_view_fluids");

    private static final Map<ResourceLocation, Boolean> EXISTS = new HashMap<>();

    private ModTextures() {
    }

    private static ResourceLocation gui(String name) {
        return new ResourceLocation(RSUnifiedTerminal.MOD_ID, "textures/gui/" + name + ".png");
    }

    public static ResourceLocation page(TerminalPage page) {
        return gui("side_button_page_" + page.name().toLowerCase());
    }

    /**
     * The optional "current page" variant of a page button, see the checklist.
     */
    public static ResourceLocation selectedPage(TerminalPage page) {
        return gui("side_button_page_" + page.name().toLowerCase() + "_selected");
    }

    public static boolean exists(ResourceLocation texture) {
        return EXISTS.computeIfAbsent(texture, t -> Minecraft.getInstance().getResourceManager().getResource(t).isPresent());
    }

    /**
     * Forgets which optional textures exist, after resource packs are reloaded.
     */
    public static void clearCache() {
        EXISTS.clear();
    }
}
