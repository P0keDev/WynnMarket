package dev.p0ke.wynnmarket.discord.commands;

import java.util.Arrays;
import java.util.List;

import org.javacord.api.event.message.MessageCreateEvent;

import dev.p0ke.wynnmarket.discord.managers.ChannelManager;

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
		boolean logAll = (args.length > 1 && args[1].equalsIgnoreCase("log"));
		ChannelManager.addChannel(id, logAll);
		event.getChannel().sendMessage("Registered this channel! " + (logAll ? "Logging all items." : ""));
	}

}
