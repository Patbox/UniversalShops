package eu.pb4.universalshops.gui.setup;

import eu.pb4.sgui.api.GuiHelpers;
import eu.pb4.sgui.api.gui.AnvilInputGui;
import eu.pb4.sgui.api.gui.GuiInterface;
import eu.pb4.universalshops.gui.GuiElements;
import eu.pb4.universalshops.gui.ShopGui;
import eu.pb4.universalshops.other.TextUtil;
import eu.pb4.universalshops.registry.TradeShopBlockEntity;
import eu.pb4.universalshops.trade.PriceHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Formatting;

public class VirtualBalanceValueGui extends AnvilInputGui implements ShopGui {
    private final PriceHandler.VirtualBalance handler;
    private final TradeShopBlockEntity be;
    private final GuiInterface previousGui;
    private boolean valid;

    public VirtualBalanceValueGui(ServerPlayerEntity player, TradeShopBlockEntity be) {
        super(player, false);
        this.be = be;
        this.handler  = ((PriceHandler.VirtualBalance) be.priceHandler);
        this.setSlot(1, GuiElements.FILLER);
        this.setSlot(2, GuiElements.BACK);
        var x = this.handler.getCurrency().formatValue(this.handler.cost, true);

        this.setDefaultInputValue(x);
        this.onInput(x);
        this.previousGui = GuiHelpers.getCurrentGui(player);
        this.open();
    }

    @Override
    public void onInput(String input) {
        this.setDefaultInputValue(input);

        try {
            this.handler.cost = this.handler.getCurrency().parseValue(input);
            this.markDirty();
            if (!this.valid) {
                this.setTitle(TextUtil.gui("virtual_balance.cost.title"));
            }
            this.valid = true;
        } catch (Throwable e) {
            if (!(e instanceof NumberFormatException)) {
                e.printStackTrace();
            }

            if (this.valid) {
                this.setTitle(TextUtil.gui("virtual_balance.cost.invalid_input").formatted(Formatting.RED));
            }

            this.valid = false;
        }
    }

    @Override
    public void close() {
        if (this.previousGui != null) {
            this.previousGui.open();
        } else {
            super.close();
        }
    }

    @Override
    public TradeShopBlockEntity getBE() {
        return this.be;
    }
}
