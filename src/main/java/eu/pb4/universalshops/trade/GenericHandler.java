package eu.pb4.universalshops.trade;

import eu.pb4.sgui.api.elements.GuiElement;
import eu.pb4.universalshops.gui.GuiElements;
import eu.pb4.universalshops.other.TextUtil;
import eu.pb4.universalshops.registry.TradeShopBlockEntity;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

import java.util.function.Supplier;

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

    public GuiElement getSetupElement() {
        return GuiElements.FILLER.build();
    }

    public GuiElement getAccessElement() {
        return GuiElements.FILLER.build();
    }

    public GuiElement getUserElement() {
        return GuiElements.FILLER.build();
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
        public final Supplier<ItemStack> icon;

        public Definition(String type, Item icon) {
            this(type, TextUtil.of("pricehandler", type), icon::getDefaultInstance);
        }

        public Definition(String type, Component displayName, Supplier<ItemStack> icon) {
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
