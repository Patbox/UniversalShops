package eu.pb4.universalshops.gui.setup;

import eu.pb4.sgui.api.GuiHelpers;
import eu.pb4.sgui.api.gui.AnvilInputGui;
import eu.pb4.sgui.api.gui.GuiInterface;
import eu.pb4.universalshops.gui.GuiElements;
import eu.pb4.universalshops.gui.ShopGui;
import eu.pb4.universalshops.other.TextUtil;
import eu.pb4.universalshops.registry.TradeShopBlockEntity;
import eu.pb4.universalshops.trade.PriceHandler;
import eu.pb4.universalshops.trade.StockHandler;
import net.minecraft.ChatFormatting;
import net.minecraft.server.level.ServerPlayer;

public class VirtualBalanceValueGui extends AnvilInputGui implements ShopGui {
    private PriceHandler.VirtualBalance priceHandler = null;
    private StockHandler.VirtualBalance stockHandler = null;
    private final TradeShopBlockEntity be;
    private final GuiInterface previousGui;
    private boolean valid;
    private boolean price;
    private String x;

    public VirtualBalanceValueGui(ServerPlayer player, TradeShopBlockEntity be, boolean price) {
        super(player, false);
        this.be = be;
        this.price = price;
        if (price) {
            this.priceHandler = ((PriceHandler.VirtualBalance) be.priceHandler);
            this.x = this.priceHandler.getCurrency().formatValue(this.priceHandler.cost, true);
        } else {
            this.stockHandler = ((StockHandler.VirtualBalance) be.stockHandler);
            this.x = this.stockHandler.getCurrency().formatValue(this.stockHandler.value, true);
        }
        this.setSlot(1, GuiElements.FILLER);
        this.setSlot(2, GuiElements.BACK);
        this.setDefaultInputValue(x);
        this.onInput(x);
        this.previousGui = GuiHelpers.getCurrentGui(player);
        this.open();
    }


    @Override
    public void onInput(String input) {
        this.setDefaultInputValue(input);

        try {
            if (price) {
                this.priceHandler.cost = this.priceHandler.getCurrency().parseValue(input);
            } else {
                this.stockHandler.value = this.stockHandler.getCurrency().parseValue(input);
            }
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
                this.setTitle(TextUtil.gui("virtual_balance.cost.invalid_input").withStyle(ChatFormatting.RED));
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
