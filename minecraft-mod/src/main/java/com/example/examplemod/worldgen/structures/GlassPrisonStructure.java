package com.example.examplemod.worldgen.structures;

import net.minecraft.world.level.levelgen.structure.PoolElementStructurePiece;
import net.minecraft.world.level.levelgen.structure.PostPlacementProcessor;
import net.minecraft.world.level.levelgen.structure.pieces.PieceGeneratorSupplier;
import net.minecraft.world.level.levelgen.structure.pieces.PieceGenerator;

import java.util.ArrayList;
import java.util.Optional;

import javax.annotation.Nullable;

import com.example.examplemod.ExampleMod;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.NoiseColumn;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.feature.StructureFeature;
import net.minecraft.world.level.levelgen.feature.configurations.JigsawConfiguration;
import net.minecraft.world.level.levelgen.feature.structures.JigsawPlacement;
import net.minecraft.world.level.levelgen.Heightmap;

public class GlassPrisonStructure extends StructureFeature<JigsawConfiguration> {

    // This is the position of one corner of the prison and y will be zero.
    public static ArrayList<BlockPos> prisons = new ArrayList<BlockPos>();

    public GlassPrisonStructure() {
        super(JigsawConfiguration.CODEC, context -> {
            if (!isFeatureChunk(context)) {
                return Optional.empty();
            } else {
                return createPiecesGenerator(context);
            }
        }, PostPlacementProcessor.NONE);
    }

    @Override
    public GenerationStep.Decoration step() {
        return GenerationStep.Decoration.SURFACE_STRUCTURES;
    }

    // Test if the current chunk (from context) has a valid location for our
    // structure
    private static boolean isFeatureChunk(PieceGeneratorSupplier.Context<JigsawConfiguration> context) {
        BlockPos pos = context.chunkPos().getWorldPosition();

        // Get height of land (stops at first non-air block)
        int landHeight = context.chunkGenerator().getFirstOccupiedHeight(pos.getX(), pos.getZ(),
                Heightmap.Types.WORLD_SURFACE_WG, context.heightAccessor());

        // Grabs column of blocks at given position. In overworld, this column will be
        // made of stone, water, and air.
        // In nether, it will be netherrack, lava, and air. End will only be endstone
        // and air. It depends on what block
        // the chunk generator will place for that dimension.
        NoiseColumn columnOfBlocks = context.chunkGenerator().getBaseColumn(pos.getX(), pos.getZ(),
                context.heightAccessor());

        // Combine the column of blocks with land height and you get the top block
        // itself which you can test.
        BlockState topBlock = columnOfBlocks.getBlock(landHeight);

        // Now we test to make sure our structure is not spawning on water or other
        // fluids.
        // You can do height check instead too to make it spawn at high elevations.
        return topBlock.getFluidState().isEmpty(); // landHeight > 100;
    }

    private static Optional<PieceGenerator<JigsawConfiguration>> createPiecesGenerator(
            PieceGeneratorSupplier.Context<JigsawConfiguration> context) {
        // Turns the chunk coordinates into actual coordinates we can use. (center of
        // that chunk)
        BlockPos blockpos = context.chunkPos().getMiddleBlockPosition(0);

        var newConfig = new JigsawConfiguration(
                () -> context.registryAccess().ownedRegistryOrThrow(Registry.TEMPLATE_POOL_REGISTRY)
                        .get(new ResourceLocation(ExampleMod.MODID, "glassprison/start_pool")),
                5 // In our case our structure is 1 chunk only but by using 5 here it can be
                  // replaced with something larger in datapacks
        );

        // Create a new context with the new config that has our json pool. We will pass
        // this into JigsawPlacement.addPieces
        var newContext = Structures.createContextWithConfig(context, newConfig);
        // Last 'true' parameter means the structure will automatically be placed at
        // ground level
        var generator = JigsawPlacement.addPieces(newContext,
                PoolElementStructurePiece::new, blockpos, false, true);

        if (generator.isPresent()) {
            // Debugging help to quickly find our structures
            // Note: This will print y=0 but the actual y is the ground height and we need
            // to find out (e.g. using tp)
            ExampleMod.LOGGER.info("GlassPrison at " + blockpos);
            prisons.add(blockpos);
        }

        // Return the pieces generator that is now set up so that the game runs it when
        // it needs to create the layout of structure pieces.
        return generator;
    }

    @Nullable
    public static BlockPos getCenterOfPrison(Player player) {
        ServerLevel world = (ServerLevel) player.level;

        BlockPos corner = getSavedPrisonPos(world);
        if (corner != null) {
            ExampleMod.LOGGER.info("Loaded default prison pos: " + corner);
        } else if (!prisons.isEmpty()) {
            corner = prisons.get(0);
            ExampleMod.LOGGER.info("Using generated prison pos: " + corner);
            savePrisonPos(world);
        } else {
            return null;
        }

        int y;
        int x = corner.getX();
        int z = corner.getZ();
        for (y = 150; y > 0; y--) {
            if (world.getBlockState(new BlockPos(x, y, z)).getBlock() == Blocks.GLASS) {
                break;
            }
        }
        if (y == 0) {
            ExampleMod.LOGGER.error("Failed to find y position of Glass Prison");
            return null;
        }
        // Search all orientations.
        if (world.getBlockState(new BlockPos(x + 3, y, z + 3)).getBlock() == Blocks.GLASS) {
            return new BlockPos(x + 3, y - 4, z + 3);
        }
        if (world.getBlockState(new BlockPos(x - 3, y, z + 3)).getBlock() == Blocks.GLASS) {
            return new BlockPos(x - 3, y - 4, z + 3);
        }
        if (world.getBlockState(new BlockPos(x + 3, y, z - 3)).getBlock() == Blocks.GLASS) {
            return new BlockPos(x + 3, y - 4, z - 3);
        }
        if (world.getBlockState(new BlockPos(x - 3, y, z - 3)).getBlock() == Blocks.GLASS) {
            return new BlockPos(x - 3, y - 4, z - 3);
        }
        ExampleMod.LOGGER.error("Failed to find orientation of Glass Prison");
        return null;
    }

    @Nullable
    private static BlockPos getSavedPrisonPos(ServerLevel world) {
        PrisonSavedData saved_data = PrisonSavedData.get(world);
        int pos[] = saved_data.getDefaultPrisonPos();
        if (pos == null) {
            savePrisonPos(world);
            return null;
        }
        return new BlockPos(pos[0], pos[1], pos[2]);
    }

    public static void savePrisonPos(ServerLevel world) {
        if (prisons.isEmpty()) {
            return;
        }
        PrisonSavedData saved_data = PrisonSavedData.get(world);
        BlockPos p = prisons.get(0);
        int new_pos[] = { p.getX(), p.getY(), p.getZ() };
        saved_data.setDefaultPrisonPos(new_pos);
    }

    public static void sendToJail(ServerPlayer player) {
        BlockPos prison = getCenterOfPrison(player);
        if (prison == null) {
            ExampleMod.LOGGER.info("Cannot find prison to put player into");
            return;
        }
        player.teleportTo(prison.getX(), prison.getY(), prison.getZ());
        ExampleMod.LOGGER
                .info("Teleported " + player.getName().getString() + " to login prison at " + prison);
    }

    public static void releaseFromJail(ServerPlayer player) {
        ServerLevel level = (ServerLevel) player.level;
        BlockPos dest = level.getSharedSpawnPos();
        player.teleportTo(dest.getX(), dest.getY(), dest.getZ());
        ExampleMod.LOGGER
                .info("Teleported " + player.getName().getString() + " from prison to " + dest);
    }
}
