package com.longwindy.rsunifiedterminal;

import net.minecraftforge.common.ForgeConfigSpec;

/**
 * Client config: preferences that only affect the player's own screens.
 */
public final class ClientConfig {
    public static final ForgeConfigSpec SPEC;

    public static final ForgeConfigSpec.BooleanValue KEEP_SEARCH_ON_PAGE_SWITCH;

    static {
        ForgeConfigSpec.Builder builder = new ForgeConfigSpec.Builder();

        builder.push("wirelessUniversalTerminal");
        KEEP_SEARCH_ON_PAGE_SWITCH = builder.comment(
                "Whether the search text is kept when switching between the pages of the Wireless Universal Terminal",
                "(and synchronized to JEI again if the search box mode does so). It is cleared when the terminal is closed.",
                "Refined Storage's own rememberSearchQuery client setting (keep it after closing and reopening)",
                "applies to the crafting and pattern pages only, not to the crafter manager page.")
            .define("keepSearchOnPageSwitch", true);
        builder.pop();

        SPEC = builder.build();
    }

    private ClientConfig() {
    }
}
