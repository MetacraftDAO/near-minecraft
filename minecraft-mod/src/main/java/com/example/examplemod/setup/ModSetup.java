package com.example.examplemod.setup;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.InterModComms;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.event.lifecycle.InterModEnqueueEvent;
import net.minecraftforge.fml.event.lifecycle.InterModProcessEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.stream.Collectors;

import com.example.examplemod.worldgen.structures.Structures;

import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public class ModSetup {
    private static final Logger LOGGER = LogManager.getLogger();

    public static final CreativeModeTab ITEM_GROUP = new CreativeModeTab("NEAR") {
        @Override
        public ItemStack makeIcon() {
            return new ItemStack(Items.GLASS);
        }
    };

    public static void init(FMLCommonSetupEvent event) {
        //// Register the setup method for modloading
        // FMLJavaModLoadingContext.get().getModEventBus().addListener(ModSetup::setup);
        // Register the enqueueIMC method for modloading
        FMLJavaModLoadingContext.get().getModEventBus().addListener(ModSetup::enqueueIMC);
        // Register the processIMC method for modloading
        FMLJavaModLoadingContext.get().getModEventBus().addListener(ModSetup::processIMC);

        event.enqueueWork(() -> {
            Structures.setupStructures();
            Structures.registerConfiguredStructures();
        });
    }

    public static void setup() {
        IEventBus bus = MinecraftForge.EVENT_BUS;
        bus.addListener(EventPriority.NORMAL, Structures::addDimensionalSpacing);
    }

    // private static void setup(final FMLCommonSetupEvent event) {
    //     // some preinit code
    //     LOGGER.info("HELLO FROM PREINIT");
    //     LOGGER.info("DIRT BLOCK >> {}", Blocks.DIRT.getRegistryName());
    // }

    private static void enqueueIMC(final InterModEnqueueEvent event) {
        // some example code to dispatch IMC to another mod
        InterModComms.sendTo("examplemod", "helloworld", () -> {
            LOGGER.info("Hello world from the MDK");
            return "Hello world";
        });
    }

    private static void processIMC(final InterModProcessEvent event) {
        // some example code to receive and process InterModComms from other mods
        LOGGER.info("Got IMC {}",
                event.getIMCStream().map(m -> m.messageSupplier().get()).collect(Collectors.toList()));
    }
}
