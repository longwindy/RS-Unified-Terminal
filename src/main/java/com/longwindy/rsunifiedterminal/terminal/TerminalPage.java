package com.longwindy.rsunifiedterminal.terminal;

/**
 * The pages of the Wireless Universal Terminal that can be selected with the side buttons.
 * The crafting monitor is not a page: it is reached through its own button and returns to the last page.
 */
public enum TerminalPage {
    CRAFTING,
    PATTERN,
    CRAFTER_MANAGER;

    private static final TerminalPage[] VALUES = values();

    public static TerminalPage byId(int id) {
        return id >= 0 && id < VALUES.length ? VALUES[id] : CRAFTING;
    }

    public int getId() {
        return ordinal();
    }
}
