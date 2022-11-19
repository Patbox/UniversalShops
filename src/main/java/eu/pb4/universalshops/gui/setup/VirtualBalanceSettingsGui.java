package eu.pb4.universalshops.gui.setup;

import eu.pb4.common.economy.api.CommonEconomy;
import eu.pb4.common.economy.api.EconomyCurrency;
import eu.pb4.sgui.api.elements.GuiElementBuilder;
import eu.pb4.universalshops.gui.BaseShopGui;
import eu.pb4.universalshops.gui.GuiElements;
import eu.pb4.universalshops.other.TextUtil;
import eu.pb4.universalshops.registry.TradeShopBlockEntity;
import eu.pb4.universalshops.trade.PriceHandler;
import net.minecraft.item.Items;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class VirtualBalanceSettingsGui extends BaseShopGui {
    private final Controller controller;
    private List<EconomyCurrency> currencies = new ArrayList<>();
    private EconomyCurrency current;

    public VirtualBalanceSettingsGui(ServerPlayerEntity player, TradeShopBlockEntity blockEntity, Controller controller, @Nullable Runnable onClose) {
        super(ScreenHandlerType.HOPPER, player, blockEntity, onClose);
        this.controller = controller;
        var id = this.controller.getCurrencyId();
        this.current = id != null ? CommonEconomy.getCurrency(player.server, id) : null;
        this.setSlot(1, GuiElements.FILLER);
        this.setSlot(3, GuiElements.FILLER);
        this.setSlot(4, GuiElements.BACK);
        this.setTitle(TextUtil.gui("virtual_balance.settings.title"));

        this.updateDynamic();

        this.open();
    }

    private void updateDynamic() {
        this.currencies.clear();

        for (var ac : CommonEconomy.getCurrencies(this.player.server)) {
            this.currencies.add(ac);
        }


        var b = GuiElementBuilder.from((this.current == null ? GuiElements.HEAD_QUESTION_MARK : this.current.icon()).copy())
                .setName(TextUtil.gui("setup.virtual_balance.currency", (this.current == null ? TextUtil.text("not_set") : this.current.name().copy()).formatted(Formatting.YELLOW)).formatted(Formatting.WHITE));


        if (!this.be.priceHandler.canSwitch()) {
            b.addLoreLine(TextUtil.gui("setup.cant_change_pricehandler.1").formatted(Formatting.RED));
            b.addLoreLine(TextUtil.gui("setup.cant_change_pricehandler.2").formatted(Formatting.RED));
        }

        this.setSlot(0, b
                .addLoreLine(Text.empty())
                .addLoreLine(Text.empty()
                        .append(Text.literal("» ").formatted(Formatting.DARK_GRAY))
                        .append(TextUtil.gui("setup.click_to_change_mode.1")).formatted(Formatting.GRAY)
                )
                .addLoreLine(Text.empty()
                        .append(Text.literal("   ").formatted(Formatting.DARK_GRAY))
                        .append(TextUtil.gui("setup.click_to_change_mode.2")).formatted(Formatting.GRAY)
                )
                .hideFlags()
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
                .setName(TextUtil.gui("setup.virtual_balance.value", (this.current == null ? TextUtil.text("not_set") : this.current.formatValueText(((PriceHandler.VirtualBalance) this.be.priceHandler).cost, true).copy()).formatted(Formatting.YELLOW)).formatted(Formatting.WHITE))
                .hideFlags()
                .setCallback((a, type, c, d) -> {
                    if (this.current == null) {
                        this.playDismissSound();
                        return;
                    }
                    this.playClickSound();
                    this.setIgnore(true);
                    this.close(true);
                    new VirtualBalanceValueGui(this.player, this.be, this::updateAndOpen);
                    this.setIgnore(false);
                }));
    }

    private void updateAndOpen() {
        this.updateDynamic();
        this.open();
    }

    public interface Controller {
        void setValue(long value);
        long getValue();

        void setCurrencyId(Identifier identifier);
        Identifier getCurrencyId();
    }
}
