package com.longwindy.rsunifiedterminal;

import com.longwindy.rsunifiedterminal.grid.UniversalGridFactory;
import com.longwindy.rsunifiedterminal.network.ModNetwork;
import com.longwindy.rsunifiedterminal.registry.ModItems;
import com.longwindy.rsunifiedterminal.registry.ModMenus;
import com.refinedmods.refinedstorage.RS;
import com.refinedmods.refinedstorage.apiimpl.API;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraftforge.event.BuildCreativeModeTabContentsEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod(RSUnifiedTerminal.MOD_ID)
public final class RSUnifiedTerminal {
    public static final String MOD_ID = "rsunifiedterminal";

    private static final ResourceKey<CreativeModeTab> RS_CREATIVE_TAB =
        ResourceKey.create(Registries.CREATIVE_MODE_TAB, new ResourceLocation(RS.ID, "general"));

    public RSUnifiedTerminal() {
        IEventBus bus = FMLJavaModLoadingContext.get().getModEventBus();

        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, Config.SPEC);

        ModItems.register(bus);
        ModMenus.register(bus);

        bus.addListener(this::onCommonSetup);
        bus.addListener(this::onBuildCreativeTab);
    }

    private void onCommonSetup(FMLCommonSetupEvent event) {
        ModNetwork.register();
        API.instance().getGridManager().add(UniversalGridFactory.ID, new UniversalGridFactory());
    }

    private void onBuildCreativeTab(BuildCreativeModeTabContentsEvent event) {
        if (RS_CREATIVE_TAB.equals(event.getTabKey())) {
            event.accept(ModItems.WIRELESS_UNIVERSAL_TERMINAL);
            event.accept(ModItems.CREATIVE_WIRELESS_UNIVERSAL_TERMINAL);
        }
    }
}
