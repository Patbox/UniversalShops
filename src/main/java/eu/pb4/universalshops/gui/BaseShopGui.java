package eu.pb4.universalshops.gui;

import eu.pb4.sgui.api.GuiHelpers;
import eu.pb4.sgui.api.gui.GuiInterface;
import eu.pb4.sgui.api.gui.SimpleGui;
import eu.pb4.universalshops.registry.TradeShopBlockEntity;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.inventory.MenuType;
import org.jetbrains.annotations.Nullable;

public class BaseShopGui extends SimpleGui implements ShopGui {
    public final TradeShopBlockEntity be;
    private final Component texture;
    private int titleTimer = -1;
    private Component realTitle = Component.empty();
    private final GuiInterface previousGui;

    public BaseShopGui(MenuType type, ServerPlayer player, TradeShopBlockEntity blockEntity, Component texture) {
        super(type, player, false);
        this.be = blockEntity;
        this.texture = texture;
        this.previousGui = GuiHelpers.getCurrentGui(player);
    }

    @Override
    public void onTick() {
        if (--this.titleTimer == -1) {
            this.setTitle(this.realTitle);
        }

        super.onTick();
    }

    public void setTempTitle(Component text) {
        this.titleTimer = 80;
        this.setTitle(texture(texture).append(text));
    }

    public void setMainTitle(Component text) {
        this.realTitle = texture(texture).append(text);
        if (this.titleTimer < 0) {
            this.setTitle(this.realTitle);
        }
    }

    @Override
    public void close() {
        if (this.previousGui != null) {
            this.previousGui.open();
        } else {
            super.close();
        }
    }

    @Override
    public TradeShopBlockEntity getBE() {
        return this.be;
    }
}
