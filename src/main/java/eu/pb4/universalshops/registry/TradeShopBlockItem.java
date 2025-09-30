package eu.pb4.universalshops.registry;

import com.mojang.authlib.GameProfile;
import eu.pb4.polymer.core.api.block.PolymerHeadBlock;
import eu.pb4.polymer.core.api.item.PolymerHeadBlockItem;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

public class TradeShopBlockItem extends PolymerHeadBlockItem {
    public <T extends Block & PolymerHeadBlock> TradeShopBlockItem(T block, Settings settings) {
        super(block, settings);
    }

    @Override
    protected boolean postPlacement(BlockPos pos, World world, @Nullable PlayerEntity player, ItemStack stack, BlockState state) {
        var x = super.postPlacement(pos, world, player, stack, state);

        if (world.getBlockEntity(pos) instanceof TradeShopBlockEntity shop) {
            if (!x && player instanceof ServerPlayerEntity serverPlayer) {
                shop.owner = new GameProfile(player.getGameProfile().id(), player.getGameProfile().name());
                shop.openSettings(serverPlayer);
            }
        }

        return x;
    }
}
