package eu.pb4.universalshops.other;

import eu.pb4.universalshops.UniversalShopsMod;
import me.lucko.fabric.api.permissions.v0.Permissions;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundSoundEntityPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.HopperBlockEntity;
import org.jetbrains.annotations.Nullable;

import java.util.Iterator;
import java.util.function.IntFunction;
import java.util.function.Predicate;

public class USUtil {

    public static final Predicate<ItemStack> ALWAYS_ALLOW = (x) -> true;

    public static boolean areStacksMatching(ItemStack a, ItemStack b) {
        return ItemStack.isSameItemSameComponents(a, b);
    }

    public static int transfer(Container inventory, Predicate<ItemStack> shouldRemove, int maxCount, boolean dryRun, Predicate<ItemStack> consumer) {
        if (maxCount < 1) {
            return 0;
        }

        int i = 0;

        for (int j = 0; j < inventory.getContainerSize(); ++j) {
            ItemStack itemStack = inventory.getItem(j);
            int k = transfer(itemStack, shouldRemove, maxCount - i, dryRun, consumer);
            if (k > 0 && !dryRun && itemStack.isEmpty()) {
                inventory.setItem(j, ItemStack.EMPTY);
            }

            i += k;
        }
        if (i != 0) {
            inventory.setChanged();
        }

        return i;
    }

    private static int transfer(ItemStack stack, Predicate<ItemStack> shouldRemove, int maxCount, boolean dryRun, Predicate<ItemStack> consumer) {
        if (!stack.isEmpty() && shouldRemove.test(stack)) {
            int i = Math.min(maxCount, stack.getCount());
            if (i != 0) {
                var copy = stack.copy();
                copy.setCount(i);
                if (!consumer.test(copy)) {
                    return 0;
                }

                if (!dryRun) {
                    stack.shrink(i);
                }
            }
            return i;
        } else {
            return 0;
        }
    }

    @Nullable
    public static Container getInventoryAt(Level world, BlockPos pos) {
        return HopperBlockEntity.getContainerAt(world, pos);
    }

    public static Component asText(ItemStack value) {
        return value.getCount() == 1 ? value.getHoverName() : Component.empty().append(value.getHoverName()).append(" × " + value.getCount());
    }

    public static SimpleContainer copyInventory(Container input) {
        var out = new SimpleContainer(input.getContainerSize());
        for (int i = 0; i < input.getContainerSize(); i++) {
            out.setItem(i, input.getItem(i).copy());
        }
        return out;
    }

    public static boolean checkAdmin(Player player, String permission) {
        return Permissions.check(player, UniversalShopsMod.MOD_ID + "." + permission, 3);
    }

    public static Predicate<CommandSourceStack> requireAdmin(String permission) {
        return Permissions.require(UniversalShopsMod.MOD_ID + "." + permission, 3);
    }

    public static boolean checkDefault(Player player, String permission) {
        return Permissions.check(player, UniversalShopsMod.MOD_ID + "." + permission, true);
    }

    public static Predicate<CommandSourceStack> requireDefault(String permission) {
        return Permissions.require(UniversalShopsMod.MOD_ID + "." + permission, true);
    }

    public static SimpleContainer copyInventory(NonNullList<ItemStack> input) {
        var out = new SimpleContainer(input.size());
        for (int i = 0; i < input.size(); i++) {
            out.setItem(i, input.get(i).copy());
        }
        return out;
    }

    public static Predicate<ItemStack> addToInventory(Container inventory) {
        return (i) -> addStack(inventory, i).isEmpty();
    }

    public static Predicate<ItemStack> addToInventory(NonNullList<ItemStack> inventory) {
        return (i) -> addStack(inventory, i).isEmpty();
    }

    public static ItemStack addStack(Container inv, ItemStack stack) {
        return addStack(inv::getItem, inv::setItem, inv.getContainerSize(), inv.getMaxStackSize(), stack);
    }

    public static ItemStack addStack(NonNullList<ItemStack> inv, ItemStack stack) {
        return addStack(inv::get, inv::set, inv.size(), 64, stack);
    }

    private static ItemStack addStack(IntFunction<ItemStack> getter, SlotSetter setter, int size, int maxStackSize, ItemStack stack) {
        ItemStack stackCopy = stack.copy();
        // Insert to existing
        for(int i = 0; i < size; ++i) {
            var itemStack = getter.apply(i);
            if (ItemStack.isSameItemSameComponents(itemStack, stackCopy)) {
                transfer(maxStackSize, stackCopy, itemStack);
                if (stackCopy.isEmpty()) {
                    break;
                }
            }
        }

        if (stackCopy.isEmpty()) {
            return ItemStack.EMPTY;
        } else {
            for(int i = 0; i < size; ++i) {
                ItemStack itemStack = getter.apply(i);
                if (itemStack.isEmpty()) {
                    setter.set(i, stackCopy.copy());
                    stackCopy.setCount(0);
                    break;
                }
            }


            return stackCopy.isEmpty() ? ItemStack.EMPTY : stackCopy;
        }
    }

    private static void transfer(int maxStackSize, ItemStack source, ItemStack target) {
        int i = Math.min(maxStackSize, target.getMaxStackSize());
        int j = Math.min(source.getCount(), i - target.getCount());
        if (j > 0) {
            target.grow(j);
            source.shrink(j);
        }
    }

    public static Predicate<ItemStack> mergeIntoCursor(AbstractContainerMenu handler) {
        return (stack) -> {
            if (handler.getCarried().isEmpty()) {
                handler.setCarried(stack.copy());
                stack.setCount(0);
                return true;
            } else if (ItemStack.isSameItemSameComponents(stack, handler.getCarried())) {
                var count = stack.getCount() + handler.getCarried().getCount();

                if (count > handler.getCarried().getMaxStackSize()) {
                    return false;
                } else {
                    handler.getCarried().setCount(count);
                    stack.setCount(0);
                    return true;
                }
            }

            return false;
        };
    }

    public static void playUiSound(ServerPlayer player, SoundEvent event) {
        player.connection.send (new ClientboundSoundEntityPacket(BuiltInRegistries.SOUND_EVENT.wrapAsHolder(event), SoundSource.UI, player, 0.8f, 1, player.getRandom().nextLong()));
    }

    public static boolean canInsert(Container chest, ItemStack value, int count) {
        return canInsert(chest::getItem, chest.getContainerSize(), value, count);
    }

    public static boolean canInsert(NonNullList<ItemStack> chest, ItemStack value, int count) {
        return canInsert(chest::get, chest.size(), value, count);
    }


    private static boolean canInsert(IntFunction<ItemStack> getter, int size, ItemStack value, int count) {
        for (int i = 0; i < size; i++) {
            var stack = getter.apply(i);

            if (stack.isEmpty()) {
                return true;
            }

            if (ItemStack.isSameItemSameComponents(stack, value)) {
                count -= (stack.getMaxStackSize() - stack.getCount());

                if (count <= 0) {
                    return true;
                }
            }
        }
        return false;
    }

    public static Iterable<ItemStack> iterable(Container container) {
        return () -> new Iterator<>() {
            int i = 0;

            @Override
            public boolean hasNext() {
                return i < container.getContainerSize();
            }

            @Override
            public ItemStack next() {
                return container.getItem(i++);
            }
        };
    }

    public interface SlotSetter {
        void set(int i, ItemStack stack);
    }
}
