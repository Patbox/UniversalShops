package eu.pb4.universalshops.registry;

import eu.pb4.polymer.api.block.PolymerBlockUtils;
import eu.pb4.universalshops.UniversalShopsMod;
import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Material;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.util.registry.Registry;

public class USRegistry {

    public static final TradeShopBlock BLOCK = register("trade_block", new TradeShopBlock(false, AbstractBlock.Settings.of(Material.WOOD).nonOpaque().strength(2.8F, 3600000.0F)), Registry.BLOCK);
    public static final TradeShopBlock BLOCK_ADMIN = register("admin_trade_block", new TradeShopBlock(true, AbstractBlock.Settings.of(Material.WOOD).nonOpaque().strength(-1.0F, 3600000.0F).dropsNothing()), Registry.BLOCK);
    public static final BlockEntityType<TradeShopBlockEntity> BLOCK_ENTITY_TYPE = register("trade_block", FabricBlockEntityTypeBuilder.create(TradeShopBlockEntity::new, BLOCK, BLOCK_ADMIN).build(), Registry.BLOCK_ENTITY_TYPE);
    public static final TradeShopBlockItem ITEM = register("trade_block",
            new TradeShopBlockItem(BLOCK, new Item.Settings().group(ItemGroup.DECORATIONS)), Registry.ITEM);
    public static final TradeShopBlockItem ITEM_ADMIN = register("admin_trade_block",
            new TradeShopBlockItem(BLOCK_ADMIN, new Item.Settings().group(ItemGroup.DECORATIONS)), Registry.ITEM);

    public static <A extends T, T> A register(String key, A value, Registry<T> registry) {
        if (value instanceof BlockEntityType<?> blockEntityType) {
            PolymerBlockUtils.registerBlockEntity(blockEntityType);
        }
        return Registry.register(registry, UniversalShopsMod.id(key), value);
    }

    public static void register() {};
}
