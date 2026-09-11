package eu.pb4.universalshops.other;

import eu.pb4.universalshops.registry.TradeShopBlockEntity;
import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.CompoundContainer;
import net.minecraft.world.Container;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

public final class ShopStorageAccess {
    private ShopStorageAccess() {
    }

    public static void register() {
        UseBlockCallback.EVENT.register((player, level, hand, hitResult) -> {
            if (!level.isClientSide() && player instanceof ServerPlayer serverPlayer
                    && isProtectedFrom(level, hitResult.getBlockPos(), serverPlayer)) {
                denyAccess(serverPlayer);
                return InteractionResult.FAIL;
            }

            return InteractionResult.PASS;
        });

        PlayerBlockBreakEvents.BEFORE.register((level, player, pos, state, blockEntity) -> {
            if (isProtectedFrom(level, pos, player)) {
                if (player instanceof ServerPlayer serverPlayer) {
                    denyAccess(serverPlayer);
                }
                return false;
            }

            return true;
        });
    }

    private static boolean isProtectedFrom(Level level, BlockPos accessedPos, Player player) {
        if (!(level.getBlockEntity(accessedPos) instanceof Container)) {
            return false;
        }

        for (Direction storageOffset : Direction.values()) {
            var linkedStoragePos = accessedPos.relative(storageOffset);
            if (isSameStorage(level, linkedStoragePos, accessedPos)
                    && hasShopNotOwnedBy(level, linkedStoragePos, player)) {
                return true;
            }
        }

        return hasShopNotOwnedBy(level, accessedPos, player);
    }

    private static boolean hasShopNotOwnedBy(Level level, BlockPos storagePos, Player player) {
        for (Direction direction : Direction.values()) {
            if (level.getBlockEntity(storagePos.relative(direction)) instanceof TradeShopBlockEntity shop
                    && storagePos.equals(shop.containerPos)
                    && shop.protectStorage
                    && !shop.isOwner(player)) {
                return true;
            }
        }

        return false;
    }

    private static boolean isSameStorage(Level level, BlockPos linkedStoragePos, BlockPos accessedPos) {
        if (!(level.getBlockEntity(accessedPos) instanceof Container accessedContainer)) {
            return false;
        }

        var linkedContainer = USUtil.getInventoryAt(level, linkedStoragePos);
        return linkedContainer == accessedContainer
                || linkedContainer instanceof CompoundContainer compoundContainer
                && compoundContainer.contains(accessedContainer);
    }

    private static void denyAccess(ServerPlayer player) {
        player.sendSystemMessage(TextUtil.prefix(TextUtil.text("shop_storage_owner_only").withStyle(ChatFormatting.RED)));
    }
}
