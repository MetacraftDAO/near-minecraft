package com.example.examplemod.commands;

import com.example.examplemod.utils.DatabaseConnector;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.exceptions.CommandSyntaxException;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.server.level.ServerPlayer;

public class LoginCommand {
    // Directly reference a log4j logger.
    private DatabaseConnector database;

    public LoginCommand(CommandDispatcher<CommandSourceStack> dispatcher) {
        database = DatabaseConnector.getInstance();
        dispatcher.register(Commands.literal("login").executes((command) -> {
            return LoginNearAccount(command.getSource());
        }));
    }

    private int LoginNearAccount(CommandSourceStack source) throws CommandSyntaxException {
        ServerPlayer player = source.getPlayerOrException();
        String uuid = player.getStringUUID();
        String username = player.getName().getString();
        if (database.isUserVerified(username, uuid)) {
            source.sendSuccess(new TextComponent("Account verified! Login successfully."), true);
        } else {
            source.sendSuccess(new TextComponent("Account is not verified, pls type /verify to verify your account."),
                    true);
        }
        return 1;
    }
}