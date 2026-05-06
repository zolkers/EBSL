package fr.riege.ebsl.fabric;

import com.mojang.brigadier.arguments.StringArgumentType;
import fr.riege.ebsl.common.layer.ICommandLayer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;

import java.util.Locale;

public class FabricCommandLayer implements ICommandLayer {
    @Override
    public void register(String name, String description, CommandHandler handler) {
        String commandName = name.toLowerCase(Locale.ROOT);
        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> dispatcher.register(
            ClientCommandManager.literal(commandName)
                .executes(context -> execute(handler, new String[0]))
                .then(ClientCommandManager.argument("args", StringArgumentType.greedyString())
                    .executes(context -> execute(handler, splitArgs(StringArgumentType.getString(context, "args")))))));
    }

    private static int execute(CommandHandler handler, String[] args) {
        handler.execute(args, FabricCommandLayer::printSuccess);
        return com.mojang.brigadier.Command.SINGLE_SUCCESS;
    }

    private static String[] splitArgs(String input) {
        String trimmed = input.trim();
        return trimmed.isEmpty() ? new String[0] : trimmed.split("\\s+");
    }

    private static void printSuccess(String message) {
        send("§a" + message);
    }

    private static void send(String message) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player != null) {
            mc.player.displayClientMessage(Component.literal(message), false);
        }
    }
}
