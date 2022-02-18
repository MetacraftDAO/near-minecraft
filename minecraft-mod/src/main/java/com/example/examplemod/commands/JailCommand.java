package com.example.examplemod.commands;

import com.example.examplemod.ExampleMod;
import com.example.examplemod.worldgen.structures.GlassPrisonStructure;
import com.mojang.brigadier.CommandDispatcher;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;

public class JailCommand {
    public JailCommand(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("account").then(Commands.literal("jail").executes((command) -> {
            CommandSourceStack source = command.getSource();
            ServerPlayer player = source.getPlayerOrException();
            JailPlayer(player);
            return 1;
        })));
    }

    public static void JailPlayer(ServerPlayer player) {
        BlockPos prison = GlassPrisonStructure.getCenterOfPrison(player);
        if (prison == null) {
            ExampleMod.LOGGER.info("Cannot find prison to put player into");
            return;
        }
        player.teleportTo(prison.getX(), prison.getY(), prison.getZ());
        ExampleMod.LOGGER
                .info("Teleported " + player.getName().getString() + " to login prison at " + prison);
    }
}