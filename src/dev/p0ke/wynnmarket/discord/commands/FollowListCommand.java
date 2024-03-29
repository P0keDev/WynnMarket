package dev.p0ke.wynnmarket.discord.commands;

import dev.p0ke.wynnmarket.discord.managers.ChannelManager;
import org.javacord.api.event.message.MessageCreateEvent;

import java.util.Arrays;
import java.util.List;

public class FollowListCommand implements Command {

    @Override
    public List<String> getNames() {
        return Arrays.asList("list", "followlist");
    }

    @Override
    public void execute(MessageCreateEvent event, String[] args) {
        if (!ChannelManager.isFollowerChannel(event.getChannel().getIdAsString())) {
            event.getChannel().sendMessage("This channel is not registered!");
            return;
        }

        List<String> items = ChannelManager.getItemsForUser(event.getChannel().getIdAsString(), event.getMessageAuthor().getIdAsString());
        if (items.isEmpty()) {
            event.getMessage().reply("You are not following any items!");
            return;
        }

        event.getMessage().reply("You are following:\n" + String.join("\n", items));
    }

}
