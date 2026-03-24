package eu.pb4.universalshops.gui;

import eu.pb4.sgui.api.elements.GuiElement;
import eu.pb4.sgui.api.elements.GuiElementBuilder;
import eu.pb4.sgui.api.elements.GuiElementBuilderCreator;
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
    public static final GuiElementBuilderCreator<?> FILLER = new GuiElementBuilder(Items.WHITE_STAINED_GLASS_PANE).setName(Component.empty()).hideTooltip();
    public static final GuiElementBuilderCreator<?> SETTINGS = new GuiElementBuilder(Items.REDSTONE).setName(TextUtil.gui("shop.settings")).setCallback(ShopGui::openSettingsCallback);
    public static final GuiElementBuilderCreator<?> CURRENCY_INVENTORY = new GuiElementBuilder(Items.CHEST).setName(TextUtil.gui("shop.currency_storage")).setCallback(ShopGui::openCurrencyCallback);
    public static final GuiElementBuilderCreator<?> BACK = new GuiElementBuilder(Items.STRUCTURE_VOID).setName(CommonComponents.GUI_BACK.copy().withStyle(ChatFormatting.WHITE)).setCallback((a, b, c, g) -> {
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
    });

    public static final GuiElementBuilder HEAD_QUESTION_MARK = new GuiElementBuilder(Items.PLAYER_HEAD).setName(Component.empty()).setProfileSkinTexture(HeadTextures.GUI_QUESTION_MARK);
    public static final GuiElementBuilder MINUS = new GuiElementBuilder(Items.PLAYER_HEAD).setName(Component.empty()).setProfileSkinTexture(HeadTextures.GUI_MINUS);
    public static final GuiElementBuilder PLUS = new GuiElementBuilder(Items.PLAYER_HEAD).setName(Component.empty()).setProfileSkinTexture(HeadTextures.GUI_ADD);
    //public static final ItemStack MINUS = new GuiElementBuilder(Items.PLAYER_HEAD).setName(Text.empty()).setProfileSkinTexture("").asStack();

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
                .setProfileSkinTexture(HeadTextures.GUI_PREVIOUS_PAGE)
                .build();

    }

    public static GuiElement nextPage(Runnable runnable) {
        return new GuiElementBuilder(Items.PLAYER_HEAD)
                .setName(Component.translatable("spectatorMenu.next_page").withStyle(ChatFormatting.WHITE))
                .setCallback((a, b, c, d) -> runnable.run())
                .setProfileSkinTexture(HeadTextures.GUI_NEXT_PAGE)
                .build();

    }

}
