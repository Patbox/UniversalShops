package eu.pb4.universalshops.gui;

import eu.pb4.polymer.networking.api.server.PolymerServerNetworking;
import eu.pb4.polymer.resourcepack.api.PolymerResourcePackUtils;
import eu.pb4.sgui.api.gui.SlotGuiInterface;
import eu.pb4.universalshops.UniversalShopsMod;
import eu.pb4.universalshops.other.USUtil;
import net.minecraft.nbt.NbtInt;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;

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

    default void playSound(RegistryEntry<SoundEvent> event) {
        USUtil.playUiSound(this.getPlayer(), event.value());
    }


    default MutableText texture(Text possibleTexture) {
        return texture(this.getPlayer(), possibleTexture);
    }

    default boolean hasTexture() {
        return hasTexture(this.getPlayer());
    }

    static boolean hasTexture(ServerPlayerEntity player) {
        return PolymerResourcePackUtils.hasMainPack(player) || PolymerServerNetworking.getMetadata(player.networkHandler, UniversalShopsMod.HELLO_PACKET, NbtInt.TYPE) != null;
    }

    static MutableText texture(ServerPlayerEntity player, Text possibleTexture) {
        if (hasTexture(player)) {
            return Text.empty().append(possibleTexture);
        } else {
            return Text.empty();
        }
    }
}
