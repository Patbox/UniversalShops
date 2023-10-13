package eu.pb4.universalshops.trade;

import eu.pb4.sgui.api.elements.GuiElementInterface;
import eu.pb4.universalshops.gui.GuiElements;
import eu.pb4.universalshops.other.TextUtil;
import eu.pb4.universalshops.registry.TradeShopBlockEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

public abstract class GenericHandler {
    public final TradeShopBlockEntity shop;
    public final GenericHandler.Definition<?> definition;

    protected GenericHandler(GenericHandler.Definition<?> definition, TradeShopBlockEntity blockEntity) {
        this.definition = definition;
        this.shop = blockEntity;
    }

    public GenericHandler.Definition<?> definition() {
        return definition;
    }

    public GuiElementInterface getSetupElement() {
        return GuiElements.FILLER;
    }

    public GuiElementInterface getAccessElement() {
        return GuiElements.FILLER;
    }

    public GuiElementInterface getUserElement() {
        return GuiElements.FILLER;
    }

    public abstract ItemStack icon();

    public abstract boolean isSetup();

    public abstract NbtCompound writeNbt(NbtCompound compound);

    protected abstract NbtElement writeValueNbt();

    public abstract boolean canSwitch();

    public abstract Text getText();

    public abstract int getMaxAmount(ServerPlayerEntity player);

    public static abstract class Definition<T extends GenericHandler> {
        public final String type;
        public final Text displayName;
        public final ItemStack icon;

        public Definition(String type, Item icon) {
            this(type, TextUtil.of("pricehandler", type), icon.getDefaultStack());
        }

        public Definition(String type, Text displayName, ItemStack icon) {
            this.type = type;
            this.displayName = displayName;
            this.icon = icon;
        }

        public abstract T createFromNbt(NbtElement compound, TradeShopBlockEntity blockEntity);
        public abstract T createInitial(TradeShopBlockEntity blockEntity);

        public boolean canUse(ServerPlayerEntity player) {
            return true;
        }
    }
}
