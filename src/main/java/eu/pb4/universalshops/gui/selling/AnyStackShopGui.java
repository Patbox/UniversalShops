package eu.pb4.universalshops.gui.selling;

import eu.pb4.sgui.api.elements.AnimatedGuiElement;
import eu.pb4.sgui.api.elements.GuiElement;
import eu.pb4.sgui.api.elements.GuiElementBuilder;
import eu.pb4.sgui.api.elements.GuiElementInterface;
import eu.pb4.universalshops.gui.BaseShopGui;
import eu.pb4.universalshops.gui.GuiBackground;
import eu.pb4.universalshops.gui.GuiElements;
import eu.pb4.universalshops.other.USUtil;
import eu.pb4.universalshops.other.TextUtil;
import eu.pb4.universalshops.registry.TradeShopBlockEntity;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.ArrayList;
import java.util.List;

public class AnyStackShopGui extends BaseShopGui {
    private int tick = 0;
    private int maxStockCount;
    private int stockCount;
    private List<BuyElement> items = new ArrayList<>();
    private int page;
    private int lastBought;

    public AnyStackShopGui(ServerPlayerEntity player, TradeShopBlockEntity blockEntity) {
        super(ScreenHandlerType.GENERIC_9X6, player, blockEntity, GuiBackground.TAKE_ANY);

        if (!hasTexture()) {
            for (int i = 0; i < 3; i++) {
                for (int i2 = 0; i2 < 6; i2++) {
                    this.setSlot(i + i2 * 9, GuiElements.FILLER);
                }
            }
        }

        if (blockEntity.isOwner(player)) {
            this.setSlot(4 * 9 + 2, GuiElements.SETTINGS);

        }
        this.maxStockCount = this.be.priceHandler.getMaxAmount(this.player);
        this.stockCount = this.be.stockHandler.getMaxAmount(this.player);

        this.setMainTitle(this.getBE().getTitle());

        this.updateValueDisplays();
        this.open();
    }

    @Override
    public void onTick() {
        if (++this.tick % 20 == 0) {
            var maxStockCount = this.be.priceHandler.getMaxAmount(this.player);
            var stockCount = this.be.stockHandler.getMaxAmount(this.player);

            if (maxStockCount != this.maxStockCount || stockCount != this.stockCount) {
                this.maxStockCount = maxStockCount;
                this.stockCount = stockCount;
                this.updateValueDisplays();
            }
        }

        var inv = this.be.getContainer();
        var s = inv.size();
        for (var e : this.items) {
            if (s < e.slot || e.itemStack != inv.getStack(e.slot)) {
                this.updateItems();
                break;
            }
        }

        super.onTick();
    }

    private void updateValueDisplays() {
        if (this.be.isOwner(this.player)) {
            var x = this.be.priceHandler.getAccessElement();
            this.setSlot(4 * 9, hasTexture() && GuiElements.FILLER == x ? GuiElement.EMPTY : x);
        }
        var x = this.be.priceHandler.getUserElement();
        this.setSlot(1 * 9, hasTexture() && GuiElements.FILLER == x ? GuiElement.EMPTY : x);

        {
            var canBuy = maxStockCount > 0;
            var item = this.be.priceHandler.icon();

            ItemStack secondStack = item;
            if (!canBuy) {
                List<Text> tooltip;

                try {
                    tooltip = item.getTooltip(Item.TooltipContext.create(this.player.getWorld()), this.player, TooltipType.Default.BASIC);
                } catch (Throwable e) {
                    tooltip = List.of(item.getName());
                }

                secondStack = new GuiElementBuilder(Items.BARRIER).setName(tooltip.remove(0)).setLore(tooltip).asStack();
            }

            this.setSlot(1 * 9 + 1, new AnimatedGuiElement(new ItemStack[]{
                    item,
                    secondStack
            }, 12, false, GuiElement.EMPTY_CALLBACK));

            this.setSlot(2 * 9 + 1, GuiElements.priceMarker(this.be.priceHandler.getText(), List.of(
                    Text.empty(),
                    TextUtil.gui("max_stock_left", TextUtil.number(maxStockCount).formatted(Formatting.WHITE)).formatted(Formatting.YELLOW)
                    )));
        }

        this.updateItems();
    }

    private void updateItems() {
        this.items.clear();
        var inv = this.be.getContainer();
        for (var i = 0; i < inv.size(); i++) {
            var stack = inv.getStack(i);
            if (!stack.isEmpty()) {
                this.items.add(new BuyElement(stack, i));
            }
        }

        this.updateItemsVisual();
    }

    private void updateItemsVisual() {
        if (this.page * 36 > this.items.size()) {
            this.page = (int) Math.ceil(this.items.size() / 36d);
        }


        for (int x = 0; x < 6; x++) {
            for (int y = 0; y < 6; y++) {
                var i = this.page * 36 + x + y * 6;
                this.setSlot(x + y * 9 + 3, i < this.items.size() ? this.items.get(i) : GuiElement.EMPTY);
            }
        }


        if (this.items.size() > 36) {
            this.setSlot(5 * 9, GuiElements.previousPage(() -> {
                this.playClickSound();
                var pages = (int) Math.ceil(this.items.size() / 36d);
                this.page = (pages + this.page - 1) % pages;
                this.updateItemsVisual();
            }));

            this.setSlot(5 * 9 + 2, GuiElements.nextPage(() -> {
                this.playClickSound();
                var pages = (int) Math.ceil(this.items.size() / 36d);
                this.page = (this.page + 1) % pages;
                this.updateItemsVisual();
            }));
        }
    }

    private record BuyElement(ItemStack itemStack, int slot) implements GuiElementInterface {
        @Override
        public ItemStack getItemStack() {
            return this.itemStack;
        }

        @Override
        public ClickCallback getGuiCallback() {
            return (a, clickType, c, d) -> {
                var gui = (AnyStackShopGui) d;
                var admin = gui.getBE().isAdmin();
                if (gui.tick == gui.lastBought) {
                    return;
                }
                gui.lastBought = gui.tick;

                if (admin || gui.getBE().getContainer().getStack(this.slot) == this.itemStack) {
                    var count = USUtil.canInsert(clickType.shift
                            ? USUtil.copyInventory(gui.getPlayer().getInventory().main)
                            : new SimpleInventory(gui.getPlayer().currentScreenHandler.getCursorStack().copy()), this.itemStack, this.itemStack.getCount());

                    if (count) {
                        var paymentCheck = gui.be.priceHandler.payFor(gui.player, true);
                        if (paymentCheck.success()) {
                            (clickType.shift ? USUtil.addToInventory(gui.player.getInventory().main) : USUtil.mergeIntoCursor(gui.player.currentScreenHandler)).test(this.itemStack.copy());
                            if (!admin) {
                                this.itemStack.setCount(0);
                                gui.be.getContainer().setStack(this.slot, ItemStack.EMPTY);
                            }
                            gui.playClickSound();
                            gui.updateValueDisplays();
                            gui.markDirty();
                        } else {
                            gui.playDismissSound();
                            gui.player.sendMessage(TextUtil.prefix(Text.empty().append(paymentCheck.failureMessage()).formatted(Formatting.RED)));
                            gui.setTempTitle(Text.empty().append(paymentCheck.failureMessage()).formatted(Formatting.DARK_RED));
                        }
                    } else {
                        var text = TextUtil.text(clickType.shift ? "not_enough_inventory_space" : "not_enough_stack_space");
                        gui.player.sendMessage(TextUtil.prefix(text.copy().formatted(Formatting.RED)));
                        gui.setTempTitle(text.formatted(Formatting.DARK_RED));
                        gui.playDismissSound();
                    }
                }
            };
        }
    }
}
