package com.example.examplemod;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.exceptions.CommandSyntaxException;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.TextComponent;


public class VerifyAccountCommand {
    public VerifyAccountCommand(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("near").then(Commands.literal("login").executes((command) -> {
            return VerifyNearAccount(command.getSource());
        })));
    }
    
    private int VerifyNearAccount(CommandSourceStack source) throws CommandSyntaxException {
        source.sendSuccess(new TextComponent("https://wallet.testnet.near.org/"), true);
        return 1;
    }
}
