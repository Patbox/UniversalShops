package eu.pb4.universalshops.gui.setup;

import eu.pb4.sgui.api.gui.AnvilInputGui;
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
    private final Runnable closeRunnable;
    private boolean ignore;
    private boolean valid;

    public VirtualBalanceValueGui(ServerPlayerEntity player, TradeShopBlockEntity be, Runnable closeRunnable) {
        super(player, false);
        this.be = be;
        this.closeRunnable = closeRunnable;
        this.handler  = ((PriceHandler.VirtualBalance) be.priceHandler);
        this.setSlot(1, GuiElements.FILLER);
        this.setSlot(2, GuiElements.BACK);
        var x = this.handler.getCurrency().formatValue(this.handler.cost, true);

        this.setDefaultInputValue(x);
        this.onInput(x);
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
            e.printStackTrace();

            if (this.valid) {
                this.setTitle(TextUtil.gui("virtual_balance.cost.invalid_input").formatted(Formatting.RED));
            }

            this.valid = false;
        }
    }

    @Override
    public void close() {
        this.close(closeRunnable != null && !this.ignore);
    }

    @Override
    public void onClose() {
        if (closeRunnable != null && !this.ignore) {
            this.closeRunnable.run();
        }
        super.onClose();
    }

    @Override
    public TradeShopBlockEntity getBE() {
        return this.be;
    }

    @Override
    public void setIgnore(boolean val) {
        this.ignore = val;
    }
}
