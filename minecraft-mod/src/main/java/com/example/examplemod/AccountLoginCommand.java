package com.example.examplemod;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import java.net.URISyntaxException;
import java.io.IOException;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.server.level.ServerPlayer;

import java.net.URI;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;
import java.util.HashMap;
import java.util.stream.Collectors;
import java.net.http.HttpClient;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import java.lang.RuntimeException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;

public class AccountLoginCommand {
    // Directly reference a log4j logger.
    private static final Logger LOGGER = LogManager.getLogger();

    public AccountLoginCommand(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("account").then(Commands.literal("login").executes((command) -> {
            CommandSourceStack source = command.getSource();
            ServerPlayer player = source.getPlayerOrException();
            LOGGER.info("Start dispatch the account login comand!");
            LOGGER.info("Play name: " + player.getName().getString());
            LOGGER.info("Current player is:" + player.getStringUUID());
            return LoginNearAccount(command.getSource());
        })));
    }

    private int LoginNearAccount(CommandSourceStack source) throws CommandSyntaxException {
        source.sendSuccess(new TextComponent("test test"), true);
        try {
            HttpRequest.Builder builder = HttpRequest.newBuilder()
                    .uri(new URI("https://parseapi.back4app.com/classes/Channel"));
            HashMap<String, String> headers = GetDBHeaders();
            headers.forEach((key, value) -> builder.header(key, value));
            HttpRequest request = builder.GET().build();
            HttpClient client = HttpClient.newHttpClient();
            HttpResponse<String> response = client.send(request,
                    BodyHandlers.ofString());
            LOGGER.info("Status: " + String.valueOf(response.statusCode()));
            LOGGER.info("Response body: " + response.body());
        } catch (URISyntaxException | IOException | InterruptedException | RuntimeException err) {
            LOGGER.info("Error:", err.toString());
        }
        return 1;
    }

    private HashMap<String, String> GetDBHeaders() throws IOException {
        HashMap<String, String> headers = new HashMap<String, String>();
        Path homePath = Paths.get(System.getProperty("user.home"));

        Path keyPath = Paths.get(homePath.toString(), ".back4app_key");
        String key = ReadFileContent(keyPath.toString());
        headers.put("X-Parse-Master-Key", key);

        Path appIdPath = Paths.get(homePath.toString(), ".back4app_app_id");
        String appId = ReadFileContent(appIdPath.toString());
        headers.put("X-Parse-Application-Id", appId);
        return headers;
    }

    private String ReadFileContent(String path) throws IOException {
        File file = new File(path);
        BufferedReader buffer = new BufferedReader(new FileReader(file));
        String content = buffer.lines().collect(Collectors.joining());
        buffer.close();
        return content;
    }

}