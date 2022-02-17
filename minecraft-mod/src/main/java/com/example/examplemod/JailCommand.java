package com.example.examplemod;

import com.example.examplemod.worldgen.structures.GlassPrisonStructure;
import com.example.examplemod.worldgen.structures.Structures;
import com.mojang.brigadier.CommandDispatcher;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
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
        BlockPos prison = Structures.findNearestStruct(new ResourceLocation("examplemod:glassprison"), player);
        // TODO: Replace 150 with ground height.
        player.teleportTo(prison.getX() + GlassPrisonStructure.TP_OFFSET_X, 150,
                prison.getZ() + GlassPrisonStructure.TP_OFFSET_Z);
        ExampleMod.LOGGER
                .info("Teleported " + player.getName().getString() + " to login prison at " + prison.getX() +
                        ", 150, " + prison.getZ());
    }
}