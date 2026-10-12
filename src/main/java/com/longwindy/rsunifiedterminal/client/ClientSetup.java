package com.longwindy.rsunifiedterminal.client;

import com.longwindy.rsunifiedterminal.RSUnifiedTerminal;
import com.longwindy.rsunifiedterminal.client.screen.WirelessCrafterManagerScreen;
import com.longwindy.rsunifiedterminal.craftermanager.WirelessCrafterManagerMenu;
import com.longwindy.rsunifiedterminal.craftermanager.WirelessCrafterManagerSlot;
import com.longwindy.rsunifiedterminal.registry.ModItems;
import com.longwindy.rsunifiedterminal.registry.ModMenus;
import com.refinedmods.refinedstorage.apiimpl.API;
import com.refinedmods.refinedstorage.item.property.NetworkItemPropertyGetter;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManagerReloadListener;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterClientReloadListenersEvent;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

@Mod.EventBusSubscriber(modid = RSUnifiedTerminal.MOD_ID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD)
public final class ClientSetup {
    private ClientSetup() {
    }

    @SubscribeEvent
    public static void onClientSetup(FMLClientSetupEvent event) {
        event.enqueueWork(() -> {
            ResourceLocation connected = new ResourceLocation("connected");
            ItemProperties.register(ModItems.WIRELESS_UNIVERSAL_TERMINAL.get(), connected, new NetworkItemPropertyGetter());
            ItemProperties.register(ModItems.CREATIVE_WIRELESS_UNIVERSAL_TERMINAL.get(), connected, new NetworkItemPropertyGetter());

            MenuScreens.register(ModMenus.CRAFTER_MANAGER.get(), WirelessCrafterManagerScreen::create);

            // Patterns show their output while they are in the crafter manager page, like in Refined Storage's crafter manager.
            API.instance().addPatternRenderHandler(ClientSetup::isInCrafterManagerPage);
        });
    }

    private static boolean isInCrafterManagerPage(ItemStack pattern) {
        Player player = Minecraft.getInstance().player;
        if (player == null || !(player.containerMenu instanceof WirelessCrafterManagerMenu menu)) {
            return false;
        }

        for (Slot slot : menu.slots) {
            if (slot instanceof WirelessCrafterManagerSlot && slot.getItem() == pattern) {
                return true;
            }
        }
        return false;
    }

    @SubscribeEvent
    public static void onRegisterKeyMappings(RegisterKeyMappingsEvent event) {
        event.register(TerminalHotkey.OPEN_TERMINAL);
    }

    @SubscribeEvent
    public static void onRegisterReloadListeners(RegisterClientReloadListenersEvent event) {
        event.registerReloadListener((ResourceManagerReloadListener) resourceManager -> ModTextures.clearCache());
    }
}
