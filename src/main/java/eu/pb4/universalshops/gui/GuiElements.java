package eu.pb4.universalshops.gui;

import eu.pb4.sgui.api.elements.GuiElement;
import eu.pb4.sgui.api.elements.GuiElementBuilder;
import eu.pb4.universalshops.other.USUtil;
import eu.pb4.universalshops.other.TextUtil;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.List;

public class GuiElements {
    public static final GuiElement FILLER = new GuiElementBuilder(Items.WHITE_STAINED_GLASS_PANE).setName(Text.empty()).build();
    public static final GuiElement SETTINGS = new GuiElementBuilder(Items.REDSTONE).setName(TextUtil.gui("shop.settings")).setCallback(ShopGui::openSettingsCallback).build();
    public static final GuiElement CURRENCY_INVENTORY = new GuiElementBuilder(Items.CHEST).setName(TextUtil.gui("shop.currency_storage")).setCallback(ShopGui::openCurrencyCallback).build();
    public static final GuiElement BACK = new GuiElementBuilder(Items.STRUCTURE_VOID).setName(ScreenTexts.BACK.copy().formatted(Formatting.WHITE)).setCallback((a, b, c, g) -> {
        USUtil.playUiSound(g.getPlayer(), SoundEvents.UI_BUTTON_CLICK.value());
        g.close();
    }).build();


    public static final ItemStack HEAD_QUESTION_MARK = new GuiElementBuilder(Items.PLAYER_HEAD).setName(Text.empty()).setSkullOwner(HeadTextures.GUI_QUESTION_MARK).asStack();
    public static final ItemStack MINUS = new GuiElementBuilder(Items.PLAYER_HEAD).setName(Text.empty()).setSkullOwner(HeadTextures.GUI_MINUS).asStack();
    public static final ItemStack PLUS = new GuiElementBuilder(Items.PLAYER_HEAD).setName(Text.empty()).setSkullOwner(HeadTextures.GUI_ADD).asStack();
    //public static final ItemStack MINUS = new GuiElementBuilder(Items.PLAYER_HEAD).setName(Text.empty()).setSkullOwner("").asStack();

    private static final Style BASE_LORE = Style.EMPTY.withFormatting(Formatting.GRAY).withItalic(false);

    public static GuiElement priceMarker(Text item, List<Text> lore) {
        var b = new GuiElementBuilder(Items.GREEN_STAINED_GLASS_PANE).setName(TextUtil.gui("shop.price_info", Text.empty().append(item).formatted(Formatting.GRAY)));

        for (var text : lore) {
            b.addLoreLine(Text.empty().append(text).setStyle(BASE_LORE));
        }

        return b.build();
    }

    public static GuiElement itemMarker(Text item, List<Text> lore) {
        var b = new GuiElementBuilder(Items.LIGHT_BLUE_STAINED_GLASS_PANE).setName(TextUtil.gui("shop.stock_info", Text.empty().append(item).formatted(Formatting.GRAY)));

        for (var text : lore) {
            b.addLoreLine(Text.empty().append(text).setStyle(BASE_LORE));
        }

        return b.build();
    }

    public static GuiElement previousPage(Runnable runnable) {
        return new GuiElementBuilder(Items.PLAYER_HEAD)
                .setName(Text.translatable("spectatorMenu.previous_page").formatted(Formatting.WHITE))
                .setCallback((a, b, c, d) -> runnable.run())
                .setSkullOwner(HeadTextures.GUI_PREVIOUS_PAGE)
                .build();

    }

    public static GuiElement nextPage(Runnable runnable) {
        return new GuiElementBuilder(Items.PLAYER_HEAD)
                .setName(Text.translatable("spectatorMenu.next_page").formatted(Formatting.WHITE))
                .setCallback((a, b, c, d) -> runnable.run())
                .setSkullOwner(HeadTextures.GUI_NEXT_PAGE)
                .build();

    }
}
