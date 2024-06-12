package eu.pb4.universalshops.gui.setup;

import eu.pb4.sgui.api.GuiHelpers;
import eu.pb4.sgui.api.elements.GuiElementBuilder;
import eu.pb4.sgui.api.elements.GuiElementInterface;
import eu.pb4.sgui.api.gui.GuiInterface;
import eu.pb4.sgui.api.gui.SimpleGui;
import eu.pb4.universalshops.gui.ExtraGui;
import eu.pb4.universalshops.gui.GuiBackground;
import eu.pb4.universalshops.gui.GuiElements;
import eu.pb4.universalshops.other.TextUtil;
import net.minecraft.item.*;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.server.network.ServerPlayerEntity;

public class ItemModificatorGui extends SimpleGui implements ExtraGui {
    private final ItemStackHolder holder;
    private final GuiInterface previousGui;

    public ItemModificatorGui(ServerPlayerEntity player, ItemStackHolder holder) {
        super(ScreenHandlerType.GENERIC_9X3, player, false);
        this.previousGui = GuiHelpers.getCurrentGui(player);
        this.holder = holder;

        if (!hasTexture()) {
            for (int i = 0; i < this.getSize(); i++) {
                this.setSlot(i, GuiElements.FILLER);
            }
        }

        this.setSlot(9 * 1 + 1, stackHolderElement(holder, false));
        if (!hasTexture()) {
            this.setSlot(9 * 2 + 1, new GuiElementBuilder(Items.LIGHT_BLUE_STAINED_GLASS_PANE).setName(TextUtil.gui("modifying_item.change_item")).setCallback((a, b, c) -> {
                this.playClickSound();
                new SelectItemGui(player, holder);
            }));
        }
        this.setSlot(9 * 1 + 3, GuiElementBuilder.from(GuiElements.MINUS).setName(TextUtil.gui("modifying_item.stack_decrease")).setCallback((a, b, c) -> {
            if (this.holder.getItemStack().getCount() > 1) {
                this.playClickSound();
                this.holder.getItemStack().decrement(1);
            } else {
                this.playDismissSound();
            }
        }));

        this.setSlot(9 * 1 + 4, GuiElementBuilder.from(GuiElements.PLUS).setName(TextUtil.gui("modifying_item.stack_increase")).setCallback((a, b, c) -> {
            if (this.holder.getItemStack().getCount() < 64) {
                this.playClickSound();
                this.holder.getItemStack().increment(1);
            } else {
                this.playDismissSound();
            }
        }));

        this.setSlot(9 * 1 + 4 + 1, setStackSizeElement(Items.RABBIT_HIDE, 1));
        this.setSlot(9 * 1 + 4 + 2, setStackSizeElement(Items.RABBIT_HIDE, 16));
        this.setSlot(9 * 1 + 4 + 3, setStackSizeElement(Items.RABBIT_HIDE, 32));
        this.setSlot(9 * 1 + 4 + 4, setStackSizeElement(Items.RABBIT_HIDE, 64));

        this.setSlot(9 * 2 + 8, GuiElements.BACK);

        this.setTitle(texture(GuiBackground.ITEM_SELECT).append(TextUtil.gui("modifying_item.title")));

        this.open();
    }

    private GuiElementInterface setStackSizeElement(Item item, int i) {
        return new GuiElementBuilder(item).setName(TextUtil.gui("modifying_item.stack_size", i)).setCount(i).hideDefaultTooltip().setCallback((a, b, c) -> {
            this.playClickSound();
            this.holder.getItemStack().setCount(i);
        }).build();
    }

    public static GuiElementInterface stackHolderElement(ItemStackHolder holder, boolean opensGui) {
        return new GuiElementInterface() {
            @Override
            public ItemStack getItemStack() {
                return holder.getItemStack();
            }

            @Override
            public ClickCallback getGuiCallback() {
                return (a, b, c, g) -> {
                    if (g instanceof ExtraGui gui) {
                        gui.playClickSound();
                        var stack = gui.getPlayer().currentScreenHandler.getCursorStack();
                        if (!stack.isEmpty()) {
                            holder.setItemStack(stack.copy());
                        } else {
                            if (opensGui) {
                                new ItemModificatorGui(gui.getPlayer(), holder);
                            } else {
                                new SelectItemGui(gui.getPlayer(), holder);
                            }
                        }
                    }
                };
            }
        };
    }

    @Override
    public void close() {
        if (this.previousGui != null) {
            this.previousGui.open();
        } else {
            super.close();
        }
    }

    public interface ItemStackHolder {
        ItemStack getItemStack();
        void setItemStack(ItemStack stack);
    }
}


