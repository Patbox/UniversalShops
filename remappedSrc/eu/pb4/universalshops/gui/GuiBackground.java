package eu.pb4.universalshops.gui;

import eu.pb4.universalshops.UniversalShopsMod;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

public class GuiBackground {
    public static final Text SINGLE_ITEM = of('0');
    public static final Text TAKE_ANY = of('1');
    public static final Text SETTINGS = of('2');
    public static final Text ITEM_SELECT = of('3');
    public static final Text SELECTOR = of('4');
    public static final Text SELECTOR_BIG = of('5');


    private static Text of(char id) {
        return Text.literal(new StringBuffer().append('-').append(id).append('.').toString()).setStyle(Style.EMPTY.withColor(0xFFFFFF).withFont(UniversalShopsMod.id("gui")));
    }

    private static Text ofCenter(char id) {
        return Text.literal(new StringBuffer().append('_').append(id).append('.').toString()).setStyle(Style.EMPTY.withColor(0xFFFFFF).withFont(UniversalShopsMod.id("gui")));
    }
}
