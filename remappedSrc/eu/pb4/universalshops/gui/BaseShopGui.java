package eu.pb4.universalshops.gui;

import eu.pb4.sgui.api.GuiHelpers;
import eu.pb4.sgui.api.gui.GuiInterface;
import eu.pb4.sgui.api.gui.SimpleGui;
import eu.pb4.universalshops.registry.TradeShopBlockEntity;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import org.jetbrains.annotations.Nullable;

public class BaseShopGui extends SimpleGui implements ShopGui {
    public final TradeShopBlockEntity be;
    private final Text texture;
    private int titleTimer = -1;
    private Text realTitle = Text.empty();
    private final GuiInterface previousGui;

    public BaseShopGui(ScreenHandlerType type, ServerPlayerEntity player, TradeShopBlockEntity blockEntity, Text texture) {
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

    public void setTempTitle(Text text) {
        this.titleTimer = 80;
        this.setTitle(texture(texture).append(text));
    }

    public void setMainTitle(Text text) {
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
