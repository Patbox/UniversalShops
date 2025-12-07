package eu.pb4.universalshops.gui.selling;

import eu.pb4.sgui.api.ClickType;
import eu.pb4.sgui.api.elements.AnimatedGuiElement;
import eu.pb4.sgui.api.elements.GuiElement;
import eu.pb4.sgui.api.elements.GuiElementBuilder;
import eu.pb4.universalshops.gui.BaseShopGui;
import eu.pb4.universalshops.gui.GuiBackground;
import eu.pb4.universalshops.gui.GuiElements;
import eu.pb4.universalshops.other.TextUtil;
import eu.pb4.universalshops.registry.TradeShopBlockEntity;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.TooltipFlag;

public abstract class SingleGenericShopGui extends BaseShopGui {
    private int tick = 0;
    private int maxStockCount;
    private int stockCount;

    public SingleGenericShopGui(ServerPlayer player, TradeShopBlockEntity blockEntity) {
        super(MenuType.GENERIC_9x3, player, blockEntity, GuiBackground.SINGLE_ITEM);

        if (!hasTexture()) {
            for (int i = 0; i < this.getSize(); i++) {
                this.setSlot(i, GuiElements.FILLER);
            }
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
            var x = this.be.priceHandler.getAccessElement();

            this.setSlot(2 * 9, hasTexture() && GuiElements.FILLER == x ? GuiElement.EMPTY : x);
        }
        var x = this.be.priceHandler.getUserElement();

        this.setSlot(0 * 9 + 2, hasTexture() && GuiElements.FILLER == x ? GuiElement.EMPTY : x);

        {
            var canBuy = maxStockCount > 0;
            var item = this.be.priceHandler.icon();

            ItemStack secondStack = item;
            if (!canBuy) {
                List<Component> tooltip;

                try {
                    tooltip = item.getTooltipLines(Item.TooltipContext.of(this.player.level()), this.player, TooltipFlag.Default.NORMAL);
                } catch (Throwable e) {
                    tooltip = List.of(item.getHoverName());
                }

                secondStack = new GuiElementBuilder(Items.BARRIER).setName(tooltip.remove(0)).setLore(tooltip).asStack();
            }

            this.setSlot(1 * 9 + 2, new AnimatedGuiElement(new ItemStack[]{
                    item,
                    secondStack
            }, 12, false, GuiElement.EMPTY_CALLBACK));

            this.setSlot(2 * 9 + 2, GuiElements.priceMarker(this.be.priceHandler.getText(),
                    List.of(
                            Component.empty(),
                            TextUtil.gui("max_stock_left", TextUtil.number(maxStockCount).withStyle(ChatFormatting.WHITE)).withStyle(ChatFormatting.YELLOW)
                    )));
        }

        {
            var stock = this.be.stockHandler;
            var hasStock = stockCount > 0;
            var item = stock.icon();

            ItemStack secondStack = item;
            if (!hasStock) {
                List<Component> tooltip;

                try {
                    tooltip = item.getTooltipLines(Item.TooltipContext.of(this.player.level()), this.player, TooltipFlag.Default.NORMAL);
                } catch (Throwable e) {
                    tooltip = new ArrayList<>();
                    tooltip.add(item.getHoverName());
                }

                secondStack = new GuiElementBuilder(Items.BARRIER).setName(tooltip.remove(0)).setLore(tooltip).asStack();
            }

            this.setSlot(1 * 9 + 6, new AnimatedGuiElement(new ItemStack[]{
                    stock.icon(),
                    secondStack
            }, 12, false, this::buyItem));
            this.setSlot(2 * 9 + 6, GuiElements.itemMarker(this.getMainText(), List.of(
                    Component.empty(),
                    TextUtil.gui("stock_left", TextUtil.number(stockCount).withStyle(ChatFormatting.WHITE)).withStyle(ChatFormatting.YELLOW),
                    Component.empty(),
                    hasStock ? TextUtil.gui("click_to_buy").withStyle(ChatFormatting.GREEN) : TextUtil.gui("out_of_stock").withStyle(ChatFormatting.RED)
            )));
        }
    }

    protected abstract Component getMainText();

    protected abstract void buyItem(int i, ClickType clickType, net.minecraft.world.inventory.ClickType slotActionType);

    @Override
    public TradeShopBlockEntity getBE() {
        return this.be;
    }
}
