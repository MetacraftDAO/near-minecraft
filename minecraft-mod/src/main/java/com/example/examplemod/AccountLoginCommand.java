package com.example.examplemod;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import java.net.URISyntaxException;
import java.io.IOException;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.TextComponent;

import java.net.URI;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;
import java.net.http.HttpClient;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class AccountLoginCommand {
    // Directly reference a log4j logger.
    private static final Logger LOGGER = LogManager.getLogger();

    public AccountLoginCommand(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("account").then(Commands.literal("login").executes((command) -> {
            return VerifyNearAccount(command.getSource());
        })));
    }

    private int VerifyNearAccount(CommandSourceStack source) throws CommandSyntaxException {
        source.sendSuccess(new TextComponent("test test"), true);
        try {
            HttpRequest request = HttpRequest.newBuilder().uri(new URI("https://parseapi.back4app.com/classes/Channel"))
                    .header("X-Parse-Application-Id", "3NYTcCWrSdGasNKlg03cQP68qBk7URgK4RCXXk45")
                    .header("X-Parse-Master-Key", "t0Bk4ShUpoHtYP1AJPuYbbLMgUzWzuxaYvMSu1PO")
                    .GET()
                    .build();
            HttpClient client = HttpClient.newHttpClient();
            HttpResponse<String> response = client.send(request, BodyHandlers.ofString());
            LOGGER.info("Status: ", response.statusCode());
            LOGGER.info("Response body: ", response.body());
        } catch (URISyntaxException | IOException | InterruptedException err) {
            LOGGER.info("Error:", err.toString());
        }
        return 1;
    }
}