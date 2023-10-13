package eu.pb4.universalshops.registry;

import eu.pb4.polymer.core.api.block.PolymerBlockUtils;
import eu.pb4.universalshops.UniversalShopsMod;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroups;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;

public class USRegistry {

    public static final TradeShopBlock BLOCK = register("trade_block", new TradeShopBlock(false, AbstractBlock.Settings.create().nonOpaque().strength(2.8F, 3600000.0F)), Registries.BLOCK);
    public static final TradeShopBlock BLOCK_ADMIN = register("admin_trade_block", new TradeShopBlock(true, AbstractBlock.Settings.create().nonOpaque().strength(-1.0F, 3600000.0F).dropsNothing()), Registries.BLOCK);
    public static final BlockEntityType<TradeShopBlockEntity> BLOCK_ENTITY_TYPE = register("trade_block", FabricBlockEntityTypeBuilder.create(TradeShopBlockEntity::new, BLOCK, BLOCK_ADMIN).build(), Registries.BLOCK_ENTITY_TYPE);
    public static final TradeShopBlockItem ITEM = register("trade_block",
            new TradeShopBlockItem(BLOCK, new Item.Settings()), Registries.ITEM);
    public static final TradeShopBlockItem ITEM_ADMIN = register("admin_trade_block",
            new TradeShopBlockItem(BLOCK_ADMIN, new Item.Settings()), Registries.ITEM);

    public static <A extends T, T> A register(String key, A value, Registry<T> registry) {
        if (value instanceof BlockEntityType<?> blockEntityType) {
            PolymerBlockUtils.registerBlockEntity(blockEntityType);
        }
        return Registry.register(registry, UniversalShopsMod.id(key), value);
    }

    public static void register() {
        ItemGroupEvents.modifyEntriesEvent(ItemGroups.FUNCTIONAL).register((c) -> {
            c.add(ITEM);

            if (c.shouldShowOpRestrictedItems()) {
                c.add(ITEM_ADMIN);
            }
        });
    };
}
