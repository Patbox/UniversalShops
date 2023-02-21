package eu.pb4.universalshops.registry;

import com.mojang.authlib.GameProfile;
import eu.pb4.common.protection.api.CommonProtection;
import eu.pb4.holograms.api.Holograms;
import eu.pb4.holograms.api.elements.item.AbstractItemHologramElement;
import eu.pb4.holograms.api.holograms.WorldHologram;
import eu.pb4.universalshops.gui.setup.ShopSettingsGui;
import eu.pb4.universalshops.other.EmptyInventory;
import eu.pb4.universalshops.other.RemappedInventory;
import eu.pb4.universalshops.other.USUtil;
import eu.pb4.universalshops.other.TextUtil;
import eu.pb4.universalshops.trade.PriceHandler;
import eu.pb4.universalshops.trade.StockHandler;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.SidedInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtHelper;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.Nullable;

import java.util.stream.IntStream;

public class TradeShopBlockEntity extends BlockEntity implements RemappedInventory, SidedInventory {
    @Nullable
    public BlockPos containerPos;
    public boolean allowHoppers = false;
    public PriceHandler priceHandler = PriceHandler.Invalid.DEFINITION.createInitial(this);
    public StockHandler stockHandler = StockHandler.Invalid.DEFINITION.createInitial(this);
    public GameProfile owner;
    private WorldHologram hologram;
    public HologramMode hologramMode = HologramMode.FULL;
    private int[] cachedSlots = new int[0];

    public TradeShopBlockEntity(BlockPos pos, BlockState state) {
        super(USRegistry.BLOCK_ENTITY_TYPE, pos, state);
        this.containerPos = pos.offset(state.get(TradeShopBlock.ATTACHED));
    }

    @Override
    protected void writeNbt(NbtCompound nbt) {
        if (this.containerPos != null) {
            nbt.put("ItemContainerPos", NbtHelper.fromBlockPos(this.containerPos));
        }
        if (this.owner != null) {
            nbt.put("Owner", NbtHelper.writeGameProfile(new NbtCompound(), this.owner));
        }

        this.priceHandler.writeNbt(nbt);
        this.stockHandler.writeNbt(nbt);
        nbt.putBoolean("AllowHoppers", this.allowHoppers);
        nbt.putString("HologramMode", this.hologramMode.name());
    }

    @Override
    public void readNbt(NbtCompound nbt) {
        if (nbt.contains("ItemContainerPos")) {
            this.containerPos = NbtHelper.toBlockPos(nbt.getCompound("ItemContainerPos"));
        }

        if (nbt.contains("Owner")) {
            this.owner = NbtHelper.toGameProfile(nbt.getCompound("Owner"));
        }
        try {
            this.hologramMode = HologramMode.valueOf(nbt.getString("HologramMode"));
        } catch (Throwable e) {

        }

        try {
            this.priceHandler = PriceHandler.readNbt(nbt, this);
        } catch (Throwable e) {
            e.printStackTrace();
        }
        try {
            this.stockHandler = StockHandler.readNbt(nbt, this);
        } catch (Throwable e) {
            e.printStackTrace();

        }
        this.allowHoppers = nbt.getBoolean("AllowHoppers");
    }

    public void tick() {

        this.tickHolo();

    }

    private void tickHolo() {
        if (hologram == null) {
            var dir = this.getCachedState().get(TradeShopBlock.ATTACHED);
            var pos = Vec3d.ofCenter(this.getPos(), dir == Direction.DOWN ? 0.8 : 1);
            if (dir != Direction.DOWN) {
                pos = pos.add(dir.getOffsetX() * 0.25, 0, dir.getOffsetZ() * 0.25);
            }
            this.hologram = Holograms.create((ServerWorld) this.world, pos, new Text[0]);
        }

        if (this.hologramMode == HologramMode.DISABLED) {
            if (this.hologram.isActive()) {
                this.hologram.hide();
                this.hologram.clearElements();
            }
            return;
        }

        if (!this.hologram.isActive()) {
            this.hologram.show();
        }

        if (this.isFullySetup()) {
            if (this.hologramMode == HologramMode.FULL) {
                if ((this.world.getTime() % 32) != 0) {
                    return;
                }
                var hasStock = this.stockHandler.getMaxAmount(null) != 0;

                clearHolo(hasStock ? 3 : 4);

                var hologramElement = this.hologram.getElement(0);

                if (hologramElement instanceof AbstractItemHologramElement x) {
                    x.setItemStack(this.stockHandler.icon());
                } else {
                    this.hologram.setItemStack(0, this.stockHandler.icon(), false);
                }

                this.hologram.setText(1, this.stockHandler.getStockName());
                this.hologram.setText(2, TextUtil.text("price",
                        this.priceHandler.getText().copy().setStyle(Style.EMPTY.withFormatting(Formatting.WHITE).withBold(false))
                ).setStyle(Style.EMPTY.withFormatting(Formatting.DARK_GREEN).withBold(true)));

                if (!hasStock) {
                    this.hologram.setText(3, (this.getContainer() != EmptyInventory.INSTANCE ? TextUtil.gui("out_of_stock") : TextUtil.text("stock_missing")).formatted(Formatting.RED));
                }
            } else {
                if ((this.world.getTime() % 12) != 0) {
                    return;
                }
                clearHolo(1);

                var hologramElement = this.hologram.getElement(0);

                var icon = this.stockHandler.getMaxAmount(null) != 0 || ((this.world.getTime() / 12) & 2) == 0 ? this.stockHandler.icon() : Items.BARRIER.getDefaultStack();

                if (hologramElement instanceof AbstractItemHologramElement x) {
                    x.setItemStack(icon);
                } else {
                    this.hologram.setItemStack(0, icon, false);
                }
            }

        } else {
            if ((this.world.getTime() % 32) != 0) {
                return;
            }
            clearHolo(1);
            this.hologram.setText(0, TextUtil.text("requires_setup_by_owner").formatted(Formatting.RED));
        }
    }

    private void clearHolo(int i) {
        if (this.hologram.getElements().size() > i) {
            this.hologram.clearElements();
        }
    }

    @Override
    public void markRemoved() {
        super.markRemoved();
        if (this.hologram != null) {
            this.hologram.hide();
            this.hologram = null;
        }
    }

    public void openGui(ServerPlayerEntity serverPlayer) {
        if (this.isFullySetup()) {
            this.stockHandler.openTradeGui(serverPlayer);
        } else if (this.isOwner(serverPlayer)) {
            openSettings(serverPlayer);
        } else {
            serverPlayer.sendMessage(TextUtil.prefix(TextUtil.text("requires_setup_by_owner").formatted(Formatting.RED)));
        }
    }

    public boolean isOwner(PlayerEntity player) {
        return isAdmin() ? USUtil.checkAdmin(player, "admin") : this.isOwner(player.getGameProfile());
    }

    public boolean isOwner(GameProfile gameProfile) {
        return !isAdmin() && this.owner != null && this.owner.getId().equals(gameProfile.getId());
    }

    public boolean isFullySetup() {
        return this.priceHandler.isSetup() && this.stockHandler.isSetup();
    }

    public Inventory getContainer() {
        if (this.containerPos != null && this.world != null && CommonProtection.canInteractBlock(this.world, this.containerPos, this.owner, null)) {
            var inv = USUtil.getInventoryAt(this.world, this.containerPos);
            if (inv != null) {
                return inv;
            }
        }

        return EmptyInventory.INSTANCE;
    }

    public void openSettings(ServerPlayerEntity player) {
        new ShopSettingsGui(player, this);
    }

    public Text getTitle() {
        return TextUtil.gui("shop.title" + (this.isAdmin() ? ".admin" : ""), this.owner != null ? this.owner.getName() : Text.literal("<???>"));
    }

    public boolean isAdmin() {
        return ((TradeShopBlock) this.getCachedState().getBlock()).isAdmin;
    }

    @Override
    public Inventory getInventory() {
        return this.allowHoppers ? this.priceHandler.getInventory() : EmptyInventory.INSTANCE;
    }

    @Override
    public int[] getAvailableSlots(Direction side) {
        if (this.getInventory().size() != this.cachedSlots.length) {
            this.cachedSlots = IntStream.range(0, this.getInventory().size()).toArray();
        }

        return this.cachedSlots;
    }

    @Override
    public boolean canInsert(int slot, ItemStack stack, @Nullable Direction dir) {
        return false;
    }

    @Override
    public boolean canExtract(int slot, ItemStack stack, Direction dir) {
        return this.allowHoppers;
    }

    public enum HologramMode {
        FULL,
        ICON,
        DISABLED
    }

}
