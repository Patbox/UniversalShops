package eu.pb4.universalshops.gui;

import eu.pb4.polymer.networking.api.server.PolymerServerNetworking;
import eu.pb4.polymer.resourcepack.api.PolymerResourcePackUtils;
import eu.pb4.sgui.api.gui.SlotGuiInterface;
import eu.pb4.universalshops.UniversalShopsMod;
import eu.pb4.universalshops.other.USUtil;
import net.minecraft.core.Holder;
import net.minecraft.nbt.IntTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;

public interface ExtraGui extends SlotGuiInterface {
    default void playClickSound() {
        playSound(SoundEvents.UI_BUTTON_CLICK);
    }

    default void playDismissSound() {
        playSound(SoundEvents.SHIELD_BLOCK);
    }

    default void playSound(SoundEvent event) {
        USUtil.playUiSound(this.getPlayer(), event);
    }

    default void playSound(Holder<SoundEvent> event) {
        USUtil.playUiSound(this.getPlayer(), event.value());
    }


    default MutableComponent texture(Component possibleTexture) {
        return texture(this.getPlayer(), possibleTexture);
    }

    default boolean hasTexture() {
        return hasTexture(this.getPlayer());
    }

    static boolean hasTexture(ServerPlayer player) {
        return PolymerResourcePackUtils.hasMainPack(player) || PolymerServerNetworking.getMetadata(player.connection, UniversalShopsMod.HELLO_PACKET, IntTag.TYPE) != null;
    }

    static MutableComponent texture(ServerPlayer player, Component possibleTexture) {
        if (hasTexture(player)) {
            return Component.empty().append(possibleTexture);
        } else {
            return Component.empty();
        }
    }
}
