package eu.pb4.universalshops.registry;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import eu.pb4.polymer.core.api.block.PolymerHeadBlock;
import net.fabricmc.fabric.api.block.BlockAttackInteractionAware;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.PlayerWallHeadBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;
import xyz.nucleoid.packettweaker.PacketContext;

public class TradeShopBlock extends BaseEntityBlock implements PolymerHeadBlock, BlockAttackInteractionAware {
    public static Property<Direction> ATTACHED = EnumProperty.create("attachment", Direction.class, (x) -> x != Direction.UP);
    public final boolean isAdmin;

    public static final MapCodec<TradeShopBlock> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            Codec.BOOL.fieldOf("is_admin").forGetter(x -> x.isAdmin),
            propertiesCodec()
    ).apply(instance, TradeShopBlock::new));

    protected TradeShopBlock(boolean isAdmin, Properties settings) {
        super(settings);
        this.isAdmin = isAdmin;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(ATTACHED);
    }

    @Override
    public float getDestroyProgress(BlockState state, Player player, BlockGetter world, BlockPos pos) {
        if (world.getBlockEntity(pos) instanceof TradeShopBlockEntity shop && shop.isOwner(player) && shop.priceHandler.canSwitch()) {
            return super.getDestroyProgress(state, player, world, pos);
        }

        return -1;
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext ctx) {
        var face = ctx.getClickedFace().getOpposite();
        if (face == Direction.UP) {
            face = Direction.DOWN;
        }
        return this.defaultBlockState().setValue(ATTACHED, face);
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new TradeShopBlockEntity(pos, state);
    }

    @Override
    public String getPolymerSkinValue(BlockState state, BlockPos pos, PacketContext context) {
        return isAdmin
                ? "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZGY0ZmNkOTU5OGExZmQzOWMyOGY1MDgwOWU0Y2U4YTEzMjJhZDQ4YTlhYmQ4YzIzZjIxMTg4Y2UwYTgyODhlNCJ9fX0="
                : "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvN2UzZGViNTdlYWEyZjRkNDAzYWQ1NzI4M2NlOGI0MTgwNWVlNWI2ZGU5MTJlZTJiNGVhNzM2YTlkMWY0NjVhNyJ9fX0=";
    }

    @Override
    public BlockState getPolymerBlockState(BlockState state, PacketContext context) {
        var dir = state.getValue(ATTACHED);

        if (dir == Direction.DOWN) {
            return Blocks.PLAYER_HEAD.defaultBlockState();
        } else {
            return Blocks.PLAYER_WALL_HEAD.defaultBlockState().setValue(PlayerWallHeadBlock.FACING, dir.getOpposite());
        }
    }

    @Override
    public void affectNeighborsAfterRemoval(BlockState state, ServerLevel world, BlockPos pos, boolean moved) {
        world.updateNeighbourForOutputSignal(pos, this);
        super.affectNeighborsAfterRemoval(state, world, pos,  moved);
    }

    @Override
    public InteractionResult useWithoutItem(BlockState state, Level world, BlockPos pos, Player player, BlockHitResult hit) {
        if (player instanceof ServerPlayer serverPlayer && world.getBlockEntity(pos) instanceof TradeShopBlockEntity be) {
            be.openGui(serverPlayer);
            return InteractionResult.SUCCESS_SERVER;
        }

        return super.useWithoutItem(state, world, pos, player, hit);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level world, BlockState state, BlockEntityType<T> type) {
        return type == USRegistry.BLOCK_ENTITY_TYPE && world instanceof ServerLevel ? this::tick : null;
    }

    private <T extends BlockEntity> void tick(Level world, BlockPos pos, BlockState state, T t) {
        ((TradeShopBlockEntity) t).tick();
    }

    @Override
    public boolean onAttackInteraction(BlockState state, Level world, BlockPos pos, Player player, InteractionHand hand, Direction direction) {
        if (world.getBlockEntity(pos) instanceof TradeShopBlockEntity shop && shop.isOwner(player) && shop.priceHandler.canSwitch()) {
            return false;
        }

        return true;
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }
}
