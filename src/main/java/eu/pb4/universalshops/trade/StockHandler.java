package eu.pb4.universalshops.trade;

import eu.pb4.sgui.api.elements.GuiElementInterface;
import eu.pb4.universalshops.gui.*;
import eu.pb4.universalshops.gui.selling.AnyStackShopGui;
import eu.pb4.universalshops.gui.selling.SingleItemShopGui;
import eu.pb4.universalshops.gui.setup.ItemModificatorGui;
import eu.pb4.universalshops.other.USUtil;
import eu.pb4.universalshops.other.TextUtil;
import eu.pb4.universalshops.registry.TradeShopBlockEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.Predicate;

public abstract class StockHandler extends GenericHandler {
    public static final Map<String, StockHandler.Definition  > TYPES_MAP = new HashMap<>();
    public static final List<StockHandler.Definition> TYPES = new ArrayList<>();

    protected StockHandler(StockHandler.Definition definition, TradeShopBlockEntity blockEntity) {
        super(definition, blockEntity);
    }

    public static void init() {
    }

    public abstract void openTradeGui(ServerPlayerEntity player);
    
    public final NbtCompound writeNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup lookup) {
        nbt.putString("StockType", this.definition.type);
        nbt.put("StockValue", this.writeValueNbt(lookup));
        return nbt;
    }
    
    public static StockHandler readNbt(NbtCompound nbt, TradeShopBlockEntity blockEntity, RegistryWrapper.WrapperLookup lookup) {
        var type = nbt.getString("StockType");

        var definition = TYPES_MAP.get(type);

        return definition != null ? definition.createFromNbt(nbt.get("StockValue"), blockEntity, lookup) : Invalid.DEFINITION.createInitial(blockEntity);
    }

    public static void register(StockHandler.Definition   definition) {
        TYPES.add(definition);
        TYPES_MAP.put(definition.type, definition);
    }
    
    public abstract Text getStockName();

    @Override
    public Text getText() {
        return Text.empty();
    }

    public static abstract class Definition extends GenericHandler.Definition<StockHandler>  {
        public Definition(String type, Item icon) {
            this(type, TextUtil.of("stockhandler", type), icon.getDefaultStack());
        }

        public Definition(String type, Text displayName, ItemStack icon) {
            super(type, displayName, icon);
        }
    }

    public record Result(boolean success, @Nullable Text failureMessage) {
        public static Result failed(Text value) {
            return new Result(false, value);
        }

        public static Result successful() {
            return new Result(true, null);
        }
    }

    public static final class Invalid extends StockHandler {
        public static final StockHandler.Definition   DEFINITION = new StockHandler.Definition("invalid", TextUtil.text("not_set"), GuiElements.HEAD_QUESTION_MARK) {
            @Override
            public StockHandler createFromNbt(NbtElement compound, TradeShopBlockEntity blockEntity, RegistryWrapper.WrapperLookup lookup) {
                return new Invalid(this, blockEntity);
            }

            @Override
            public StockHandler createInitial(TradeShopBlockEntity blockEntity) {
                return new Invalid(this, blockEntity);
            }

            @Override
            public boolean canUse(ServerPlayerEntity player) {
                return false;
            }
        };

        protected Invalid(StockHandler.Definition   create, TradeShopBlockEntity blockEntity) {
            super(create, blockEntity);
        }

        @Override
        public void openTradeGui(ServerPlayerEntity player) {

        }

        @Override
        public ItemStack icon() {
            return GuiElements.HEAD_QUESTION_MARK.copy();
        }

        @Override
        public int getMaxAmount(ServerPlayerEntity player) {
            return 0;
        }

        @Override
        public boolean isSetup() {
            return false;
        }

        @Override
        protected NbtElement writeValueNbt(RegistryWrapper.WrapperLookup lookup) {
            return new NbtCompound();
        }

        @Override
        public boolean canSwitch() {
            return true;
        }

        @Override
        public Text getStockName() {
            return TextUtil.text("not_set").formatted(Formatting.RED);
        }
    }

    public static final class SingleItem extends StockHandler implements ItemModificatorGui.ItemStackHolder {
        public static final StockHandler.Definition   DEFINITION = new StockHandler.Definition  ("single_item", Items.NETHERITE_PICKAXE) {
            @Override
            public StockHandler createFromNbt(NbtElement compound, TradeShopBlockEntity blockEntity, RegistryWrapper.WrapperLookup lookup) {
                return new SingleItem(this, ItemStack.fromNbtOrEmpty(lookup, (NbtCompound) compound), blockEntity);
            }

            @Override
            public StockHandler createInitial(TradeShopBlockEntity blockEntity) {
                return new SingleItem(this, ItemStack.EMPTY, blockEntity);
            }
        };

        public ItemStack value;

        protected SingleItem(StockHandler.Definition creator, ItemStack itemStack, TradeShopBlockEntity blockEntity) {
            super(creator, blockEntity);
            this.value = itemStack;
        }

        @Override
        public void openTradeGui(ServerPlayerEntity player) {
            new SingleItemShopGui(player, this.shop);
        }

        @Override
        public ItemStack icon() {
            return this.value.copy();
        }

        @Override
        public GuiElementInterface getSetupElement() {
            return ItemModificatorGui.stackHolderElement(this, true);
        }

        @Override
        public boolean isSetup() {
            return !this.value.isEmpty();
        }

        @Override
        protected NbtElement writeValueNbt(RegistryWrapper.WrapperLookup lookup) {
            return this.value.encodeAllowEmpty(lookup);
        }

        @Override
        public boolean canSwitch() {
            return true;
        }

        @Override
        public Text getStockName() {
            return USUtil.asText(this.value);
        }

        public int getMaxAmount(ServerPlayerEntity player) {
            return this.shop.isAdmin() ? Integer.MAX_VALUE : transfer(true, (a) -> true) / this.value.getCount();
        }

        public int transfer(boolean dryRun, Predicate<ItemStack> stackConsumer) {
            if (this.shop.isAdmin()) {
                return stackConsumer.test(this.value.copy()) ? this.value.getCount() : 0;
            }

            return USUtil.transfer(this.shop.getContainer(), (b) -> USUtil.areStacksMatching(this.value, b), dryRun ? Integer.MAX_VALUE : this.value.getCount(), dryRun, stackConsumer);
        }

        @Override
        public ItemStack getItemStack() {
            return this.value;
        }

        @Override
        public void setItemStack(ItemStack stack) {
            this.value = stack;
            this.shop.markDirty();
        }
    }

    public static final class SelectedItem extends StockHandler {
        public static final StockHandler.Definition   DEFINITION = new StockHandler.Definition  ("selected_item", Items.POTION) {
            @Override
            public StockHandler createFromNbt(NbtElement compound, TradeShopBlockEntity blockEntity, RegistryWrapper.WrapperLookup lookup) {
                return new SelectedItem(this, blockEntity);
            }

            @Override
            public StockHandler createInitial(TradeShopBlockEntity blockEntity) {
                return new SelectedItem(this, blockEntity);
            }
        };


        protected SelectedItem(StockHandler.Definition   creator, TradeShopBlockEntity blockEntity) {
            super(creator, blockEntity);
        }

        @Override
        public void openTradeGui(ServerPlayerEntity player) {
            new AnyStackShopGui(player, this.shop);
        }

        @Override
        public ItemStack icon() {
            var container = this.shop.getContainer();
            if (container.isEmpty()) {
                return GuiElements.HEAD_QUESTION_MARK.copy();
            } else {
                var list = new ArrayList<ItemStack>(container.size());
                for (int i = 0; i < container.size(); i++) {
                    if (!container.getStack(i).isEmpty()) {
                        list.add(container.getStack(i));
                    }
                }

                return list.get((int) ((this.shop.getWorld().getTime() / 32) % list.size())).copy();
            }

        }

        @Override
        public int getMaxAmount(ServerPlayerEntity player) {
            if (this.shop.isAdmin()) {
                return Integer.MAX_VALUE;
            }

            int i = 0;
            for (var stack : USUtil.iterable(this.shop.getContainer())) {
                if (!stack.isEmpty()) {
                    i++;
                }
            }

            return i;
        }

        @Override
        public boolean isSetup() {
            return true;
        }

        @Override
        protected NbtElement writeValueNbt(RegistryWrapper.WrapperLookup lookup) {
            return new NbtCompound();
        }

        @Override
        public boolean canSwitch() {
            return true;
        }

        @Override
        public Text getStockName() {
            return TextUtil.text("select_anything");
        }
    }

    static {
        register(Invalid.DEFINITION);
        register(SingleItem.DEFINITION);
        register(SelectedItem.DEFINITION);
    }
}
