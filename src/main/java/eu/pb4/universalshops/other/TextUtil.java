package eu.pb4.universalshops.other;

import eu.pb4.universalshops.UniversalShopsMod;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;

public class TextUtil {

    public static MutableComponent of(String type, String path, Object... args) {
        return Component.translatable(type + "." + UniversalShopsMod.MOD_ID + "." + path, args);
    }

    public static MutableComponent text(String path, Object... args) {
        return of("text", path, args);
    }

    public static MutableComponent gui(String path, Object... args) {
        return of("gui", path, args);
    }

    public static MutableComponent number(int value) {
        return Component.literal(value == Integer.MAX_VALUE ? "∞" : Integer.toString(value));
    }

    public static Component prefix(Component text) {
        return Component.empty()
                .append(Component.literal("[").withStyle(ChatFormatting.DARK_GRAY))
                .append(text("prefix").setStyle(Style.EMPTY.withColor(0xfcc488)))
                .append(Component.literal("]").withStyle(ChatFormatting.DARK_GRAY))


                .append(" ").append(text);
    }
}
