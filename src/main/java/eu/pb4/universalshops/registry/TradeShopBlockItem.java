package eu.pb4.universalshops.registry;

import com.mojang.authlib.GameProfile;
import eu.pb4.polymer.core.api.block.PolymerHeadBlock;
import eu.pb4.polymer.core.api.item.PolymerHeadBlockItem;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

public class TradeShopBlockItem extends PolymerHeadBlockItem {
    public <T extends Block & PolymerHeadBlock> TradeShopBlockItem(T block, Properties settings) {
        super(block, settings);
    }

    @Override
    protected boolean updateCustomBlockEntityTag(BlockPos pos, Level world, @Nullable Player player, ItemStack stack, BlockState state) {
        var x = super.updateCustomBlockEntityTag(pos, world, player, stack, state);

        if (world.getBlockEntity(pos) instanceof TradeShopBlockEntity shop) {
            if (!x && player instanceof ServerPlayer serverPlayer) {
                shop.owner = new GameProfile(player.getGameProfile().id(), player.getGameProfile().name());
                shop.openSettings(serverPlayer);
            }
        }

        return x;
    }
}
