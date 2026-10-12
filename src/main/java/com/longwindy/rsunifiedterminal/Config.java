package com.longwindy.rsunifiedterminal;

import net.minecraftforge.common.ForgeConfigSpec;

/**
 * Common config, so the energy capacity is available as soon as item stacks get their capabilities
 * (which can happen before a world, and thus a server config, is loaded).
 */
public final class Config {
    public static final ForgeConfigSpec SPEC;

    public static final ForgeConfigSpec.BooleanValue USE_ENERGY;
    public static final ForgeConfigSpec.IntValue CAPACITY;
    public static final ForgeConfigSpec.IntValue OPEN_USAGE;
    public static final ForgeConfigSpec.IntValue CRAFT_USAGE;
    public static final ForgeConfigSpec.IntValue CLEAR_USAGE;

    static {
        ForgeConfigSpec.Builder builder = new ForgeConfigSpec.Builder();

        builder.push("wirelessUniversalTerminal");
        USE_ENERGY = builder.comment("Whether the Wireless Universal Terminal uses energy")
            .define("useEnergy", true);
        CAPACITY = builder.comment("The energy capacity of the Wireless Universal Terminal")
            .defineInRange("capacity", 32000, 0, Integer.MAX_VALUE);
        OPEN_USAGE = builder.comment("The energy used by the Wireless Universal Terminal to open. Switching between its pages is free")
            .defineInRange("openUsage", 30, 0, Integer.MAX_VALUE);
        CRAFT_USAGE = builder.comment("The energy used by the Wireless Universal Terminal to craft an item")
            .defineInRange("craftUsage", 1, 0, Integer.MAX_VALUE);
        CLEAR_USAGE = builder.comment("The energy used by the Wireless Universal Terminal to clear the crafting matrix")
            .defineInRange("clearUsage", 10, 0, Integer.MAX_VALUE);
        builder.pop();

        SPEC = builder.build();
    }

    private Config() {
    }
}
