package eu.pb4.universalshops.gui.setup;

import eu.pb4.sgui.api.ClickType;
import eu.pb4.sgui.api.elements.GuiElement;
import eu.pb4.sgui.api.elements.GuiElementBuilder;
import eu.pb4.sgui.api.gui.SimpleGui;
import eu.pb4.universalshops.gui.ExtraGui;
import eu.pb4.universalshops.gui.GuiElements;
import eu.pb4.universalshops.other.TextUtil;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.registry.Registry;

public class SelectItemGui extends SimpleGui implements ExtraGui {

    private final ItemModificatorGui.ItemStackHolder holder;
    private final Runnable closeRunnable;
    private final DefaultedList<ItemStack> items = DefaultedList.of();
    private boolean ignore;
    private int page;

    public SelectItemGui(ServerPlayerEntity player, ItemModificatorGui.ItemStackHolder holder, Runnable closeRunnable) {
        super(ScreenHandlerType.GENERIC_9X6, player, false);
        this.holder = holder;
        this.closeRunnable = closeRunnable;

        for (var item : Registry.ITEM) {
            try {
                item.appendStacks(ItemGroup.SEARCH, this.items);
            } catch (Throwable e) {
                e.printStackTrace();
            }
        }

        for (int i = 0; i < 9; i++) {
            this.setSlot(9 * 5 + i, GuiElements.FILLER);
        }
        this.updateItemsVisual();
        this.setSlot(5 * 9 + 2, GuiElements.previousPage(() -> {
            this.playClickSound();
            var pages = (int) Math.ceil(this.items.size() / (9 * 5d));
            this.page = (pages + this.page - 1) % pages;
            this.updateItemsVisual();
        }));

        this.setSlot(5 * 9 + 4, GuiElements.BACK);

        this.setSlot(5 * 9 + 6, GuiElements.nextPage(() -> {
            this.playClickSound();
            var pages = (int) Math.ceil(this.items.size() / (9 * 5d));
            this.page = (this.page + 1) % pages;
            this.updateItemsVisual();
        }));

        this.setTitle(TextUtil.gui("modifying_item.change_item.title"));
        this.open();
    }

    private void updateItemsVisual() {
        for (int x = 0; x < 9 * 5; x++) {
            var i = this.page * 9 * 5 + x ;
            this.setSlot(x, i < this.items.size() ? GuiElementBuilder.from(this.items.get(i)).setCallback(this::setStack).build() : GuiElement.EMPTY);
        }

    }

    private void setStack(int i, ClickType type, SlotActionType slotActionType) {
        this.holder.setItemStack(this.items.get(i + this.page * 9 * 5));
        this.playClickSound();
        this.close();
    }

    @Override
    public void onClose() {
        super.onClose();
        if (!this.ignore && this.closeRunnable != null) {
            this.closeRunnable.run();
        }
    }

    @Override
    public void setIgnore(boolean val) {
        this.ignore = val;
    }
}
