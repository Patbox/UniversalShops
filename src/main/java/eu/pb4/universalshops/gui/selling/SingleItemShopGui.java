package eu.pb4.universalshops.gui.selling;

import eu.pb4.sgui.api.ClickType;
import eu.pb4.universalshops.other.USUtil;
import eu.pb4.universalshops.other.TextUtil;
import eu.pb4.universalshops.registry.TradeShopBlockEntity;
import eu.pb4.universalshops.trade.StockHandler;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.collection.DefaultedList;

public class SingleItemShopGui extends SingleGenericShopGui {
    public SingleItemShopGui(ServerPlayerEntity player, TradeShopBlockEntity blockEntity) {
        super(player, blockEntity);
    }

    @Override
    protected Text getMainText() {
        return USUtil.asText(((StockHandler.SingleItem) this.be.stockHandler).value);
    }

    protected void buyItem(int i, ClickType clickType, SlotActionType slotActionType) {
        var stockHandler = (StockHandler.SingleItem) this.be.stockHandler;

        var stockCount = this.be.stockHandler.getMaxAmount(player);

        if (stockCount != 0) {
            var count = stockHandler.transfer(true,
                    clickType.shift
                            ? USUtil.addToInventory(USUtil.copyInventory(this.player.getInventory().main))
                            : USUtil.addToInventory(DefaultedList.copyOf(ItemStack.EMPTY, this.player.currentScreenHandler.getCursorStack().copy()))
            );

            if (count >= stockHandler.value.getCount()) {
                var paymentCheck = this.be.priceHandler.payFor(player, true);
                if (paymentCheck.success()) {
                    stockHandler.transfer(false, clickType.shift ? USUtil.addToInventory(this.player.getInventory().main) : USUtil.mergeIntoCursor(this.player.currentScreenHandler));
                    this.playClickSound();
                    this.updateValueDisplays();
                    this.markDirty();
                } else {
                    this.playDismissSound();
                    this.player.sendMessage(TextUtil.prefix(Text.empty().append(paymentCheck.failureMessage()).formatted(Formatting.RED)));
                    this.setTempTitle(Text.empty().append(paymentCheck.failureMessage()).formatted(Formatting.DARK_RED));
                }
            } else {
                var text = TextUtil.text(clickType.shift ? "not_enough_inventory_space" : "not_enough_stack_space");
                this.player.sendMessage(TextUtil.prefix(text.copy().formatted(Formatting.RED)));
                this.setTempTitle(text.formatted(Formatting.DARK_RED));
                this.playDismissSound();
            }
        } else {
            var text = TextUtil.text("not_enough_stock");
            this.player.sendMessage(TextUtil.prefix(text.copy().formatted(Formatting.RED)));
            this.setTempTitle(text.formatted(Formatting.DARK_RED));
            this.playDismissSound();
        }
    }

    @Override
    public TradeShopBlockEntity getBE() {
        return this.be;
    }
}
