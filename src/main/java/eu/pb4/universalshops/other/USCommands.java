package eu.pb4.universalshops.other;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import eu.pb4.universalshops.GenericModInfo;
import eu.pb4.universalshops.registry.USRegistry;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.server.level.ServerPlayer;

import static net.minecraft.commands.Commands.argument;
import static net.minecraft.commands.Commands.literal;

public class USCommands {

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher, CommandBuildContext registryAccess, Commands.CommandSelection environment) {
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

    private static int getShop(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        var player = context.getSource().getPlayerOrException();

        var admin = BoolArgumentType.getBool(context, "admin");

        player.addItem((admin ? USRegistry.ITEM_ADMIN : USRegistry.ITEM).getDefaultInstance());

        return 0;
    }

    private static int about(CommandContext<CommandSourceStack> context) {
        for (var text : context.getSource().getEntity() instanceof ServerPlayer ? GenericModInfo.getAboutFull() : GenericModInfo.getAboutConsole()) {
            context.getSource().sendSuccess(() -> text, false);
        }

        return 1;
    }
}
