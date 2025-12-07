package eu.pb4.universalshops.gui;

import net.minecraft.world.Container;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public class CurrencySlot extends Slot {
    public CurrencySlot(Container inventory, int index) {
        super(inventory, index, 0, 0);
    }

    @Override
    public boolean mayPlace(ItemStack stack) {
        return false;
    }

    @Override
    public ItemStack safeInsert(ItemStack stack, int count) {
        return stack;
    }
}
