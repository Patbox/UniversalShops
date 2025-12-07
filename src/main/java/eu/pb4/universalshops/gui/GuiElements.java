package eu.pb4.universalshops.gui;

import eu.pb4.sgui.api.elements.GuiElement;
import eu.pb4.sgui.api.elements.GuiElementBuilder;
import eu.pb4.universalshops.gui.setup.ShopSettingsGui;
import eu.pb4.universalshops.other.USUtil;
import eu.pb4.universalshops.other.TextUtil;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public class GuiElements {
    public static final GuiElement FILLER = new GuiElementBuilder(Items.WHITE_STAINED_GLASS_PANE).setName(Component.empty()).hideTooltip().build();
    public static final GuiElement SETTINGS = new GuiElementBuilder(Items.REDSTONE).setName(TextUtil.gui("shop.settings")).setCallback(ShopGui::openSettingsCallback).build();
    public static final GuiElement CURRENCY_INVENTORY = new GuiElementBuilder(Items.CHEST).setName(TextUtil.gui("shop.currency_storage")).setCallback(ShopGui::openCurrencyCallback).build();
    public static final GuiElement BACK = new GuiElementBuilder(Items.STRUCTURE_VOID).setName(CommonComponents.GUI_BACK.copy().withStyle(ChatFormatting.WHITE)).setCallback((a, b, c, g) -> {
        USUtil.playUiSound(g.getPlayer(), SoundEvents.UI_BUTTON_CLICK.value());
        if (g instanceof ShopSettingsGui shopSettingsGui) {
            if (shopSettingsGui.be.isFullySetup()) {
                shopSettingsGui.be.openGui(g.getPlayer());
            } else {
                g.close(false);
            }
        } else {
            g.close();
        }
    }).build();

    public static final ItemStack HEAD_QUESTION_MARK = new GuiElementBuilder(Items.PLAYER_HEAD).setName(Component.empty()).setSkullOwner(HeadTextures.GUI_QUESTION_MARK).asStack();
    public static final ItemStack MINUS = new GuiElementBuilder(Items.PLAYER_HEAD).setName(Component.empty()).setSkullOwner(HeadTextures.GUI_MINUS).asStack();
    public static final ItemStack PLUS = new GuiElementBuilder(Items.PLAYER_HEAD).setName(Component.empty()).setSkullOwner(HeadTextures.GUI_ADD).asStack();
    //public static final ItemStack MINUS = new GuiElementBuilder(Items.PLAYER_HEAD).setName(Text.empty()).setSkullOwner("").asStack();

    private static final Style BASE_LORE = Style.EMPTY.applyFormat(ChatFormatting.GRAY).withItalic(false);

    public static GuiElement priceMarker(Component item, List<Component> lore) {
        var b = new GuiElementBuilder(Items.GREEN_STAINED_GLASS_PANE).setName(TextUtil.gui("shop.price_info", Component.empty().append(item).withStyle(ChatFormatting.GRAY)));

        for (var text : lore) {
            b.addLoreLine(Component.empty().append(text).setStyle(BASE_LORE));
        }

        return b.build();
    }

    public static GuiElement itemMarker(Component item, List<Component> lore) {
        var b = new GuiElementBuilder(Items.LIGHT_BLUE_STAINED_GLASS_PANE).setName(TextUtil.gui("shop.stock_info", Component.empty().append(item).withStyle(ChatFormatting.GRAY)));

        for (var text : lore) {
            b.addLoreLine(Component.empty().append(text).setStyle(BASE_LORE));
        }

        return b.build();
    }

    public static GuiElement previousPage(Runnable runnable) {
        return new GuiElementBuilder(Items.PLAYER_HEAD)
                .setName(Component.translatable("spectatorMenu.previous_page").withStyle(ChatFormatting.WHITE))
                .setCallback((a, b, c, d) -> runnable.run())
                .setSkullOwner(HeadTextures.GUI_PREVIOUS_PAGE)
                .build();

    }

    public static GuiElement nextPage(Runnable runnable) {
        return new GuiElementBuilder(Items.PLAYER_HEAD)
                .setName(Component.translatable("spectatorMenu.next_page").withStyle(ChatFormatting.WHITE))
                .setCallback((a, b, c, d) -> runnable.run())
                .setSkullOwner(HeadTextures.GUI_NEXT_PAGE)
                .build();

    }

}
