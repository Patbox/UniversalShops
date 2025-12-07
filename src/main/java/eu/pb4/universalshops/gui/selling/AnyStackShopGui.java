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
import java.util.ArrayList;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.TooltipFlag;

public class AnyStackShopGui extends BaseShopGui {
    private int tick = 0;
    private int maxStockCount;
    private int stockCount;
    private List<BuyElement> items = new ArrayList<>();
    private int page;
    private int lastBought;

    public AnyStackShopGui(ServerPlayer player, TradeShopBlockEntity blockEntity) {
        super(MenuType.GENERIC_9x6, player, blockEntity, GuiBackground.TAKE_ANY);

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
        var s = inv.getContainerSize();
        for (var e : this.items) {
            if (s < e.slot || e.itemStack != inv.getItem(e.slot)) {
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
                List<Component> tooltip;

                try {
                    tooltip = item.getTooltipLines(Item.TooltipContext.of(this.player.level()), this.player, TooltipFlag.Default.NORMAL);
                } catch (Throwable e) {
                    tooltip = List.of(item.getHoverName());
                }

                secondStack = new GuiElementBuilder(Items.BARRIER).setName(tooltip.remove(0)).setLore(tooltip).asStack();
            }

            this.setSlot(1 * 9 + 1, new AnimatedGuiElement(new ItemStack[]{
                    item,
                    secondStack
            }, 12, false, GuiElement.EMPTY_CALLBACK));

            this.setSlot(2 * 9 + 1, GuiElements.priceMarker(this.be.priceHandler.getText(), List.of(
                    Component.empty(),
                    TextUtil.gui("max_stock_left", TextUtil.number(maxStockCount).withStyle(ChatFormatting.WHITE)).withStyle(ChatFormatting.YELLOW)
                    )));
        }

        this.updateItems();
    }

    private void updateItems() {
        this.items.clear();
        var inv = this.be.getContainer();
        for (var i = 0; i < inv.getContainerSize(); i++) {
            var stack = inv.getItem(i);
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

                if (admin || gui.getBE().getContainer().getItem(this.slot) == this.itemStack) {
                    var count = USUtil.canInsert(clickType.shift
                            ? USUtil.copyInventory(gui.getPlayer().getInventory().getNonEquipmentItems())
                            : new SimpleContainer(gui.getPlayer().containerMenu.getCarried().copy()), this.itemStack, this.itemStack.getCount());

                    if (count) {
                        var paymentCheck = gui.be.priceHandler.payFor(gui.player, true);
                        if (paymentCheck.success()) {
                            (clickType.shift ? USUtil.addToInventory(gui.player.getInventory().getNonEquipmentItems()) : USUtil.mergeIntoCursor(gui.player.containerMenu)).test(this.itemStack.copy());
                            if (!admin) {
                                this.itemStack.setCount(0);
                                gui.be.getContainer().setItem(this.slot, ItemStack.EMPTY);
                            }
                            gui.playClickSound();
                            gui.updateValueDisplays();
                            gui.markDirty();
                        } else {
                            gui.playDismissSound();
                            gui.player.sendSystemMessage(TextUtil.prefix(Component.empty().append(paymentCheck.failureMessage()).withStyle(ChatFormatting.RED)));
                            gui.setTempTitle(Component.empty().append(paymentCheck.failureMessage()).withStyle(ChatFormatting.DARK_RED));
                        }
                    } else {
                        var text = TextUtil.text(clickType.shift ? "not_enough_inventory_space" : "not_enough_stack_space");
                        gui.player.sendSystemMessage(TextUtil.prefix(text.copy().withStyle(ChatFormatting.RED)));
                        gui.setTempTitle(text.withStyle(ChatFormatting.DARK_RED));
                        gui.playDismissSound();
                    }
                }
            };
        }
    }
}
