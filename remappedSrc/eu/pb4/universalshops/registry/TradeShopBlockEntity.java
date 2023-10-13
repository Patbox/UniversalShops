package eu.pb4.universalshops.registry;

import Z;
import com.mojang.authlib.GameProfile;
import eu.pb4.common.protection.api.CommonProtection;
import eu.pb4.polymer.virtualentity.api.ElementHolder;
import eu.pb4.polymer.virtualentity.api.attachment.ChunkAttachment;
import eu.pb4.polymer.virtualentity.api.elements.ItemDisplayElement;
import eu.pb4.polymer.virtualentity.api.elements.TextDisplayElement;
import eu.pb4.universalshops.gui.setup.ShopSettingsGui;
import eu.pb4.universalshops.other.EmptyInventory;
import eu.pb4.universalshops.other.RemappedInventory;
import eu.pb4.universalshops.other.TextUtil;
import eu.pb4.universalshops.other.USUtil;
import eu.pb4.universalshops.trade.PriceHandler;
import eu.pb4.universalshops.trade.StockHandler;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.client.render.model.json.ModelTransformationMode;
import net.minecraft.entity.decoration.DisplayEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.InventoryChangedListener;
import net.minecraft.inventory.SidedInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtHelper;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.MutableText;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.chunk.WorldChunk;
import org.jetbrains.annotations.Nullable;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import java.util.stream.IntStream;

public class TradeShopBlockEntity extends BlockEntity implements RemappedInventory, SidedInventory, InventoryChangedListener {
    private final TextDisplayElement textDisplay = new TextDisplayElement();
    private final ItemDisplayElement itemDisplay = new ItemDisplayElement();
    @Nullable
    public BlockPos containerPos;
    public boolean allowHoppers = false;
    public PriceHandler priceHandler = PriceHandler.Invalid.DEFINITION.createInitial(this);
    public StockHandler stockHandler = StockHandler.Invalid.DEFINITION.createInitial(this);
    public GameProfile owner;
    public HologramMode hologramMode = HologramMode.FULL;
    private int[] cachedSlots = new int[0];
    private ElementHolder elementHolder;

    public TradeShopBlockEntity(BlockPos pos, BlockState state) {
        super(USRegistry.BLOCK_ENTITY_TYPE, pos, state);
        this.containerPos = pos.offset(state.get(TradeShopBlock.ATTACHED));
        this.textDisplay.setBillboardMode(DisplayEntity.BillboardMode.CENTER);
        this.textDisplay.setViewRange(0.5f);
        this.itemDisplay.setBillboardMode(DisplayEntity.BillboardMode.CENTER);
        this.itemDisplay.setModelTransformation(ModelTransformationMode.GUI);
        this.itemDisplay.setLeftRotation(new Quaternionf().rotateY(MathHelper.PI));
        this.itemDisplay.setViewRange(0.5f);
        this.itemDisplay.setScale(new Vector3f(0.6f, 0.6f, 0.01f));
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
        if (this.hologramMode == HologramMode.DISABLED) {
            if (this.elementHolder != null) {
                this.elementHolder.destroy();
                this.elementHolder = null;
            }
            return;
        }

        if (elementHolder == null) {
            var dir = this.getCachedState().get(TradeShopBlock.ATTACHED);
            var pos = Vec3d.ofCenter(this.getPos(), dir == Direction.DOWN ? 0.6 : 0.8);
            if (dir != Direction.DOWN) {
                pos = pos.add(dir.getOffsetX() * 0.25, 0, dir.getOffsetZ() * 0.25);
            }
            elementHolder = new ElementHolder();

            ChunkAttachment.of(elementHolder, (ServerWorld) world, pos);
        }

        if (this.isFullySetup()) {
            if (this.hologramMode == HologramMode.FULL) {
                if ((this.world.getTime() % 32) != 0) {
                    return;
                }

                var hasStock = this.stockHandler.getMaxAmount(null) != 0;

                this.itemDisplay.setItem(this.stockHandler.icon());

                int lines = 2;
                var text = Text.empty()
                        .append(this.stockHandler.getStockName())
                        .append("\n")
                        .append(TextUtil.text("price",
                                this.priceHandler.getText().copy().setStyle(Style.EMPTY.withFormatting(Formatting.WHITE).withBold(false))
                        ).setStyle(Style.EMPTY.withFormatting(Formatting.DARK_GREEN).withBold(true)));

                if (!hasStock) {
                    lines++;
                    text.append("\n").append((this.getContainer() != EmptyInventory.INSTANCE ? TextUtil.gui("out_of_stock") : TextUtil.text("stock_missing")).formatted(Formatting.RED));
                }
                this.textDisplay.setText(text);
                this.itemDisplay.setTranslation(new Vector3f(0, 0.25f + 0.28f * lines, 0));

                if (this.itemDisplay.getHolder() != elementHolder) {
                    elementHolder.addElement(this.itemDisplay);
                }

                if (this.textDisplay.getHolder() != elementHolder) {
                    elementHolder.addElement(this.textDisplay);
                }

            } else {
                if ((this.world.getTime() % 12) != 0) {
                    return;
                }

                var icon = this.stockHandler.getMaxAmount(null) != 0 || ((this.world.getTime() / 12) & 2) == 0 ? this.stockHandler.icon() : Items.BARRIER.getDefaultStack();

                itemDisplay.setItem(icon);
                this.itemDisplay.setTranslation(new Vector3f(0, 0.25f, 0));

                if (this.itemDisplay.getHolder() != elementHolder) {
                    elementHolder.addElement(this.itemDisplay);
                }

                if (this.textDisplay.getHolder() != null) {
                    elementHolder.removeElement(this.textDisplay);
                }
            }

        } else {
            if ((this.world.getTime() % 32) != 0) {
                return;
            }

            this.textDisplay.setText(TextUtil.text("requires_setup_by_owner").formatted(Formatting.RED));
            if (this.itemDisplay.getHolder() != null) {
                elementHolder.removeElement(this.itemDisplay);
            }

            if (this.textDisplay.getHolder() != elementHolder) {
                elementHolder.addElement(this.textDisplay);
            }
        }

        elementHolder.tick();
    }


    @Override
    public void markRemoved() {
        super.markRemoved();
        if (this.elementHolder != null) {
            this.elementHolder.destroy();
            this.elementHolder = null;
            this.itemDisplay.setHolder(null);
            this.textDisplay.setHolder(null);
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

    @Override
    public void onInventoryChanged(Inventory sender) {
        this.markDirty();
    }

    public enum HologramMode {
        FULL,
        ICON,
        DISABLED
    }

}
