package eu.pb4.universalshops.gui.selling;

import eu.pb4.sgui.api.ClickType;
import eu.pb4.universalshops.other.USUtil;
import eu.pb4.universalshops.other.TextUtil;
import eu.pb4.universalshops.registry.TradeShopBlockEntity;
import eu.pb4.universalshops.trade.StockHandler;
import net.minecraft.ChatFormatting;
import net.minecraft.core.NonNullList;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

public class SingleItemShopGui extends SingleGenericShopGui {
    public SingleItemShopGui(ServerPlayer player, TradeShopBlockEntity blockEntity) {
        super(player, blockEntity);
    }

    @Override
    protected Component getMainText() {
        return USUtil.asText(((StockHandler.SingleItem) this.be.stockHandler).value);
    }

    protected void buyItem(int i, ClickType clickType, net.minecraft.world.inventory.ClickType slotActionType) {
        var stockHandler = (StockHandler.SingleItem) this.be.stockHandler;

        var stockCount = this.be.stockHandler.getMaxAmount(player);

        if (stockCount != 0) {
            var count = stockHandler.transfer(true,
                    clickType.shift
                            ? USUtil.addToInventory(USUtil.copyInventory(this.player.getInventory().getNonEquipmentItems()))
                            : USUtil.addToInventory(NonNullList.of(ItemStack.EMPTY, this.player.containerMenu.getCarried().copy()))
            );

            if (count >= stockHandler.value.getCount()) {
                var paymentCheck = this.be.priceHandler.payFor(player, true);
                if (paymentCheck.success()) {
                    stockHandler.transfer(false, clickType.shift ? USUtil.addToInventory(this.player.getInventory().getNonEquipmentItems()) : USUtil.mergeIntoCursor(this.player.containerMenu));
                    this.playClickSound();
                    this.updateValueDisplays();
                    this.markDirty();
                } else {
                    this.playDismissSound();
                    this.player.sendSystemMessage(TextUtil.prefix(Component.empty().append(paymentCheck.failureMessage()).withStyle(ChatFormatting.RED)));
                    this.setTempTitle(Component.empty().append(paymentCheck.failureMessage()).withStyle(ChatFormatting.DARK_RED));
                }
            } else {
                var text = TextUtil.text(clickType.shift ? "not_enough_inventory_space" : "not_enough_stack_space");
                this.player.sendSystemMessage(TextUtil.prefix(text.copy().withStyle(ChatFormatting.RED)));
                this.setTempTitle(text.withStyle(ChatFormatting.DARK_RED));
                this.playDismissSound();
            }
        } else {
            var text = TextUtil.text("not_enough_stock");
            this.player.sendSystemMessage(TextUtil.prefix(text.copy().withStyle(ChatFormatting.RED)));
            this.setTempTitle(text.withStyle(ChatFormatting.DARK_RED));
            this.playDismissSound();
        }
    }

    @Override
    public TradeShopBlockEntity getBE() {
        return this.be;
    }
}
