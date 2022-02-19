package com.example.examplemod.worldgen.structures;

import java.util.HashMap;
import java.util.Map;

import javax.annotation.Nullable;

import com.example.examplemod.ExampleMod;
import com.example.examplemod.setup.Registration;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMultimap;

import org.jetbrains.annotations.NotNull;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.data.BuiltinRegistries;
import net.minecraft.data.worldgen.PlainVillagePools;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Biome.BiomeCategory;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.FlatLevelSource;
import net.minecraft.world.level.levelgen.StructureSettings;
import net.minecraft.world.level.levelgen.feature.ConfiguredStructureFeature;
import net.minecraft.world.level.levelgen.feature.StructureFeature;
import net.minecraft.world.level.levelgen.feature.configurations.JigsawConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.StructureFeatureConfiguration;
import net.minecraft.world.level.levelgen.structure.pieces.PieceGeneratorSupplier;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.registries.ForgeRegistries;

public class Structures {
    // Static instance of our structure to add to biomes easily.
    // We cannot get our own pool here at mod init so we use PlainVillagePools.START
    // We will modify this pool later in createPiecesGenerator.
    public static ConfiguredStructureFeature<?, ?> CONFIGURED_GLASSPRISON = Registration.GLASSPRISON.get()
            .configured(new JigsawConfiguration(() -> PlainVillagePools.START, 0));

    public static void registerConfiguredStructures() {
        Registry.register(BuiltinRegistries.CONFIGURED_STRUCTURE_FEATURE,
                new ResourceLocation(ExampleMod.MODID, "glassprison"), CONFIGURED_GLASSPRISON);
    }

    public static void setupStructures() {
        setupMapSpacingAndLand(
                Registration.GLASSPRISON.get(),
                new StructureFeatureConfiguration(
                        10, // Average distance in chunks
                        5, // Min distance in chunks
                        1234567890),
                false);
    }

    private static <F extends StructureFeature<?>> void setupMapSpacingAndLand(
            F structure,
            StructureFeatureConfiguration structureFeatureConfiguration,
            boolean transformSurroundingLand) {
        StructureFeature.STRUCTURES_REGISTRY.put(structure.getRegistryName().toString(), structure);

        // This is the map that holds the spacing between structures. It's usually
        // private
        // so we need the access transformer here.
        StructureSettings.DEFAULTS = ImmutableMap.<StructureFeature<?>, StructureFeatureConfiguration>builder()
                .putAll(StructureSettings.DEFAULTS)
                .put(structure, structureFeatureConfiguration)
                .build();

        BuiltinRegistries.NOISE_GENERATOR_SETTINGS.entrySet().forEach(settings -> {
            Map<StructureFeature<?>, StructureFeatureConfiguration> structureMap = settings.getValue()
                    .structureSettings().structureConfig();

            if (structureMap instanceof ImmutableMap) {
                Map<StructureFeature<?>, StructureFeatureConfiguration> tempMap = new HashMap<>(structureMap);
                tempMap.put(structure, structureFeatureConfiguration);
                settings.getValue().structureSettings().structureConfig = tempMap;
            } else {
                structureMap.put(structure, structureFeatureConfiguration);
            }
        });
    }

    /**
     * Tells the chunkgenerator which biomes our structure can spawn in.
     * Will go into the world's chunkgenerator where we manually add our structure
     * spacing.
     * If the spacing is not added, the structure doesn't spawn.
     *
     * Use this for dimension blacklists for your structure.
     * (Don't forget to attempt to remove your structure too from the map if you are
     * blacklisting that dimension!)
     * (It might have your structure in it already.)
     *
     * Basically use this to make absolutely sure the chunkgenerator can or cannot
     * spawn your structure.
     */
    public static void addDimensionalSpacing(final WorldEvent.Load event) {
        if (event.getWorld() instanceof ServerLevel serverLevel) {
            ChunkGenerator chunkGenerator = serverLevel.getChunkSource().getGenerator();
            // Skip superflat to prevent issues with it. Plus, users don't want structures
            // clogging up their superflat worlds.
            if (chunkGenerator instanceof FlatLevelSource && serverLevel.dimension().equals(Level.OVERWORLD)) {
                return;
            }

            ConfiguredStructureFeature<?, ?> glassPrisonFeature = null;
            if (serverLevel.dimension().equals(Level.OVERWORLD)) {
                glassPrisonFeature = CONFIGURED_GLASSPRISON;
            }

            StructureSettings worldStructureConfig = chunkGenerator.getSettings();

            /*
             * NOTE: BiomeLoadingEvent from Forge API does not work with structures anymore.
             * Instead, we will use the below to add our structure to overworld biomes.
             * Remember, this is temporary until Forge API finds a better solution for
             * adding structures to biomes.
             */

            // Create a mutable map we will use for easier adding to biomes
            var structureToMultimap = new HashMap<StructureFeature<?>, HashMultimap<ConfiguredStructureFeature<?, ?>, ResourceKey<Biome>>>();

            // Add the resourcekey of all biomes that this Configured Structure can spawn
            // in.
            for (var biomeEntry : serverLevel.registryAccess().ownedRegistryOrThrow(Registry.BIOME_REGISTRY)
                    .entrySet()) {
                // Skip all ocean, end, nether, and none category biomes.
                // You can do checks for other traits that the biome has.
                BiomeCategory category = biomeEntry.getValue().getBiomeCategory();
                if (category != BiomeCategory.OCEAN && category != BiomeCategory.THEEND
                        && category != BiomeCategory.NETHER && category != BiomeCategory.NONE) {
                    associateBiomeToConfiguredStructure(structureToMultimap, CONFIGURED_GLASSPRISON,
                            biomeEntry.getKey());
                }
                if (glassPrisonFeature != null) {
                    if (category != BiomeCategory.THEEND && category != BiomeCategory.NETHER
                            && category != BiomeCategory.NONE) {
                        associateBiomeToConfiguredStructure(structureToMultimap, glassPrisonFeature,
                                biomeEntry.getKey());
                    }
                }
            }

            // Grab the map that holds what ConfigureStructures a structure has and what
            // biomes it can spawn in.
            // Requires AccessTransformer (see resources/META-INF/accesstransformer.cfg)
            ImmutableMap.Builder<StructureFeature<?>, ImmutableMultimap<ConfiguredStructureFeature<?, ?>, ResourceKey<Biome>>> tempStructureToMultiMap = ImmutableMap
                    .builder();
            worldStructureConfig.configuredStructures.entrySet()
                    .stream()
                    .filter(entry -> !structureToMultimap.containsKey(entry.getKey()))
                    .forEach(tempStructureToMultiMap::put);

            // Add our structures to the structure map/multimap and set the world to use
            // this combined map/multimap.
            structureToMultimap
                    .forEach((key, value) -> tempStructureToMultiMap.put(key, ImmutableMultimap.copyOf(value)));

            // Requires AccessTransformer (see resources/META-INF/accesstransformer.cfg)
            worldStructureConfig.configuredStructures = tempStructureToMultiMap.build();
        }
    }

    /**
     * Helper method that handles setting up the map to multimap relationship to
     * help prevent issues.
     */
    private static void associateBiomeToConfiguredStructure(
            Map<StructureFeature<?>, HashMultimap<ConfiguredStructureFeature<?, ?>, ResourceKey<Biome>>> structureToMultimap,
            ConfiguredStructureFeature<?, ?> configuredStructureFeature, ResourceKey<Biome> biomeRegistryKey) {
        structureToMultimap.putIfAbsent(configuredStructureFeature.feature, HashMultimap.create());
        var configuredStructureToBiomeMultiMap = structureToMultimap.get(configuredStructureFeature.feature);
        if (configuredStructureToBiomeMultiMap.containsValue(biomeRegistryKey)) {
            ExampleMod.LOGGER.error(
                    """
                                Detected 2 ConfiguredStructureFeatures that share the same base StructureFeature trying to be added to same biome. One will be prevented from spawning.
                                This issue happens with vanilla too and is why a Snowy Village and Plains Village cannot spawn in the same biome because they both use the Village base structure.
                                The two conflicting ConfiguredStructures are: {}, {}
                                The biome that is attempting to be shared: {}
                            """,
                    BuiltinRegistries.CONFIGURED_STRUCTURE_FEATURE.getId(configuredStructureFeature),
                    BuiltinRegistries.CONFIGURED_STRUCTURE_FEATURE.getId(configuredStructureToBiomeMultiMap.entries()
                            .stream()
                            .filter(e -> e.getValue() == biomeRegistryKey)
                            .findFirst()
                            .get().getKey()),
                    biomeRegistryKey);
        } else {
            configuredStructureToBiomeMultiMap.put(configuredStructureFeature, biomeRegistryKey);
        }
    }

    /**
     * Create a copy of a piece generator context with another config. This is used
     * by the structures
     */
    @NotNull
    static PieceGeneratorSupplier.Context<JigsawConfiguration> createContextWithConfig(
            PieceGeneratorSupplier.Context<JigsawConfiguration> context, JigsawConfiguration newConfig) {
        return new PieceGeneratorSupplier.Context<>(
                context.chunkGenerator(),
                context.biomeSource(),
                context.seed(),
                context.chunkPos(),
                newConfig,
                context.heightAccessor(),
                context.validBiome(),
                context.structureManager(),
                context.registryAccess());
    }

    @Nullable
    public static BlockPos findNearestStruct(ResourceLocation structureName, Player player) {
        StructureFeature<?> structure = ForgeRegistries.STRUCTURE_FEATURES.getValue(structureName);
        if (structure == null) {
            ExampleMod.LOGGER.error("Cannot find structure feature from Registry");
            return null;
        }

        int searchRange = 200;
        boolean findUnexplored = false;

        ServerLevel worldIn = (ServerLevel) player.level;
        BlockPos structurePos = worldIn.findNearestMapFeature(structure, player.blockPosition(), searchRange,
                findUnexplored);
        if (structurePos == null) {
            ExampleMod.LOGGER.error("Cannot find structure position");
            return null;
        }

        ExampleMod.LOGGER.info("Found nearest structure at " + structurePos.getX() + ", " + structurePos.getY() + ", "
                + structurePos.getZ());
        return structurePos;
    }
}
