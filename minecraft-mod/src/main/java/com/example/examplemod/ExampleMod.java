package com.example.examplemod;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.Block;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.event.entity.player.PlayerEvent.PlayerLoggedInEvent;
import net.minecraftforge.event.entity.player.PlayerEvent.PlayerLoggedOutEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.event.server.ServerStartingEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.server.command.ConfigCommand;

import com.example.examplemod.commands.JailCommand;
import com.example.examplemod.commands.JailbreakCommand;
import com.example.examplemod.commands.LoginCommand;
import com.example.examplemod.commands.VerifyAccountCommand;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.example.examplemod.setup.ModSetup;
import com.example.examplemod.setup.Registration;

import com.example.examplemod.utils.DatabaseConnector;
import com.example.examplemod.utils.TikTokTime;
import com.example.examplemod.worldgen.structures.GlassPrisonStructure;

// The value here should match an entry in the META-INF/mods.toml file
@Mod(ExampleMod.MODID)
public class ExampleMod {
    // Directly reference a log4j logger.
    public static final Logger LOGGER = LogManager.getLogger();

    public static final String MODID = "examplemod";
    private DatabaseConnector database;
    private TikTokTime tiktok;

    public ExampleMod() {
        ModSetup.setup();
        Registration.init();

        database = DatabaseConnector.getInstance();
        tiktok = new TikTokTime(database);

        IEventBus modbus = FMLJavaModLoadingContext.get().getModEventBus();
        modbus.addListener(ModSetup::init);

        // Register ourselves for server and other game events we are interested in
        MinecraftForge.EVENT_BUS.register(this);
    }

    // You can use SubscribeEvent and let the Event Bus discover methods to call
    @SubscribeEvent
    public void onServerStarting(ServerStartingEvent event) {
        // do something when the server starts
        LOGGER.info("HELLO from server starting");
    }

    private boolean playerShouldBeJailed(Player player) {
        return !database.isUserVerified(player.getStringUUID());
    }

    @SubscribeEvent
    public void onPlayerLogin(PlayerLoggedInEvent event) {
        Player player = event.getPlayer();
        String uuid = player.getStringUUID();
        LOGGER.info("login!!: player " + player.getName().getString() + " with uuid: " + uuid);
        if (database.isUserVerified(uuid)) {
            // Start recording play time.
            tiktok.tik(uuid);
        }

        if (playerShouldBeJailed(player)) {
            GlassPrisonStructure.sendToJail((ServerPlayer) player);
        }

        // Note: when the player is released from the login prison, we can use
        // getSharedSpawnPos to find a suitable place to teleport to.
    }

    @SubscribeEvent
    public void onPlayerLogout(PlayerLoggedOutEvent event) {
        Player player = event.getPlayer();
        LOGGER.info(
                "logout!!, logout: player " + player.getName().getString() + " with uuid: " + player.getStringUUID());
        // End recording play time.
        tiktok.tok(player.getStringUUID());
    }

    // You can use EventBusSubscriber to automatically subscribe events on the
    // contained class (this is subscribing to the MOD
    // Event bus for receiving Registry Events)
    @Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD)
    public static class RegistryEvents {
        @SubscribeEvent
        public static void onBlocksRegistry(final RegistryEvent.Register<Block> blockRegistryEvent) {
            // register a new block here
            LOGGER.info("HELLO from Register Block");
        }
    }

    @Mod.EventBusSubscriber
    public static class CommandEvents {
        @SubscribeEvent
        public static void onCommandRegister(RegisterCommandsEvent event) {
            new VerifyAccountCommand(event.getDispatcher());
            new LoginCommand(event.getDispatcher());

            // Only for testing
            new JailCommand(event.getDispatcher());
            new JailbreakCommand(event.getDispatcher());

            ConfigCommand.register(event.getDispatcher());
        }
    }
}
