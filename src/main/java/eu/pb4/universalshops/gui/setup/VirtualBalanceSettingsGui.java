package eu.pb4.universalshops.gui.setup;

import eu.pb4.common.economy.api.CommonEconomy;
import eu.pb4.common.economy.api.EconomyCurrency;
import eu.pb4.sgui.api.elements.GuiElementBuilder;
import eu.pb4.universalshops.gui.BaseShopGui;
import eu.pb4.universalshops.gui.GuiBackground;
import eu.pb4.universalshops.gui.GuiElements;
import eu.pb4.universalshops.other.TextUtil;
import eu.pb4.universalshops.registry.TradeShopBlockEntity;
import eu.pb4.universalshops.trade.PriceHandler;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.Items;

public class VirtualBalanceSettingsGui extends BaseShopGui {
    private final Controller controller;
    private List<EconomyCurrency> currencies = new ArrayList<>();
    private EconomyCurrency current;

    public VirtualBalanceSettingsGui(ServerPlayer player, TradeShopBlockEntity blockEntity, Controller controller) {
        super(MenuType.HOPPER, player, blockEntity, Component.empty());
        this.controller = controller;
        var id = this.controller.getCurrencyId();
        this.current = id != null ? CommonEconomy.getCurrency(player.level().getServer(), id) : null;
        this.setSlot(1, GuiElements.FILLER);
        this.setSlot(3, GuiElements.FILLER);
        this.setSlot(4, GuiElements.BACK);
        this.setTitle(TextUtil.gui("virtual_balance.settings.title"));
        this.open();
    }

    @Override
    public void beforeOpen() {
        this.updateDynamic();
    }

    private void updateDynamic() {
        this.currencies.clear();

        this.currencies.addAll(CommonEconomy.getCurrencies(this.player.level().getServer()));

        var b = GuiElementBuilder.from((this.current == null ? GuiElements.HEAD_QUESTION_MARK : this.current.icon()).copy())
                .setName(TextUtil.gui("setup.virtual_balance.currency", (this.current == null ? TextUtil.text("not_set") : this.current.name().copy()).withStyle(ChatFormatting.YELLOW)).withStyle(ChatFormatting.WHITE));


        if (!this.be.priceHandler.canSwitch()) {
            b.addLoreLine(TextUtil.gui("setup.cant_change_pricehandler.1").withStyle(ChatFormatting.RED));
            b.addLoreLine(TextUtil.gui("setup.cant_change_pricehandler.2").withStyle(ChatFormatting.RED));
        }

        this.setSlot(0, b
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
                    if (!this.be.priceHandler.canSwitch()) {
                        this.playDismissSound();
                        return;
                    }
                    int dir = type.shift ? -1 : 1;
                    this.playClickSound();

                    var size = this.currencies.size();
                    this.current = this.currencies.get((size + this.currencies.indexOf(this.current) + dir) % size);
                    ((PriceHandler.VirtualBalance) this.getBE().priceHandler).currency = this.current.id();
                    this.updateDynamic();
                    this.markDirty();
                }));

        this.setSlot(2, new GuiElementBuilder(Items.SUNFLOWER)
                .setName(TextUtil.gui("setup.virtual_balance.value", (this.current == null ? TextUtil.text("not_set") : this.current.formatValueText(((PriceHandler.VirtualBalance) this.be.priceHandler).cost, true).copy()).withStyle(ChatFormatting.YELLOW)).withStyle(ChatFormatting.WHITE))
                .hideDefaultTooltip()
                .setCallback((a, type, c, d) -> {
                    if (this.current == null) {
                        this.playDismissSound();
                        return;
                    }
                    this.playClickSound();
                    new VirtualBalanceValueGui(this.player, this.be);
                }));
    }

    public interface Controller {
        void setValue(long value);
        long getValue();

        void setCurrencyId(Identifier identifier);
        Identifier getCurrencyId();
    }
}
