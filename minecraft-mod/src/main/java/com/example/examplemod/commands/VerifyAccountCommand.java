package com.example.examplemod.commands;

import java.util.Base64;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.exceptions.CommandSyntaxException;

import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.server.level.ServerPlayer;

import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextColor;

public class VerifyAccountCommand {
    public VerifyAccountCommand(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("verify").executes((command) -> {
            return VerifyAccount(command.getSource());
        }));
    }

    private int VerifyAccount(CommandSourceStack source) throws CommandSyntaxException {
        ServerPlayer player = source.getPlayerOrException();
        String uuid = player.getStringUUID();
        String username = player.getName().getString();
        String params = "uuid=" + uuid + "&username=" + username;
        String encodedParams = Base64.getUrlEncoder().encodeToString(params.getBytes());
        String url = "https://metacraft.netlify.app/verify?params=" + encodedParams;
        TextComponent msg = new TextComponent(url);
        Style style = msg.getStyle();
        ClickEvent click = new ClickEvent(ClickEvent.Action.OPEN_URL, url);
        msg.setStyle(style.withClickEvent(click).withColor(TextColor.fromLegacyFormat(ChatFormatting.YELLOW)));
        source.sendSuccess(msg, true);
        return 1;
    }
}
