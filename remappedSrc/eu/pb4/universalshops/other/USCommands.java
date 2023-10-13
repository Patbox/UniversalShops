package eu.pb4.universalshops.other;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import eu.pb4.universalshops.GenericModInfo;
import eu.pb4.universalshops.registry.USRegistry;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

import Z;

public class USCommands {

    public static void register(CommandDispatcher<ServerCommandSource> dispatcher, CommandRegistryAccess registryAccess, CommandManager.RegistrationEnvironment environment) {
        dispatcher.register(
                literal("universal_shops")
                        .requires(USUtil.requireDefault("command"))
                        .executes(USCommands::about)
                        .then(literal("get")
                                .requires(USUtil.requireAdmin("admin"))
                                .then(argument("admin", BoolArgumentType.bool())
                                        .executes(USCommands::getShop)
                                )
                        )
        );
    }

    private static int getShop(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        var player = context.getSource().getPlayerOrThrow();

        var admin = BoolArgumentType.getBool(context, "admin");

        player.giveItemStack((admin ? USRegistry.ITEM_ADMIN : USRegistry.ITEM).getDefaultStack());

        return 0;
    }

    private static int about(CommandContext<ServerCommandSource> context) {
        for (var text : context.getSource().getEntity() instanceof ServerPlayerEntity ? GenericModInfo.getAboutFull() : GenericModInfo.getAboutConsole()) {
            context.getSource().sendFeedback(() -> text, false);
        }

        return 1;
    }
}
