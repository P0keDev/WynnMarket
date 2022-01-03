package dev.p0ke.wynnmarket.discord.commands;

import dev.p0ke.wynnmarket.discord.managers.ChannelManager;
import org.apache.commons.lang3.text.WordUtils;
import org.javacord.api.event.message.MessageCreateEvent;

import java.util.Arrays;
import java.util.List;

@SuppressWarnings("deprecation")
public class UnfollowItemCommand implements Command {

    @Override
    public List<String> getNames() {
        return Arrays.asList("unfollow");
    }

    @Override
    public void execute(MessageCreateEvent event, String[] args) {
        if (!ChannelManager.isFollowerChannel(event.getChannel().getIdAsString())) {
            event.getChannel().sendMessage("This channel is not registered!");
            return;
        }

        if (args.length <= 1) {
            event.getMessage().reply("Must specify an item!");
            return;
        }

        String item = "";
        for (int i = 1; i < args.length; i++) {
            item += args[i] + " ";
        }
        item = item.trim();

        if (ChannelManager.removeItem(event.getChannel().getIdAsString(), event.getMessageAuthor().getIdAsString(), item))
            event.getMessage().reply("You unfollowed: " + WordUtils.capitalize(item));
        else
            event.getMessage().reply("You are not following that item!");
    }

}
