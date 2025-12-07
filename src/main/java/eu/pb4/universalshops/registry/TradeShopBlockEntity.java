package eu.pb4.universalshops.registry;

import com.mojang.authlib.GameProfile;
import eu.pb4.common.protection.api.CommonProtection;
import eu.pb4.polymer.virtualentity.api.ElementHolder;
import eu.pb4.polymer.virtualentity.api.attachment.ChunkAttachment;
import eu.pb4.polymer.virtualentity.api.elements.ItemDisplayElement;
import eu.pb4.polymer.virtualentity.api.elements.TextDisplayElement;
import eu.pb4.universalshops.gui.setup.ShopSettingsGui;
import eu.pb4.universalshops.other.*;
import eu.pb4.universalshops.trade.PriceHandler;
import eu.pb4.universalshops.trade.StockHandler;
import org.jetbrains.annotations.Nullable;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import java.util.stream.IntStream;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.Container;
import net.minecraft.world.ContainerListener;
import net.minecraft.world.Containers;
import net.minecraft.world.WorldlyContainer;
import net.minecraft.world.entity.Display;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.Vec3;

public class TradeShopBlockEntity extends BlockEntity implements RemappedInventory, WorldlyContainer, ContainerListener {
    private final TextDisplayElement textDisplay = new TextDisplayElement();
    private final ItemDisplayElement itemDisplay = new ItemDisplayElement();
    @Nullable
    public BlockPos containerPos;
    public boolean allowHoppers = false;
    public PriceHandler priceHandler = PriceHandler.Invalid.DEFINITION.createInitial(this);
    public StockHandler stockHandler = StockHandler.Invalid.DEFINITION.createInitial(this);
    @Nullable
    public GameProfile owner;
    public HologramMode hologramMode = HologramMode.FULL;
    private int[] cachedSlots = new int[0];
    private ElementHolder elementHolder;

    public TradeShopBlockEntity(BlockPos pos, BlockState state) {
        super(USRegistry.BLOCK_ENTITY_TYPE, pos, state);
        this.containerPos = pos.relative(state.getValue(TradeShopBlock.ATTACHED));
        this.textDisplay.setBillboardMode(Display.BillboardConstraints.CENTER);
        this.textDisplay.setViewRange(0.5f);
        this.itemDisplay.setBillboardMode(Display.BillboardConstraints.CENTER);
        this.itemDisplay.setItemDisplayContext(ItemDisplayContext.GUI);
        this.itemDisplay.setLeftRotation(new Quaternionf().rotateY(Mth.PI));
        this.itemDisplay.setViewRange(0.5f);
        this.itemDisplay.setScale(new Vector3f(0.6f, 0.6f, 0.01f));
    }

    @Override
    protected void saveAdditional(ValueOutput view) {
        if (this.containerPos != null) {
            view.store("ItemContainerPos", CompoundTag.CODEC, LegacyNbtHelper.fromBlockPos(this.containerPos));
        }
        if (this.owner != null) {
            view.store("Owner", CompoundTag.CODEC, LegacyNbtHelper.writeGameProfile(new CompoundTag(), this.owner));
        }

        this.priceHandler.writeData(view);
        this.stockHandler.writeData(view);
        view.putBoolean("AllowHoppers", this.allowHoppers);
        view.putString("HologramMode", this.hologramMode.name());
    }

    @Override
    public void loadAdditional(ValueInput view) {
        this.containerPos = view.read("ItemContainerPos", CompoundTag.CODEC).map(LegacyNbtHelper::toBlockPos).orElse(null);


            this.owner = view.read("Owner", CompoundTag.CODEC).map(LegacyNbtHelper::toGameProfile).orElse(null);
        try {
            this.hologramMode = HologramMode.valueOf(view.getStringOr("HologramMode", ""));
        } catch (Throwable e) {

        }

        try {
            this.priceHandler = PriceHandler.readView(view, this);
        } catch (Throwable e) {
            e.printStackTrace();
        }
        try {
            this.stockHandler = StockHandler.readData(view, this);
        } catch (Throwable e) {
            e.printStackTrace();

        }
        this.allowHoppers = view.getBooleanOr("AllowHoppers", false);
    }

    public void tick() {
        this.tickHolo();
    }

    @Override
    public void preRemoveSideEffects(BlockPos pos, BlockState oldState) {
        super.preRemoveSideEffects(pos, oldState);
        if (this.priceHandler.usesInventory() && this.level != null) {
            Containers.dropContents(level, pos, this.priceHandler.getInventory());
        }
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
            var dir = this.getBlockState().getValue(TradeShopBlock.ATTACHED);
            var pos = Vec3.upFromBottomCenterOf(this.getBlockPos(), dir == Direction.DOWN ? 0.6 : 0.8);
            if (dir != Direction.DOWN) {
                pos = pos.add(dir.getStepX() * 0.25, 0, dir.getStepZ() * 0.25);
            }
            elementHolder = new ElementHolder();

            ChunkAttachment.of(elementHolder, (ServerLevel) level, pos);
        }

        if (this.isFullySetup()) {
            if (this.hologramMode == HologramMode.FULL) {
                if ((this.level.getGameTime() % 32) != 0) {
                    return;
                }

                var hasStock = this.stockHandler.getMaxAmount(null) != 0;

                this.itemDisplay.setItem(this.stockHandler.icon());

                int lines = 2;
                var text = Component.empty()
                        .append(this.stockHandler.getStockName())
                        .append("\n")
                        .append(TextUtil.text("price",
                                this.priceHandler.getText().copy().setStyle(Style.EMPTY.applyFormat(ChatFormatting.WHITE).withBold(false))
                        ).setStyle(Style.EMPTY.applyFormat(ChatFormatting.DARK_GREEN).withBold(true)));

                if (!hasStock) {
                    lines++;
                    text.append("\n").append((this.getContainer() != EmptyInventory.INSTANCE ? TextUtil.gui("out_of_stock") : TextUtil.text("stock_missing")).withStyle(ChatFormatting.RED));
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
                if ((this.level.getGameTime() % 12) != 0) {
                    return;
                }

                var icon = this.stockHandler.getMaxAmount(null) != 0 || ((this.level.getGameTime() / 12) & 2) == 0 ? this.stockHandler.icon() : Items.BARRIER.getDefaultInstance();

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
            if ((this.level.getGameTime() % 32) != 0) {
                return;
            }

            this.textDisplay.setText(TextUtil.text("requires_setup_by_owner").withStyle(ChatFormatting.RED));
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
    public void setRemoved() {
        super.setRemoved();
        if (this.elementHolder != null) {
            this.elementHolder.destroy();
            this.elementHolder = null;
            this.itemDisplay.setHolder(null);
            this.textDisplay.setHolder(null);
        }
    }

    public void openGui(ServerPlayer serverPlayer) {
        if (this.isFullySetup()) {
            this.stockHandler.openTradeGui(serverPlayer);
        } else if (this.isOwner(serverPlayer)) {
            openSettings(serverPlayer);
        } else {
            serverPlayer.sendSystemMessage(TextUtil.prefix(TextUtil.text("requires_setup_by_owner").withStyle(ChatFormatting.RED)));
        }
    }

    public boolean isOwner(Player player) {
        return isAdmin() ? USUtil.checkAdmin(player, "admin") : this.isOwner(player.getGameProfile());
    }

    public boolean isOwner(GameProfile gameProfile) {
        return !isAdmin() && this.owner != null && this.owner.id().equals(gameProfile.id());
    }

    public boolean isFullySetup() {
        return this.priceHandler.isSetup() && this.stockHandler.isSetup();
    }

    public Container getContainer() {
        if (this.containerPos != null && this.level != null && CommonProtection.canInteractBlock(this.level, this.containerPos, this.owner, null)) {
            var inv = USUtil.getInventoryAt(this.level, this.containerPos);
            if (inv != null) {
                return inv;
            }
        }

        return EmptyInventory.INSTANCE;
    }

    public void openSettings(ServerPlayer player) {
        new ShopSettingsGui(player, this);
    }

    public Component getTitle() {
        return TextUtil.gui("shop.title" + (this.isAdmin() ? ".admin" : ""), this.owner != null ? this.owner.name() : Component.literal("<???>"));
    }

    public boolean isAdmin() {
        return ((TradeShopBlock) this.getBlockState().getBlock()).isAdmin;
    }

    @Override
    public Container getInventory() {
        return this.allowHoppers ? this.priceHandler.getInventory() : EmptyInventory.INSTANCE;
    }

    @Override
    public int[] getSlotsForFace(Direction side) {
        if (this.getInventory().getContainerSize() != this.cachedSlots.length) {
            this.cachedSlots = IntStream.range(0, this.getInventory().getContainerSize()).toArray();
        }

        return this.cachedSlots;
    }

    @Override
    public boolean canPlaceItemThroughFace(int slot, ItemStack stack, @Nullable Direction dir) {
        return false;
    }

    @Override
    public boolean canTakeItemThroughFace(int slot, ItemStack stack, Direction dir) {
        return this.allowHoppers;
    }

    @Override
    public void containerChanged(Container sender) {
        this.setChanged();
    }

    public enum HologramMode {
        FULL,
        ICON,
        DISABLED
    }

}
