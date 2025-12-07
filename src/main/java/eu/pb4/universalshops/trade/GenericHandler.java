package eu.pb4.universalshops.trade;

import eu.pb4.sgui.api.elements.GuiElementInterface;
import eu.pb4.universalshops.gui.GuiElements;
import eu.pb4.universalshops.other.TextUtil;
import eu.pb4.universalshops.registry.TradeShopBlockEntity;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

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

    public abstract void writeData(ValueOutput view);

    protected abstract void writeValueData(ValueOutput view);

    public abstract boolean canSwitch();

    public abstract Component getText();

    public abstract int getMaxAmount(ServerPlayer player);

    public static abstract class Definition<T extends GenericHandler> {
        public final String type;
        public final Component displayName;
        public final ItemStack icon;

        public Definition(String type, Item icon) {
            this(type, TextUtil.of("pricehandler", type), icon.getDefaultInstance());
        }

        public Definition(String type, Component displayName, ItemStack icon) {
            this.type = type;
            this.displayName = displayName;
            this.icon = icon;
        }

        public abstract T createFromData(ValueInput view, TradeShopBlockEntity blockEntity);
        public abstract T createInitial(TradeShopBlockEntity blockEntity);

        public boolean canUse(ServerPlayer player) {
            return true;
        }
    }
}
