package eu.pb4.universalshops.gui.selling;

import eu.pb4.sgui.api.ClickType;
import eu.pb4.universalshops.other.TextUtil;
import eu.pb4.universalshops.other.USUtil;
import eu.pb4.universalshops.registry.TradeShopBlockEntity;
import eu.pb4.universalshops.trade.StockHandler;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

public class CurrencyShopGui extends SingleGenericShopGui {

    @Override
    protected Component getMainText() {
        var stockHandler = (StockHandler.VirtualBalance) this.be.stockHandler;
        return TextUtil.text("Buying for " + stockHandler.getValue());
    }

    public CurrencyShopGui(ServerPlayer player, TradeShopBlockEntity blockEntity) {
        super(player, blockEntity);
    }

    @Override
    protected void buyItem(int i, ClickType clickType, net.minecraft.world.inventory.ClickType slotActionType) {
        var stockHandler  = (StockHandler.VirtualBalance) this.be.stockHandler;

        var stockCount = this.be.stockHandler.getMaxAmount(player);

        if (stockCount != 0){
            var paymentCheck = this.be.priceHandler.payFor(player, true);
            if (paymentCheck.success()){
                if (((int) stockHandler.getOwnerAccount(this.be.owner).balance() >= (int) stockHandler.getValue()) || this.be.isAdmin()){
                    if (!this.be.isAdmin()) stockHandler.getOwnerAccount(this.be.owner).decreaseBalance(stockHandler.getValue());
                    stockHandler.getPlayerAccount(player).increaseBalance(stockHandler.getValue());
                    this.playClickSound();
                    this.updateValueDisplays();
                    this.markDirty();
                } else {
                    this.playDismissSound();
                    this.player.sendSystemMessage(TextUtil.text("virtual_money.not_enough_money_owner").withStyle(ChatFormatting.RED));
                    this.setTempTitle(TextUtil.text("virtual_money.not_enough_money_owner").withStyle(ChatFormatting.DARK_RED));
                }
            } else {
                this.playDismissSound();
                this.player.sendSystemMessage(TextUtil.text("not_enough_currency").withStyle(ChatFormatting.RED));
                this.setTempTitle(TextUtil.text("not_enough_currency").withStyle(ChatFormatting.DARK_RED));
            }
        } else {
            var text = TextUtil.text("not_enough_stock");
            this.player.sendSystemMessage(TextUtil.prefix(text.copy().withStyle(ChatFormatting.RED)));
            this.setTempTitle(text.withStyle(ChatFormatting.DARK_RED));
            this.playDismissSound();
        }
    }
}
