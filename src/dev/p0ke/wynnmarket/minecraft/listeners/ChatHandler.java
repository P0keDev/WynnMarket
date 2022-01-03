package dev.p0ke.wynnmarket.minecraft.listeners;

import com.github.steveice10.mc.protocol.data.game.MessageType;
import com.github.steveice10.mc.protocol.packet.ingame.server.ServerChatPacket;
import dev.p0ke.wynnmarket.discord.DiscordManager;
import dev.p0ke.wynnmarket.minecraft.event.Listener;
import dev.p0ke.wynnmarket.minecraft.event.PacketHandler;
import dev.p0ke.wynnmarket.minecraft.util.StringUtil;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ChatHandler extends Listener {

    private static final Pattern PM_PATTERN = Pattern.compile("\\[([\\w ]+)[()\\w ]* \u27A4 [\\w ]+] (.*)");

    @PacketHandler
    public void onChat(ServerChatPacket chatPacket) {
        if (chatPacket.getType() == MessageType.NOTIFICATION) return;

        String message = StringUtil.parseChatMessage(chatPacket);
        message = StringUtil.removeFormatting(message);

        Matcher pmMatcher = PM_PATTERN.matcher(message);
        if (pmMatcher.matches()) {
            String ign = pmMatcher.group(1);
            String msg = pmMatcher.group(2);
            DiscordManager.infoMessage("PM from: " + ign, msg);
        }
    }

    @Override
    public void finish() {
    }
}
