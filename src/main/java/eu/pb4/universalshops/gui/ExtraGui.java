package eu.pb4.universalshops.gui;

import eu.pb4.sgui.api.gui.SlotGuiInterface;
import eu.pb4.universalshops.other.USUtil;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;

public interface ExtraGui extends SlotGuiInterface {
    default void playClickSound() {
        playSound(SoundEvents.UI_BUTTON_CLICK);
    }

    default void playDismissSound() {
        playSound(SoundEvents.ITEM_SHIELD_BLOCK);
    }

    default void playSound(SoundEvent event) {
        USUtil.playUiSound(this.getPlayer(), event);
    }

    void setIgnore(boolean val);
}
