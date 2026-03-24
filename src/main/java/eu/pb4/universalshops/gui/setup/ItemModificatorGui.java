package eu.pb4.universalshops.gui.setup;

import eu.pb4.sgui.api.SguiUtils;
import eu.pb4.sgui.api.elements.GuiElementBuilder;
import eu.pb4.sgui.api.elements.GuiElement;
import eu.pb4.sgui.api.gui.GuiLike;
import eu.pb4.sgui.api.gui.SimpleGui;
import eu.pb4.universalshops.gui.ExtraGui;
import eu.pb4.universalshops.gui.GuiBackground;
import eu.pb4.universalshops.gui.GuiElements;
import eu.pb4.universalshops.other.TextUtil;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public class ItemModificatorGui extends SimpleGui implements ExtraGui {
    private final ItemStackHolder holder;
    private final GuiLike previousGui;

    public ItemModificatorGui(ServerPlayer player, ItemStackHolder holder) {
        super(MenuType.GENERIC_9x3, player, false);
        this.previousGui = SguiUtils.getCurrentGui(player);
        this.holder = holder;

        if (!hasTexture()) {
            for (int i = 0; i < this.getSize(); i++) {
                this.setSlot(i, GuiElements.FILLER);
            }
        }

        this.setSlot(9 * 1 + 1, stackHolderElement(holder, false));
        if (!hasTexture()) {
            this.setSlot(9 * 2 + 1, new GuiElementBuilder(Items.LIGHT_BLUE_STAINED_GLASS_PANE).setName(TextUtil.gui("modifying_item.change_item")).setCallback(() -> {
                this.playClickSound();
                new SelectItemGui(player, holder);
            }));
        }
        this.setSlot(9 * 1 + 3, GuiElementBuilder.from(GuiElements.MINUS.asStack()).setName(TextUtil.gui("modifying_item.stack_decrease")).setCallback(() -> {
            if (this.holder.getItemStack().getCount() > 1) {
                this.playClickSound();
                this.holder.getItemStack().shrink(1);
            } else {
                this.playDismissSound();
            }
        }));

        this.setSlot(9 * 1 + 4, GuiElementBuilder.from(GuiElements.PLUS.asStack()).setName(TextUtil.gui("modifying_item.stack_increase")).setCallback(() -> {
            if (this.holder.getItemStack().getCount() < 64) {
                this.playClickSound();
                this.holder.getItemStack().grow(1);
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

    private GuiElement setStackSizeElement(Item item, int i) {
        return new GuiElementBuilder(item).setName(TextUtil.gui("modifying_item.stack_size", i)).setCount(i).hideDefaultTooltip().setCallback(() -> {
            this.playClickSound();
            this.holder.getItemStack().setCount(i);
        }).build();
    }

    public static GuiElement stackHolderElement(ItemStackHolder holder, boolean opensGui) {
        return new GuiElement() {
            @Override
            public ItemStack getItemStack() {
                return holder.getItemStack();
            }

            @Override
            public ClickCallback getGuiCallback() {
                return (a, b, c, g) -> {
                    if (g instanceof ExtraGui gui) {
                        gui.playClickSound();
                        var stack = gui.getPlayer().containerMenu.getCarried();
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


