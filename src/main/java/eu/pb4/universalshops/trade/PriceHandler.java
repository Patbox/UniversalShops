package eu.pb4.universalshops.trade;

import eu.pb4.common.economy.api.CommonEconomy;
import eu.pb4.common.economy.api.EconomyAccount;
import eu.pb4.common.economy.api.EconomyCurrency;
import eu.pb4.sgui.api.elements.GuiElementBuilder;
import eu.pb4.sgui.api.elements.GuiElementInterface;
import eu.pb4.sgui.api.gui.GuiInterface;
import eu.pb4.sgui.api.gui.SimpleGui;
import eu.pb4.universalshops.gui.*;
import eu.pb4.universalshops.gui.setup.ItemModificatorGui;
import eu.pb4.universalshops.gui.setup.VirtualBalanceSettingsGui;
import eu.pb4.universalshops.other.EmptyInventory;
import eu.pb4.universalshops.other.USUtil;
import eu.pb4.universalshops.other.TextUtil;
import eu.pb4.universalshops.registry.TradeShopBlockEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtByte;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.HoverEvent;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public abstract class PriceHandler extends GenericHandler {
    public static final Map<String, Definition> TYPES_MAP = new HashMap<>();
    public static final List<Definition> TYPES = new ArrayList<>();

    protected PriceHandler(PriceHandler.Definition  definition, TradeShopBlockEntity blockEntity) {
        super(definition, blockEntity);
    }

    public static void init() {
    }

    public abstract Result payFor(ServerPlayerEntity player, boolean canTake);

    public final NbtCompound writeNbt(NbtCompound nbt) {
        nbt.putString("PriceType", this.definition.type);
        nbt.put("PriceValue", writeValueNbt());
        return nbt;
    }

    public static PriceHandler readNbt(NbtCompound nbt, TradeShopBlockEntity blockEntity) {
        var type = nbt.getString("PriceType");

        var definition = TYPES_MAP.get(type);

        return definition != null ? definition.createFromNbt(nbt.get("PriceValue"), blockEntity) : Invalid.DEFINITION.createInitial(blockEntity);
    }

    public static void register(PriceHandler.Definition  definition) {
        TYPES.add(definition);
        TYPES_MAP.put(definition.type, definition);
    }

    public boolean usesInventory() {
        return false;
    }

    @Override
    public boolean canSwitch() {
        return this.getInventory().isEmpty();
    }

    public Inventory getInventory() {
        return EmptyInventory.INSTANCE;
    }

    public void openInventory(ServerPlayerEntity player, Runnable closeRunnable) {}

    public static abstract class Definition extends GenericHandler.Definition<PriceHandler> {
        public Definition(String type, Item icon) {
            this(type, TextUtil.of("pricehandler", type), icon.getDefaultStack());
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

    public static final class Invalid extends PriceHandler {
        public static final PriceHandler.Definition DEFINITION = new PriceHandler.Definition("invalid", TextUtil.text("not_set"), GuiElements.HEAD_QUESTION_MARK) {
            @Override
            public PriceHandler createFromNbt(NbtElement element, TradeShopBlockEntity blockEntity) {
                return new Invalid(this, blockEntity);
            }

            @Override
            public PriceHandler createInitial(TradeShopBlockEntity blockEntity) {
                return new Invalid(this, blockEntity);
            }

            @Override
            public boolean canUse(ServerPlayerEntity player) {
                return false;
            }
        };

        protected Invalid(PriceHandler.Definition create, TradeShopBlockEntity blockEntity) {
            super(create, blockEntity);
        }

        @Override
        public Result payFor(ServerPlayerEntity player, boolean canTake) {
            return Result.failed(Text.translatable("todo"));
        }

        @Override
        public ItemStack icon() {
            return GuiElements.HEAD_QUESTION_MARK.copy();
        }

        @Override
        public boolean isSetup() {
            return false;
        }

        @Override
        protected NbtElement writeValueNbt() {
            return NbtByte.of(false);
        }

        @Override
        public Text getText() {
            return TextUtil.text("not_set").formatted(Formatting.RED);
        }

        @Override
        public int getMaxAmount(ServerPlayerEntity player) {
            return 0;
        }
    }

    public static final class SingleItem extends PriceHandler implements ItemModificatorGui.ItemStackHolder {
        public static final PriceHandler.Definition DEFINITION = new PriceHandler.Definition("items", Items.DIAMOND) {
            @Override
            public PriceHandler createFromNbt(NbtElement element, TradeShopBlockEntity blockEntity) {
                var nbt = (NbtCompound) element;
                var x = new SingleItem(this, ItemStack.fromNbt(nbt.getCompound("Value")), blockEntity);

                x.currencyInventory.readNbtList(nbt.getList("CurrencyContainer", NbtElement.COMPOUND_TYPE));
                return x;
            }

            @Override
            public PriceHandler createInitial(TradeShopBlockEntity blockEntity) {
                return new SingleItem(this, ItemStack.EMPTY, blockEntity);
            }
        };

        private ItemStack value;
        public final SimpleInventory currencyInventory = new SimpleInventory(27);


        protected SingleItem(PriceHandler.Definition  creator, ItemStack initialValue, TradeShopBlockEntity blockEntity) {
            super(creator, blockEntity);
            this.value = initialValue;
        }

        @Override
        public GuiElementInterface getSetupElement() {
            return ItemModificatorGui.stackHolderElement(this, true);
        }

        @Override
        public boolean usesInventory() {
            return true;
        }

        @Override
        public Inventory getInventory() {
            return this.currencyInventory;
        }

        @Override
        public void openInventory(ServerPlayerEntity player, Runnable closeRunnable) {
            var gui = new SimpleGui(ScreenHandlerType.GENERIC_9X4, player, false) {
                @Override
                public void close() {
                    this.close(true);
                }

                @Override
                public void onClose() {
                    closeRunnable.run();
                    super.onClose();
                }
            };

            gui.setTitle(ExtraGui.texture(player, GuiBackground.SELECTOR).append(TextUtil.gui("shop.currency_storage")));

            for (int i = 0; i < this.currencyInventory.size(); i++) {
                gui.setSlotRedirect(i, new CurrencySlot(this.currencyInventory, i));
            }

            if (!ExtraGui.hasTexture(player)) {
                for (int i = 0; i < 9; i++) {
                    gui.setSlot(9 * 3 + i, GuiElements.FILLER);
                }
            }

            gui.setSlot(9 * 3 + 8, GuiElements.BACK);

            gui.open();
        }

        @Override
        public boolean canSwitch() {
            return this.currencyInventory.isEmpty();
        }

        @Override
        public Text getText() {
            return USUtil.asText(this.value);
        }

        @Override
        public int getMaxAmount(ServerPlayerEntity player) {
            var a = USUtil.transfer(player.getInventory(), this::equalsValue, Integer.MAX_VALUE, true, USUtil.ALWAYS_ALLOW);
            return a / this.value.getCount();
        }

        @Override
        public Result payFor(ServerPlayerEntity player, boolean canTake) {
            var chest = USUtil.copyInventory(this.currencyInventory);

            var count = USUtil.transfer(player.getInventory(), this::equalsValue, this.value.getCount(), true, this.shop.isAdmin() ? USUtil.ALWAYS_ALLOW : USUtil.addToInventory(chest));

            if (count >= this.value.getCount()) {
                if (canTake) {
                    USUtil.transfer(player.getInventory(), this::equalsValue, this.value.getCount(), false, this.shop.isAdmin() ? USUtil.ALWAYS_ALLOW : USUtil.addToInventory(this.currencyInventory));
                }

                return Result.successful();
            }

            return Result.failed(TextUtil.text(USUtil.canInsert(chest, this.value, this.value.getCount())  ? "not_enough_currency" : "not_enough_shop_storage_space",
                    this.value.getName().copy().styled(x -> x.withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_ITEM, new HoverEvent.ItemStackContent(this.value)))),
                    this.value.getCount(), count));
        }

        private boolean equalsValue(ItemStack stack) {
            return USUtil.areStacksMatching(this.value, stack);
        }

        @Override
        public ItemStack icon() {
            return this.value.copy();
        }

        @Override
        public GuiElementInterface getAccessElement() {
            return this.shop.isAdmin() ? GuiElements.FILLER : GuiElements.CURRENCY_INVENTORY;
        }

        @Override
        public boolean isSetup() {
            return !this.value.isEmpty();
        }

        @Override
        protected NbtElement writeValueNbt() {
            var nbt = new NbtCompound();
            nbt.put("Value", this.value.writeNbt(new NbtCompound()));
            nbt.put("CurrencyContainer", this.currencyInventory.toNbtList());
            return nbt;
        }

        @Override
        public ItemStack getItemStack() {
            return this.value;
        }

        @Override
        public void setItemStack(ItemStack stack) {
            this.value = stack;
        }
    }

    public static final class Free extends PriceHandler {
        public static final PriceHandler.Definition DEFINITION = new PriceHandler.Definition("free", Items.OXEYE_DAISY) {
            @Override
            public PriceHandler createFromNbt(NbtElement nbt, TradeShopBlockEntity blockEntity) {
                return new Free(this, blockEntity);

            }

            @Override
            public PriceHandler createInitial(TradeShopBlockEntity blockEntity) {
                return new Free(this, blockEntity);
            }
        };
        
        protected Free(PriceHandler.Definition  definition, TradeShopBlockEntity blockEntity) {
            super(definition, blockEntity);
        }

        @Override
        public boolean canSwitch() {
            return true;
        }

        @Override
        public Text getText() {
            return TextUtil.text("free");
        }

        @Override
        public int getMaxAmount(ServerPlayerEntity player) {
            return Integer.MAX_VALUE;
        }

        @Override
        public Result payFor(ServerPlayerEntity player, boolean canTake) {
            return Result.successful();
        }

        @Override
        public ItemStack icon() {
            var i = DEFINITION.icon.copy();
            i.setCustomName(this.getText().copy().setStyle(Style.EMPTY.withItalic(false)));
            return i;
        }

        @Override
        public boolean isSetup() {
            return true;
        }

        @Override
        protected NbtElement writeValueNbt() {
            return NbtByte.of(false);
        }
    }

    public static final class VirtualBalance extends PriceHandler implements VirtualBalanceSettingsGui.Controller {
        public static final PriceHandler.Definition DEFINITION = new PriceHandler.Definition("virtual_balance", Items.SUNFLOWER) {
            @Override
            public PriceHandler createFromNbt(NbtElement nbte, TradeShopBlockEntity blockEntity) {
                var nbt = (NbtCompound) nbte;
                return new VirtualBalance(this, Identifier.tryParse(nbt.getString("Currency")), nbt.getLong("Value"), nbt.getLong("Stored"), blockEntity);

            }

            @Override
            public PriceHandler createInitial(TradeShopBlockEntity blockEntity) {
                return new VirtualBalance(this, null, 0, 0, blockEntity);
            }

            @Override
            public boolean canUse(ServerPlayerEntity player) {
                return CommonEconomy.getCurrencies(player.server).size() > 0;
            }
        };
        private static final GuiElementInterface SETTINGS = new GuiElementBuilder(Items.GREEN_STAINED_GLASS_PANE).setName(TextUtil.text("configure")).setCallback((a, b, c, g) -> {
            if (g instanceof ShopGui gui) {
                gui.playClickSound();
                new VirtualBalanceSettingsGui(gui.getPlayer(), gui.getBE(), (VirtualBalanceSettingsGui.Controller) gui.getBE().priceHandler);
            }
        }).build();
        @Nullable
        public Identifier currency;
        public long cost;
        public long storedMoney;
        public final WeakHashMap<ServerPlayerEntity, Identifier> usedAccounts = new WeakHashMap<>();

        protected VirtualBalance(PriceHandler.Definition  definition, Identifier account, long price, long stored, TradeShopBlockEntity blockEntity) {
            super(definition, blockEntity);
            this.currency = account;
            this.cost = price;
            this.storedMoney = stored;
        }

        @Override
        public boolean canSwitch() {
            return this.storedMoney <= 0;
        }

        @Override
        public Text getText() {
            var ac = getCurrency();

            return ac != null ? ac.formatValueText(this.cost, false) : Text.empty();
        }

        public EconomyCurrency getCurrency() {
            return CommonEconomy.getCurrency(this.shop.getWorld().getServer(), this.currency);
        }

        @Override
        public int getMaxAmount(ServerPlayerEntity player) {
            var ac = getSelectedAccount(player);

            return ac != null ? (int) (ac.balance() / this.cost) : 0;
        }

        @Override
        public Result payFor(ServerPlayerEntity player, boolean canTake) {
            var admin = this.shop.isAdmin();
            var pac = getSelectedAccount(player);

            if (pac != null) {
                if (!admin && this.storedMoney + this.cost <= 0) {
                    return Result.failed(TextUtil.text("virtual_money.cant_store"));
                }

                var transaction = pac.decreaseBalance(this.cost);
                if (transaction.isFailure()) {
                    return Result.failed(TextUtil.text("virtual_money.not_enough_money", transaction.message()));
                }

                if (!admin) {
                    this.storedMoney += this.cost;
                }
                this.shop.markDirty();
                return Result.successful();
            }

            return Result.failed(TextUtil.text("virtual_money.no_account", this.currency));
        }

        @Override
        public ItemStack icon() {
            var ac = getCurrency();

            if (ac != null) {
                var icon = ac.icon();

                icon.setCustomName(ac.formatValueText(this.cost, false).copy().setStyle(Style.EMPTY.withItalic(false)));

                return icon;
            }

            return GuiElements.HEAD_QUESTION_MARK.copy();
        }

        @Override
        public GuiElementInterface getSetupElement() {
            return SETTINGS;
        }

        @Override
        public GuiElementInterface getAccessElement() {
            return new GuiElementBuilder(Items.CHEST)
                    .setName(TextUtil.gui("virtual_balance.collect"))
                    .addLoreLine(TextUtil.gui("virtual_balance.stored", this.getCurrency().formatValueText(this.storedMoney, false).copy().formatted(Formatting.WHITE)).formatted(Formatting.YELLOW))
                    .setCallback((x, y, z, g) -> {
                        var gui = (ShopGui) g;
                        gui.playClickSound();
                        var ac = this.getSelectedAccount(g.getPlayer());

                        if (ac.increaseBalance(this.storedMoney).isSuccessful()) {
                            this.storedMoney = 0;
                        }
                    })
                    .build();
        }

        @Override
        public GuiElementInterface getUserElement() {
            return new GuiElementInterface() {
                @Override
                public ItemStack getItemStack() {
                    return ItemStack.EMPTY;
                }

                @Override
                public ItemStack getItemStackForDisplay(GuiInterface gui) {
                    var ac = VirtualBalance.this.getSelectedAccount(gui.getPlayer());


                    return GuiElementBuilder.from(ac != null ? ac.accountIcon() : GuiElements.HEAD_QUESTION_MARK)
                            .setName(TextUtil.gui("virtual_balance.selected_account", (ac != null ? ac.name().copy() : TextUtil.text("not_set")).formatted(Formatting.GRAY)))
                            .addLoreLine(TextUtil.gui("virtual_balance.balance", (ac != null ? ac.formattedBalance().copy() : TextUtil.text("not_set")).formatted(Formatting.WHITE)).formatted(Formatting.YELLOW))
                            .addLoreLine(Text.empty())
                            .addLoreLine(Text.empty()
                                    .append(Text.literal("» ").formatted(Formatting.DARK_GRAY))
                                    .append(TextUtil.gui("setup.click_to_change_mode.1")).formatted(Formatting.GRAY)
                            )
                            .addLoreLine(Text.empty()
                                    .append(Text.literal("   ").formatted(Formatting.DARK_GRAY))
                                    .append(TextUtil.gui("setup.click_to_change_mode.2")).formatted(Formatting.GRAY)
                            )
                            .hideFlags()
                            .asStack();
                }

                @Override
                public ClickCallback getGuiCallback() {
                    return (x, type, z, g) -> {
                        var gui = (ShopGui) g;

                        var accounts = List.copyOf(CommonEconomy.getAccounts(g.getPlayer(), VirtualBalance.this.getCurrency()));
                        var current = getSelectedAccount(g.getPlayer());

                        if (accounts.isEmpty() || current == null) {
                            gui.playDismissSound();
                            return;
                        }
                        gui.playClickSound();


                        int dir = type.shift ? -1 : 1;
                        gui.playClickSound();

                        var size = accounts.size();
                        VirtualBalance.this.usedAccounts.put(gui.getPlayer(), accounts.get((size + accounts.indexOf(current) + dir) % size).id());
                    };
                }
            };
        }

        private EconomyAccount getSelectedAccount(ServerPlayerEntity player) {
            var identifier = this.usedAccounts.get(player);
            if (identifier != null) {
                var account = CommonEconomy.getAccount(player, identifier);

                if (account != null && account.currency() == this.getCurrency()) {
                    return account;
                }
            }

            return CommonEconomy.getAccounts(player, this.getCurrency()).stream().sorted(Comparator.comparing(x -> -x.balance())).findFirst().orElse(null);
        }

        @Override
        public boolean isSetup() {
            return this.cost > 0 && this.currency != null && (this.shop.owner != null || this.shop.isAdmin()) && getCurrency() != null;
        }

        @Override
        protected NbtElement writeValueNbt() {
            var nbt = new NbtCompound();
            if (this.currency != null) {
                nbt.putString("Currency", this.currency.toString());
            }

            nbt.putLong("Value", this.cost);
            nbt.putLong("Stored", this.storedMoney);
            return nbt;
        }

        @Override
        public void setValue(long value) {
            this.cost = value;
        }

        @Override
        public long getValue() {
            return this.cost;
        }

        @Override
        public void setCurrencyId(Identifier identifier) {
            this.currency = identifier;
        }

        @Override
        public Identifier getCurrencyId() {
            return this.currency;
        }
    }

    static {
        register(Invalid.DEFINITION);
        register(SingleItem.DEFINITION);
        register(Free.DEFINITION);
        register(VirtualBalance.DEFINITION);
    }
}
