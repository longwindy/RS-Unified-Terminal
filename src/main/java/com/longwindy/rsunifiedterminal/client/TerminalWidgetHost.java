package com.longwindy.rsunifiedterminal.client;

import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.events.GuiEventListener;

/**
 * Implemented on Refined Storage's grid screen by GridScreenMixin, to add and remove widgets from outside the screen.
 * <p>
 * This is an interface on purpose: lambdas or method references written inside a mixin end up referencing the mixin class
 * itself, which can't be loaded, and that breaks every grid screen when it's created.
 */
public interface TerminalWidgetHost {
    void rsunifiedterminal$addWidget(AbstractWidget widget);

    void rsunifiedterminal$removeWidget(GuiEventListener widget);
}
