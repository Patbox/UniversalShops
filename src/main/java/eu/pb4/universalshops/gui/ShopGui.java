package eu.pb4.universalshops.gui;

import eu.pb4.sgui.api.ClickType;
import eu.pb4.sgui.api.gui.SlotGuiInterface;
import eu.pb4.universalshops.gui.setup.ShopSettingsGui;
import eu.pb4.universalshops.registry.TradeShopBlockEntity;
import net.minecraft.screen.slot.SlotActionType;

public interface ShopGui extends ExtraGui {
    TradeShopBlockEntity getBE();

    default void openSettings() {
        this.close(true);
        new ShopSettingsGui(this.getPlayer(), this.getBE());
    }

    default void openCurrencyStorage() {
        this.close(true);
        this.getBE().priceHandler.openInventory(this.getPlayer(), () -> this.getBE().openGui(this.getPlayer()));
    }

    default boolean isAdmin() {
        return this.getBE().isAdmin();
    }

    static void openSettingsCallback(int i, ClickType clickType, SlotActionType slotActionType, SlotGuiInterface guiInterface) {
        if (guiInterface instanceof ShopGui gui) {
            gui.playClickSound();
            gui.openSettings();
        }
    }

    static void openCurrencyCallback(int i, ClickType clickType, SlotActionType slotActionType, SlotGuiInterface guiInterface) {
        if (guiInterface instanceof ShopGui gui && gui.getBE().priceHandler.usesInventory()) {
            gui.playClickSound();
            gui.openCurrencyStorage();
        }
    }

    default void markDirty() {
        this.getBE().markDirty();
    };
}
