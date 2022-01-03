package dev.p0ke.wynnmarket.discord.commands;

import dev.p0ke.wynnmarket.discord.managers.ChannelManager;
import org.javacord.api.event.message.MessageCreateEvent;

import java.util.Arrays;
import java.util.List;

public class RegisterChannelCommand implements Command {

    @Override
    public List<String> getNames() {
        return Arrays.asList("register");
    }

    @Override
    public void execute(MessageCreateEvent event, String[] args) {
        if (!event.getMessageAuthor().isServerAdmin()) {
            event.getChannel().sendMessage("You must be an admin to register a channel!");
            return;
        }

        String id = event.getChannel().getIdAsString();
        List<String> argList = Arrays.asList(args);
        boolean logAll = argList.contains("log");
        boolean info = !logAll && argList.contains("info");
        ChannelManager.addChannel(id, logAll, info);
        event.getChannel().sendMessage("Registered this channel! " +
                (logAll ? "Logging all items." : (info ? "Logging info messages." : "")));
    }

}
