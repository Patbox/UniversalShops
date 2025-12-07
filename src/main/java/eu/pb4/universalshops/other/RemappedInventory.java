package eu.pb4.universalshops.other;

import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public interface RemappedInventory extends Container {
    Container getInventory();

    @Override
    default int getContainerSize() {
        return this.getInventory().getContainerSize();
    }

    @Override
    default boolean isEmpty() {
        return this.getInventory().isEmpty();
    }

    @Override
    default ItemStack getItem(int slot) {
        return this.getInventory().getItem(slot);
    }

    @Override
    default ItemStack removeItem(int slot, int amount) {
        return this.getInventory().removeItem(slot, amount);
    }

    @Override
    default ItemStack removeItemNoUpdate(int slot) {
        return this.getInventory().removeItemNoUpdate(slot);
    }

    @Override
    default void setItem(int slot, ItemStack stack) {
        this.getInventory().setItem(slot, stack);
    }

    @Override
    default void setChanged() {
        this.getInventory().setChanged();
    }

    @Override
    default boolean stillValid(Player player) {
        return this.getInventory().stillValid(player);
    }

    @Override
    default void clearContent() {
        this.getInventory().clearContent();
    }

    @Override
    default int getMaxStackSize() {
        return this.getInventory().getMaxStackSize();
    }
}
