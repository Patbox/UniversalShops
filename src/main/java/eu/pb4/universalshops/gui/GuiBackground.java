package eu.pb4.universalshops.gui;

import eu.pb4.universalshops.UniversalShopsMod;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FontDescription;
import net.minecraft.network.chat.Style;

public class GuiBackground {
    public static final Component SINGLE_ITEM = of('0');
    public static final Component TAKE_ANY = of('1');
    public static final Component SETTINGS = of('2');
    public static final Component ITEM_SELECT = of('3');
    public static final Component SELECTOR = of('4');
    public static final Component SELECTOR_BIG = of('5');


    private static Component of(char id) {
        return Component.literal(new StringBuffer().append('-').append(id).append('.').toString()).setStyle(Style.EMPTY.withColor(0xFFFFFF).withFont(new FontDescription.Resource(UniversalShopsMod.id("gui"))));
    }

    private static Component ofCenter(char id) {
        return Component.literal(new StringBuffer().append('_').append(id).append('.').toString()).setStyle(Style.EMPTY.withColor(0xFFFFFF).withFont(new FontDescription.Resource(UniversalShopsMod.id("gui"))));
    }
}
