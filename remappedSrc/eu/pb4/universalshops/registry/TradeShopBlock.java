package eu.pb4.universalshops.registry;

import eu.pb4.polymer.core.api.block.PolymerHeadBlock;
import net.fabricmc.fabric.api.block.BlockAttackInteractionAware;
import net.minecraft.block.*;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityTicker;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.EnumProperty;
import net.minecraft.state.property.Property;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.ItemScatterer;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

public class TradeShopBlock extends BlockWithEntity implements PolymerHeadBlock, BlockAttackInteractionAware {
    public static Property<Direction> ATTACHED = EnumProperty.of("attachment", Direction.class, (x) -> x != Direction.UP);
    public final boolean isAdmin;

    protected TradeShopBlock(boolean isAdmin, Settings settings) {
        super(settings);
        this.isAdmin = isAdmin;
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        super.appendProperties(builder);
        builder.add(ATTACHED);
    }

    @Override
    public float calcBlockBreakingDelta(BlockState state, PlayerEntity player, BlockView world, BlockPos pos) {
        if (world.getBlockEntity(pos) instanceof TradeShopBlockEntity shop && shop.isOwner(player) && shop.priceHandler.canSwitch()) {
            return super.calcBlockBreakingDelta(state, player, world, pos);
        }

        return -1;
    }

    @Nullable
    @Override
    public BlockState getPlacementState(ItemPlacementContext ctx) {
        var face = ctx.getSide().getOpposite();
        if (face == Direction.UP) {
            face = Direction.DOWN;
        }
        return this.getDefaultState().with(ATTACHED, face);
    }

    @Nullable
    @Override
    public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
        return new TradeShopBlockEntity(pos, state);
    }

    @Override
    public String getPolymerSkinValue(BlockState state, BlockPos pos, ServerPlayerEntity player) {
        return isAdmin
                ? "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZGY0ZmNkOTU5OGExZmQzOWMyOGY1MDgwOWU0Y2U4YTEzMjJhZDQ4YTlhYmQ4YzIzZjIxMTg4Y2UwYTgyODhlNCJ9fX0="
                : "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvN2UzZGViNTdlYWEyZjRkNDAzYWQ1NzI4M2NlOGI0MTgwNWVlNWI2ZGU5MTJlZTJiNGVhNzM2YTlkMWY0NjVhNyJ9fX0=";
    }

    @Override
    public Block getPolymerBlock(BlockState state) {
        return Blocks.PLAYER_HEAD;
    }

    @Override
    public BlockState getPolymerBlockState(BlockState state) {
        var dir = state.get(ATTACHED);

        if (dir == Direction.DOWN) {
            return Blocks.PLAYER_HEAD.getDefaultState();
        } else {
            return Blocks.PLAYER_WALL_HEAD.getDefaultState().with(WallPlayerSkullBlock.FACING, dir.getOpposite());
        }
    }

    @Override
    public void onStateReplaced(BlockState state, World world, BlockPos pos, BlockState newState, boolean moved) {
        if (!state.isOf(newState.getBlock())) {
            BlockEntity blockEntity = world.getBlockEntity(pos);
            if (blockEntity instanceof TradeShopBlockEntity shop && shop.priceHandler.usesInventory()) {
                ItemScatterer.spawn(world, pos, shop.priceHandler.getInventory());
                world.updateComparators(pos, this);
            }

            super.onStateReplaced(state, world, pos, newState, moved);
        }
    }

    @Override
    public ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit) {
        if (hand == Hand.OFF_HAND) {
            return ActionResult.FAIL;
        }

        if (player instanceof ServerPlayerEntity serverPlayer && world.getBlockEntity(pos) instanceof TradeShopBlockEntity be) {
            be.openGui(serverPlayer);
            return ActionResult.SUCCESS;
        }

        return super.onUse(state, world, pos, player, hand, hit);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(World world, BlockState state, BlockEntityType<T> type) {
        return type == USRegistry.BLOCK_ENTITY_TYPE && world instanceof ServerWorld ? this::tick : null;
    }

    private <T extends BlockEntity> void tick(World world, BlockPos pos, BlockState state, T t) {
        ((TradeShopBlockEntity) t).tick();
    }

    @Override
    public boolean onAttackInteraction(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, Direction direction) {
        if (world.getBlockEntity(pos) instanceof TradeShopBlockEntity shop && shop.isOwner(player) && shop.priceHandler.canSwitch()) {
            return false;
        }

        return true;
    }
}
