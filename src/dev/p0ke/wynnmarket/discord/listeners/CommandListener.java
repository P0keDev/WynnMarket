package dev.p0ke.wynnmarket.discord.listeners;

import java.util.ArrayList;
import java.util.List;

import org.javacord.api.event.message.MessageCreateEvent;
import org.javacord.api.listener.message.MessageCreateListener;

import dev.p0ke.wynnmarket.discord.commands.Command;

public class CommandListener implements MessageCreateListener {

	private List<Command> commands = new ArrayList<>();
	private List<String> prefixes = new ArrayList<>();

	@Override
	public void onMessageCreate(MessageCreateEvent event) {
		String msg = event.getMessage().getContent();

		for (String prefix : prefixes) {
			if (msg.startsWith(prefix)) {
				String[] args = msg.substring(prefix.length()).split(" ");
				String command = args[0];
				for (Command c : commands) {
					if (c.getNames().contains(command)) {
						new Thread(() -> c.execute(event, args)).start();
						return;
					}
				}
			}
		}
	}

	public void registerCommand(Command c) {
		commands.add(c);
	}

	public void registerPrefix(String p) {
		prefixes.add(p);
	}

}
