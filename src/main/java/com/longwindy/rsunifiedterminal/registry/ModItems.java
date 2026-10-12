package com.longwindy.rsunifiedterminal.registry;

import com.longwindy.rsunifiedterminal.RSUnifiedTerminal;
import com.longwindy.rsunifiedterminal.item.UniversalTerminalItem;
import net.minecraft.world.item.Item;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public final class ModItems {
    private static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, RSUnifiedTerminal.MOD_ID);

    public static final RegistryObject<UniversalTerminalItem> WIRELESS_UNIVERSAL_TERMINAL =
        ITEMS.register("wireless_universal_terminal", () -> new UniversalTerminalItem(UniversalTerminalItem.Type.NORMAL));
    public static final RegistryObject<UniversalTerminalItem> CREATIVE_WIRELESS_UNIVERSAL_TERMINAL =
        ITEMS.register("creative_wireless_universal_terminal", () -> new UniversalTerminalItem(UniversalTerminalItem.Type.CREATIVE));

    private ModItems() {
    }

    public static void register(IEventBus bus) {
        ITEMS.register(bus);
    }
}
