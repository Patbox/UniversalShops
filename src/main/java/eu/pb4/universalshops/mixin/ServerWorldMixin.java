package eu.pb4.universalshops.mixin;

import eu.pb4.universalshops.other.USUtil;
import eu.pb4.universalshops.registry.TradeShopBlockEntity;
import me.lucko.fabric.api.permissions.v0.Permissions;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.profiler.Profiler;
import net.minecraft.util.registry.RegistryEntry;
import net.minecraft.util.registry.RegistryKey;
import net.minecraft.world.MutableWorldProperties;
import net.minecraft.world.World;
import net.minecraft.world.dimension.DimensionType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.function.Supplier;

@Mixin(ServerWorld.class)
public abstract class ServerWorldMixin extends World {
    protected ServerWorldMixin(MutableWorldProperties properties, RegistryKey<World> registryRef, RegistryEntry<DimensionType> dimension, Supplier<Profiler> profiler, boolean isClient, boolean debugWorld, long seed, int maxChainedNeighborUpdates) {
        super(properties, registryRef, dimension, profiler, isClient, debugWorld, seed, maxChainedNeighborUpdates);
    }

    @Inject(method = "canPlayerModifyAt", at = @At("HEAD"), cancellable = true)
    private void universalShops$canModify(PlayerEntity player, BlockPos pos, CallbackInfoReturnable<Boolean> cir) {
        if (this.getBlockEntity(pos) instanceof TradeShopBlockEntity be && (!be.isOwner(player) && USUtil.checkAdmin(player, "break_other"))) {
            cir.setReturnValue(false);
        }
    }
}