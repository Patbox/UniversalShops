package eu.pb4.universalshops.registry;

import eu.pb4.polymer.core.api.block.PolymerBlockUtils;
import eu.pb4.universalshops.UniversalShopsMod;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import java.util.function.Function;

public class USRegistry {

    public static final TradeShopBlock BLOCK = register("trade_block", (k) -> new TradeShopBlock(false, BlockBehaviour.Properties.of().setId(k).noOcclusion().strength(2.8F, 3600000.0F)), BuiltInRegistries.BLOCK);
    public static final TradeShopBlock BLOCK_ADMIN = register("admin_trade_block", (k) -> new TradeShopBlock(true, BlockBehaviour.Properties.of().setId(k).noOcclusion().strength(-1.0F, 3600000.0F).noLootTable()), BuiltInRegistries.BLOCK);
    public static final BlockEntityType<TradeShopBlockEntity> BLOCK_ENTITY_TYPE = register("trade_block", (k) -> FabricBlockEntityTypeBuilder.create(TradeShopBlockEntity::new, BLOCK, BLOCK_ADMIN).build(), BuiltInRegistries.BLOCK_ENTITY_TYPE);
    public static final TradeShopBlockItem ITEM = register("trade_block", (k) ->
            new TradeShopBlockItem(BLOCK, new Item.Properties().setId(k).useBlockDescriptionPrefix()), BuiltInRegistries.ITEM);
    public static final TradeShopBlockItem ITEM_ADMIN = register("admin_trade_block", (k) ->
            new TradeShopBlockItem(BLOCK_ADMIN, new Item.Properties().setId(k).useBlockDescriptionPrefix()), BuiltInRegistries.ITEM);

    public static <A extends T, T> A register(String key, Function<ResourceKey<T>, A> value, Registry<T> registry) {
        var id = UniversalShopsMod.id(key);
        var v = value.apply(ResourceKey.create(registry.key(), id));
        if (v instanceof BlockEntityType<?> blockEntityType) {
            PolymerBlockUtils.registerBlockEntity(blockEntityType);
        }
        return Registry.register(registry, id, v);
    }

    public static void register() {
        ItemGroupEvents.modifyEntriesEvent(CreativeModeTabs.FUNCTIONAL_BLOCKS).register((c) -> {
            c.accept(ITEM);

            if (c.shouldShowOpRestrictedItems()) {
                c.accept(ITEM_ADMIN);
            }
        });
    };
}
