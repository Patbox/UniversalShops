package eu.pb4.universalshops.gui;

import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.slot.Slot;

public class CurrencySlot extends Slot {
    public CurrencySlot(Inventory inventory, int index) {
        super(inventory, index, 0, 0);
    }

    @Override
    public boolean canInsert(ItemStack stack) {
        return false;
    }

    @Override
    public ItemStack insertStack(ItemStack stack, int count) {
        return stack;
    }
}
