package com.longwindy.rsunifiedterminal.registry;

import com.longwindy.rsunifiedterminal.RSUnifiedTerminal;
import com.longwindy.rsunifiedterminal.craftermanager.WirelessCrafterManagerMenu;
import com.longwindy.rsunifiedterminal.craftermanager.WirelessCrafterManagerMenuProvider;
import net.minecraft.world.inventory.MenuType;
import net.minecraftforge.common.extensions.IForgeMenuType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public final class ModMenus {
    private static final DeferredRegister<MenuType<?>> MENUS = DeferredRegister.create(ForgeRegistries.MENU_TYPES, RSUnifiedTerminal.MOD_ID);

    public static final RegistryObject<MenuType<WirelessCrafterManagerMenu>> CRAFTER_MANAGER =
        MENUS.register("crafter_manager", () -> IForgeMenuType.create(WirelessCrafterManagerMenuProvider::create));

    private ModMenus() {
    }

    public static void register(IEventBus bus) {
        MENUS.register(bus);
    }
}
