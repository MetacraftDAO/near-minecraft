package com.example.examplemod;

import java.util.Base64;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.exceptions.CommandSyntaxException;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.server.level.ServerPlayer;

public class VerifyAccountCommand {
    public VerifyAccountCommand(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("near").then(Commands.literal("login").executes((command) -> {
            return VerifyNearAccount(command.getSource());
        })));
    }

    private int VerifyNearAccount(CommandSourceStack source) throws CommandSyntaxException {
        ServerPlayer player = source.getPlayerOrException();
        String uuid = player.getStringUUID();
        String username = player.getName().getString();
        String params = "uuid=" + uuid + "&username=" + username;
        String encodedParams = Base64.getUrlEncoder().encodeToString(params.getBytes());
        String url = "https://meta-minecraft.netlify.app?params=" + encodedParams;
        // Style style = usl.getStyoe
        source.sendSuccess(new TextComponent(url), true);
        return 1;
    }
}
