package com.example.examplemod.commands;

import com.example.examplemod.ExampleMod;
import com.mojang.brigadier.CommandDispatcher;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;

public class JailbreakCommand {
    public JailbreakCommand(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("account").then(Commands.literal("jailbreak").executes((command) -> {
            CommandSourceStack source = command.getSource();
            ServerPlayer player = source.getPlayerOrException();
            releaseFromJail(player);
            return 1;
        })));
    }

    public static void releaseFromJail(ServerPlayer player) {
        ServerLevel level = (ServerLevel) player.level;
        BlockPos dest = level.getSharedSpawnPos();
        player.teleportTo(dest.getX(), dest.getY(), dest.getZ());
        ExampleMod.LOGGER
                .info("Teleported " + player.getName().getString() + " from prison to " + dest);
    }
}