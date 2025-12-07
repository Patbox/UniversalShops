package eu.pb4.universalshops.mixin;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.types.templates.TypeTemplate;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Map;
import java.util.function.Supplier;
import net.minecraft.util.datafix.fixes.References;
import net.minecraft.util.datafix.schemas.V1460;

@Mixin(V1460.class)
public abstract class V1460Mixin extends Schema {
    public V1460Mixin(int versionKey, Schema parent) {
        super(versionKey, parent);
    }

    @Inject(method = "registerBlockEntities", at = @At("RETURN"))
    private void registerPolyFactoryBlockEntities(Schema schema, CallbackInfoReturnable<Map<String, Supplier<TypeTemplate>>> cir) {
        var map = cir.getReturnValue();

        schema.register(map, "universal_shops:trade_block", () -> DSL.optionalFields(
                    "PriceValue", DSL.optionalFields(
                            "Value", References.ITEM_STACK.in(schema),
                            "CurrencyContainer", DSL.list(References.ITEM_STACK.in(schema))
                    ),
                    "StockValue", References.ITEM_STACK.in(schema)
            )
        );
    }
}