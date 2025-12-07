package eu.pb4.universalshops.trade;

import eu.pb4.sgui.api.elements.GuiElementInterface;
import eu.pb4.universalshops.gui.*;
import eu.pb4.universalshops.gui.selling.AnyStackShopGui;
import eu.pb4.universalshops.gui.selling.SingleItemShopGui;
import eu.pb4.universalshops.gui.setup.ItemModificatorGui;
import eu.pb4.universalshops.other.USUtil;
import eu.pb4.universalshops.other.TextUtil;
import eu.pb4.universalshops.registry.TradeShopBlockEntity;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.Predicate;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

public abstract class StockHandler extends GenericHandler {
    public static final Map<String, StockHandler.Definition  > TYPES_MAP = new HashMap<>();
    public static final List<StockHandler.Definition> TYPES = new ArrayList<>();

    protected StockHandler(StockHandler.Definition definition, TradeShopBlockEntity blockEntity) {
        super(definition, blockEntity);
    }

    public static void init() {
    }

    public abstract void openTradeGui(ServerPlayer player);
    
    public final void writeData(ValueOutput view) {
        view.putString("StockType", this.definition.type);
        this.writeValueData(view.child("StockValue"));
    }
    
    public static StockHandler readData(ValueInput view, TradeShopBlockEntity blockEntity) {
        var type = view.getStringOr("StockType", "");

        var definition = TYPES_MAP.get(type);

        return definition != null ? definition.createFromData(view.childOrEmpty("StockValue"), blockEntity) : Invalid.DEFINITION.createInitial(blockEntity);
    }

    public static void register(StockHandler.Definition   definition) {
        TYPES.add(definition);
        TYPES_MAP.put(definition.type, definition);
    }
    
    public abstract Component getStockName();

    @Override
    public Component getText() {
        return Component.empty();
    }

    public static abstract class Definition extends GenericHandler.Definition<StockHandler>  {
        public Definition(String type, Item icon) {
            this(type, TextUtil.of("stockhandler", type), icon.getDefaultInstance());
        }

        public Definition(String type, Component displayName, ItemStack icon) {
            super(type, displayName, icon);
        }
    }

    public record Result(boolean success, @Nullable Component failureMessage) {
        public static Result failed(Component value) {
            return new Result(false, value);
        }

        public static Result successful() {
            return new Result(true, null);
        }
    }

    public static final class Invalid extends StockHandler {
        public static final StockHandler.Definition  DEFINITION = new StockHandler.Definition("invalid", TextUtil.text("not_set"), GuiElements.HEAD_QUESTION_MARK) {
            @Override
            public StockHandler createFromData(ValueInput view, TradeShopBlockEntity blockEntity) {
                return new Invalid(this, blockEntity);
            }

            @Override
            public StockHandler createInitial(TradeShopBlockEntity blockEntity) {
                return new Invalid(this, blockEntity);
            }

            @Override
            public boolean canUse(ServerPlayer player) {
                return false;
            }
        };

        protected Invalid(StockHandler.Definition   create, TradeShopBlockEntity blockEntity) {
            super(create, blockEntity);
        }

        @Override
        public void openTradeGui(ServerPlayer player) {

        }

        @Override
        public ItemStack icon() {
            return GuiElements.HEAD_QUESTION_MARK.copy();
        }

        @Override
        public int getMaxAmount(ServerPlayer player) {
            return 0;
        }

        @Override
        public boolean isSetup() {
            return false;
        }

        @Override
        protected void writeValueData(ValueOutput view) {

        }

        @Override
        public boolean canSwitch() {
            return true;
        }

        @Override
        public Component getStockName() {
            return TextUtil.text("not_set").withStyle(ChatFormatting.RED);
        }
    }

    public static final class SingleItem extends StockHandler implements ItemModificatorGui.ItemStackHolder {
        public static final StockHandler.Definition   DEFINITION = new StockHandler.Definition  ("single_item", Items.NETHERITE_PICKAXE) {
            @Override
            public StockHandler createFromData(ValueInput view, TradeShopBlockEntity blockEntity) {
                return new SingleItem(this, view.read(ItemStack.MAP_CODEC).orElse(ItemStack.EMPTY), blockEntity);
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
        public void openTradeGui(ServerPlayer player) {
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
        protected void writeValueData(ValueOutput view) {
            if (!this.value.isEmpty()) {
                view.store(ItemStack.MAP_CODEC, this.value);
            }
        }

        @Override
        public boolean canSwitch() {
            return true;
        }

        @Override
        public Component getStockName() {
            return USUtil.asText(this.value);
        }

        public int getMaxAmount(ServerPlayer player) {
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
            this.shop.setChanged();
        }
    }

    public static final class SelectedItem extends StockHandler {
        public static final StockHandler.Definition   DEFINITION = new StockHandler.Definition  ("selected_item", Items.POTION) {
            @Override
            public StockHandler createFromData(ValueInput view, TradeShopBlockEntity blockEntity) {
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
        public void openTradeGui(ServerPlayer player) {
            new AnyStackShopGui(player, this.shop);
        }

        @Override
        public ItemStack icon() {
            var container = this.shop.getContainer();
            if (container.isEmpty()) {
                return GuiElements.HEAD_QUESTION_MARK.copy();
            } else {
                var list = new ArrayList<ItemStack>(container.getContainerSize());
                for (int i = 0; i < container.getContainerSize(); i++) {
                    if (!container.getItem(i).isEmpty()) {
                        list.add(container.getItem(i));
                    }
                }

                return list.get((int) ((this.shop.getLevel().getGameTime() / 32) % list.size())).copy();
            }

        }

        @Override
        public int getMaxAmount(ServerPlayer player) {
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
        protected void writeValueData(ValueOutput view) {

        }

        @Override
        public boolean canSwitch() {
            return true;
        }

        @Override
        public Component getStockName() {
            return TextUtil.text("select_anything");
        }
    }

    static {
        register(Invalid.DEFINITION);
        register(SingleItem.DEFINITION);
        register(SelectedItem.DEFINITION);
    }
}
