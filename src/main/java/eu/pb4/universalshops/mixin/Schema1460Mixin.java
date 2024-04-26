package eu.pb4.universalshops.mixin;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.types.templates.TypeTemplate;
import net.minecraft.datafixer.TypeReferences;
import net.minecraft.datafixer.schema.Schema1460;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Map;
import java.util.function.Supplier;

@Mixin(Schema1460.class)
public abstract class Schema1460Mixin extends Schema {
    public Schema1460Mixin(int versionKey, Schema parent) {
        super(versionKey, parent);
    }

    @Inject(method = "registerBlockEntities", at = @At("RETURN"))
    private void registerPolyFactoryBlockEntities(Schema schema, CallbackInfoReturnable<Map<String, Supplier<TypeTemplate>>> cir) {
        var map = cir.getReturnValue();

        schema.register(map, "universal_shops:trade_block", () -> DSL.optionalFields(
                    "PriceValue", DSL.optionalFields(
                            "Value", TypeReferences.ITEM_STACK.in(schema),
                            "CurrencyContainer", DSL.list(TypeReferences.ITEM_STACK.in(schema))
                    ),
                    "StockValue", TypeReferences.ITEM_STACK.in(schema)
            )
        );
    }
}