package eu.pb4.universalshops.gui.selling;

import eu.pb4.sgui.api.ClickType;
import eu.pb4.sgui.api.elements.AnimatedGuiElement;
import eu.pb4.sgui.api.elements.GuiElement;
import eu.pb4.sgui.api.elements.GuiElementBuilder;
import eu.pb4.universalshops.gui.BaseShopGui;
import eu.pb4.universalshops.gui.GuiElements;
import eu.pb4.universalshops.other.TextUtil;
import eu.pb4.universalshops.registry.TradeShopBlockEntity;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.List;

public abstract class SingleGenericShopGui extends BaseShopGui {
    private int tick = 0;
    private int maxStockCount;
    private int stockCount;

    public SingleGenericShopGui(ServerPlayerEntity player, TradeShopBlockEntity blockEntity, Runnable onClose) {
        super(ScreenHandlerType.GENERIC_9X3, player, blockEntity, onClose);

        for (int i = 0; i < this.getSize(); i++) {
            this.setSlot(i, GuiElements.FILLER);
        }

        if (blockEntity.isOwner(player)) {
            this.setSlot(3 * 9 - 1, GuiElements.SETTINGS);
        }
        this.maxStockCount = this.be.priceHandler.getMaxAmount(this.player);
        this.stockCount = this.be.stockHandler.getMaxAmount(player);

        this.setMainTitle(blockEntity.getTitle());

        this.updateValueDisplays();
        this.open();
    }

    @Override
    public void onTick() {
        if (++this.tick % 20 == 0) {
            var maxStockCount = this.be.priceHandler.getMaxAmount(this.player);
            var stockCount = this.be.stockHandler.getMaxAmount(player);

            if (maxStockCount != this.maxStockCount || stockCount != this.stockCount) {
                this.maxStockCount = maxStockCount;
                this.stockCount = stockCount;
                this.updateValueDisplays();
            }
        }

        super.onTick();
    }

    protected void updateValueDisplays() {
        if (this.be.isOwner(this.player)) {
            this.setSlot(2 * 9, this.be.priceHandler.getAccessElement());
        }
        this.setSlot(0 * 9 + 2, this.be.priceHandler.getUserElement());

        {
            var canBuy = maxStockCount > 0;
            var item = this.be.priceHandler.icon();

            ItemStack secondStack = item;
            if (!canBuy) {
                List<Text> tooltip;

                try {
                    tooltip = item.getTooltip(this.player, TooltipContext.Default.NORMAL);
                } catch (Throwable e) {
                    tooltip = List.of(item.getName());
                }

                secondStack = new GuiElementBuilder(Items.BARRIER).setName(tooltip.remove(0)).setLore(tooltip).asStack();
            }

            this.setSlot(1 * 9 + 2, new AnimatedGuiElement(new ItemStack[]{
                    item,
                    secondStack
            }, 12, false, GuiElement.EMPTY_CALLBACK));

            this.setSlot(2 * 9 + 2, GuiElements.priceMarker(this.be.priceHandler.getText(),
                    List.of(
                            Text.empty(),
                            TextUtil.gui("max_stock_left", TextUtil.number(maxStockCount).formatted(Formatting.WHITE)).formatted(Formatting.YELLOW)
                    )));
        }

        {
            var stock = this.be.stockHandler;
            var hasStock = stockCount > 0;
            var item = stock.icon();

            ItemStack secondStack = item;
            if (!hasStock) {
                List<Text> tooltip;

                try {
                    tooltip = item.getTooltip(this.player, TooltipContext.Default.NORMAL);
                } catch (Throwable e) {
                    tooltip = List.of(item.getName());
                }

                secondStack = new GuiElementBuilder(Items.BARRIER).setName(tooltip.remove(0)).setLore(tooltip).asStack();
            }

            this.setSlot(1 * 9 + 6, new AnimatedGuiElement(new ItemStack[]{
                    stock.icon(),
                    secondStack
            }, 12, false, this::buyItem));
            this.setSlot(2 * 9 + 6, GuiElements.itemMarker(this.getMainText(), List.of(
                    Text.empty(),
                    TextUtil.gui("stock_left", TextUtil.number(stockCount).formatted(Formatting.WHITE)).formatted(Formatting.YELLOW),
                    Text.empty(),
                    hasStock ? TextUtil.gui("click_to_buy").formatted(Formatting.GREEN) : TextUtil.gui("out_of_stock").formatted(Formatting.RED)
            )));
        }
    }

    protected abstract Text getMainText();

    protected abstract void buyItem(int i, ClickType clickType, SlotActionType slotActionType);

    @Override
    public TradeShopBlockEntity getBE() {
        return this.be;
    }
}
