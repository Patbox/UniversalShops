package eu.pb4.universalshops.gui.setup;

import eu.pb4.sgui.api.elements.GuiElementBuilder;
import eu.pb4.universalshops.gui.BaseShopGui;
import eu.pb4.universalshops.gui.GuiBackground;
import eu.pb4.universalshops.gui.GuiElements;
import eu.pb4.universalshops.other.TextUtil;
import eu.pb4.universalshops.registry.TradeShopBlockEntity;
import eu.pb4.universalshops.trade.PriceHandler;
import eu.pb4.universalshops.trade.StockHandler;
import java.util.ArrayList;
import java.util.Locale;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.Items;


public class ShopSettingsGui extends BaseShopGui {
    public ShopSettingsGui(ServerPlayer player, TradeShopBlockEntity be) {
        super(MenuType.GENERIC_9x3, player, be, GuiBackground.SETTINGS);
        this.setMainTitle(TextUtil.gui("shop.settings"));

        this.setSlot(9 * 2 + 3 + 2, GuiElements.BACK);

        this.updateStock();
        this.updatePrice();
        this.updateHologram();
        this.updateHopper();

        if (!hasTexture()) {
            while (this.getFirstEmptySlot() != -1) {
                this.addSlot(GuiElements.FILLER);
            }
        }

        this.open();
    }

    private void updateHopper() {
        var x = new GuiElementBuilder(Items.HOPPER)
                .setName(TextUtil.gui("setup.allow_hoppers", CommonComponents.optionStatus(this.be.allowHoppers).copy().withStyle(ChatFormatting.YELLOW)).withStyle(ChatFormatting.WHITE))
                .addLoreLine(Component.empty())
                .addLoreLine(Component.empty()
                        .append(Component.literal("» ").withStyle(ChatFormatting.DARK_GRAY))
                        .append(TextUtil.gui("setup.click_to_change_mode.1")).withStyle(ChatFormatting.GRAY)
                )
                .addLoreLine(Component.empty()
                        .append(Component.literal("   ").withStyle(ChatFormatting.DARK_GRAY))
                        .append(TextUtil.gui("setup.click_to_change_mode.2")).withStyle(ChatFormatting.GRAY)
                )
                .hideDefaultTooltip()
                .setCallback((a, type, c, d) -> {
                    this.playClickSound();
                    this.be.allowHoppers = !this.be.allowHoppers;
                    this.updateHopper();
                    this.markDirty();
                });

        if (this.be.allowHoppers) {
            x.glow();
        }

        this.setSlot(9 * 2 + 3 + 1, x);
    }

    private void updateHologram() {
        this.setSlot(9 * 2 + 3, new GuiElementBuilder(Items.NAME_TAG)
                .setName(TextUtil.gui("setup.hologram", TextUtil.of("hologram_type", this.be.hologramMode.name().toLowerCase(Locale.ROOT)).withStyle(ChatFormatting.YELLOW)).withStyle(ChatFormatting.WHITE))
                .addLoreLine(Component.empty())
                .addLoreLine(Component.empty()
                        .append(Component.literal("» ").withStyle(ChatFormatting.DARK_GRAY))
                        .append(TextUtil.gui("setup.click_to_change_mode.1")).withStyle(ChatFormatting.GRAY)
                )
                .addLoreLine(Component.empty()
                        .append(Component.literal("   ").withStyle(ChatFormatting.DARK_GRAY))
                        .append(TextUtil.gui("setup.click_to_change_mode.2")).withStyle(ChatFormatting.GRAY)
                )
                .hideDefaultTooltip()
                .setCallback((a, type, c, d) -> {
                    int dir = type.shift ? -1 : 1;
                    this.playClickSound();

                    var size = TradeShopBlockEntity.HologramMode.values().length;
                    this.be.hologramMode = TradeShopBlockEntity.HologramMode.values()[((size + this.be.hologramMode.ordinal() + dir) % size)];
                    this.updateHologram();
                    this.markDirty();
                }));
    }

    private void updatePrice() {
        var handler = this.be.priceHandler;

        var b = GuiElementBuilder.from(handler.definition().icon)
                .setName(TextUtil.gui("setup.price_type", handler.definition().displayName.copy().withStyle(ChatFormatting.YELLOW)).withStyle(ChatFormatting.WHITE));

        if (!handler.canSwitch()) {
            b.addLoreLine(TextUtil.gui("setup.cant_change_pricehandler.1").withStyle(ChatFormatting.RED));
            b.addLoreLine(TextUtil.gui("setup.cant_change_pricehandler.2").withStyle(ChatFormatting.RED));
        }

        this.setSlot(3, b
                .addLoreLine(Component.empty())
                .addLoreLine(Component.empty()
                        .append(Component.literal("» ").withStyle(ChatFormatting.DARK_GRAY))
                        .append(TextUtil.gui("setup.click_to_change_mode.1")).withStyle(ChatFormatting.GRAY)
                )
                .addLoreLine(Component.empty()
                        .append(Component.literal("   ").withStyle(ChatFormatting.DARK_GRAY))
                        .append(TextUtil.gui("setup.click_to_change_mode.2")).withStyle(ChatFormatting.GRAY)
                )
                .hideDefaultTooltip()
                .setCallback((a, type, c, d) -> {
                    if (!handler.canSwitch()) {
                        this.playDismissSound();
                        return;
                    }

                    int dir = type.shift ? -1 : 1;
                    this.playClickSound();

                    var types = new ArrayList<PriceHandler.Definition>();
                    for (var x : PriceHandler.TYPES) {
                        if (x.canUse(player)) {
                            types.add(x);
                        }
                    }


                    var size = types.size();
                    this.be.priceHandler = (handler.definition() != PriceHandler.Invalid.DEFINITION
                            ? types.get((size + types.indexOf(handler.definition()) + dir) % size)
                            : PriceHandler.SingleItem.DEFINITION).createInitial(this.be);
                    this.updatePrice();
                    this.markDirty();
                }));

        this.setSlot(3 + 2, handler.getSetupElement());
    }

    private void updateStock() {
        var handler = this.be.stockHandler;
        var b = GuiElementBuilder.from(handler.definition().icon)
                .setName(TextUtil.gui("setup.stock_type", handler.definition().displayName.copy().withStyle(ChatFormatting.YELLOW)).withStyle(ChatFormatting.WHITE));

        if (!handler.canSwitch()) {
            b.addLoreLine(TextUtil.gui("setup.cant_change_pricehandler.1").withStyle(ChatFormatting.RED));
            b.addLoreLine(TextUtil.gui("setup.cant_change_pricehandler.2").withStyle(ChatFormatting.RED));
        }

        this.setSlot(9 + 3, b
                .addLoreLine(Component.empty())
                .addLoreLine(Component.empty()
                        .append(Component.literal("» ").withStyle(ChatFormatting.DARK_GRAY))
                        .append(TextUtil.gui("setup.click_to_change_mode.1")).withStyle(ChatFormatting.GRAY)
                )
                .addLoreLine(Component.empty()
                        .append(Component.literal("   ").withStyle(ChatFormatting.DARK_GRAY))
                        .append(TextUtil.gui("setup.click_to_change_mode.2")).withStyle(ChatFormatting.GRAY)
                )
                .hideDefaultTooltip()
                .setCallback((a, type, c, d) -> {
                    if (!handler.canSwitch()) {
                        this.playDismissSound();
                        return;
                    }

                    int dir = type.shift ? -1 : 1;
                    this.playClickSound();

                    var types = new ArrayList<StockHandler.Definition>();
                    for (var x : StockHandler.TYPES) {
                        if (x.canUse(player)) {
                            types.add(x);
                        }
                    }

                    var size = types.size();
                    this.be.stockHandler = (handler.definition() != StockHandler.Invalid.DEFINITION
                            ? types.get((size + types.indexOf(handler.definition()) + dir) % size)
                            : StockHandler.SingleItem.DEFINITION
                    ).createInitial(this.be);
                    this.updateStock();
                    this.markDirty();
                })
        );
        this.setSlot(9 + 3 + 2, handler.getSetupElement());
    }
}
