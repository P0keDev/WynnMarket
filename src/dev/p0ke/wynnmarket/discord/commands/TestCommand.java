package dev.p0ke.wynnmarket.discord.commands;

import java.util.Arrays;
import java.util.List;

import org.javacord.api.event.message.MessageCreateEvent;

import dev.p0ke.wynnmarket.minecraft.ClientManager;

public class TestCommand implements Command {

	@Override
	public List<String> getNames() {
		return Arrays.asList("test");
	}

	@Override
	public void execute(MessageCreateEvent event, String[] args) {
		ClientManager.resetListeners();
		ClientManager.sendMessage("/hub");
	}

}