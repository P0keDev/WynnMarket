package dev.p0ke.wynnmarket.discord.commands;

import dev.p0ke.wynnmarket.discord.managers.ChannelManager;
import org.javacord.api.event.message.MessageCreateEvent;

import java.util.Arrays;
import java.util.List;

public class UnregisterChannelCommand implements Command {

    @Override
    public List<String> getNames() {
        return Arrays.asList("unregister");
    }

    @Override
    public void execute(MessageCreateEvent event, String[] args) {
        if (!event.getMessageAuthor().isServerAdmin()) {
            event.getChannel().sendMessage("You must be an admin to register a channel!");
            return;
        }

        String id = event.getChannel().getIdAsString();
        ChannelManager.removeChannel(id);
        event.getChannel().sendMessage("Unregistered this channel!");
    }

}
