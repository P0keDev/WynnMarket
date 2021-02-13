package dev.p0ke.wynnmarket.discord.commands;

import java.util.List;

import org.javacord.api.event.message.MessageCreateEvent;

public interface Command {

	public List<String> getNames();
	public void execute(MessageCreateEvent event, String[] args);

}
