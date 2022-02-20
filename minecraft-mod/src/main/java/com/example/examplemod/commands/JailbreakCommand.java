package com.example.examplemod.commands;

import com.example.examplemod.worldgen.structures.GlassPrisonStructure;
import com.mojang.brigadier.CommandDispatcher;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.server.level.ServerPlayer;

public class JailbreakCommand {
    public JailbreakCommand(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("account").then(Commands.literal("jailbreak").executes((command) -> {
            CommandSourceStack source = command.getSource();
            ServerPlayer player = source.getPlayerOrException();
            GlassPrisonStructure.releaseFromJail(player);
            return 1;
        })));
    }
}