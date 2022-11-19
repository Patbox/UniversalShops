package eu.pb4.universalshops.gui;

import eu.pb4.sgui.api.gui.SimpleGui;
import eu.pb4.universalshops.registry.TradeShopBlockEntity;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import org.jetbrains.annotations.Nullable;

public class BaseShopGui extends SimpleGui implements ShopGui {
    public final TradeShopBlockEntity be;
    private final Runnable onCloseRunnable;
    private int titleTimer = -1;
    private Text realTitle = Text.empty();
    private boolean ignore;

    public BaseShopGui(ScreenHandlerType type, ServerPlayerEntity player, TradeShopBlockEntity blockEntity, @Nullable Runnable onClose) {
        super(type, player, false);
        this.onCloseRunnable = onClose;
        this.be = blockEntity;
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
        this.setTitle(text);
    }

    public void setMainTitle(Text text) {
        this.realTitle = text;
        if (this.titleTimer < 0) {
            this.setTitle(text);
        }
    }

    @Override
    public void close() {
        this.close(onCloseRunnable != null && !this.ignore);
    }

    @Override
    public void onClose() {
        if (onCloseRunnable != null && !this.ignore) {
            this.onCloseRunnable.run();
        }
        super.onClose();
    }

    @Override
    public TradeShopBlockEntity getBE() {
        return this.be;
    }

    @Override
    public void setIgnore(boolean val) {
        this.ignore = val;
    }
}
