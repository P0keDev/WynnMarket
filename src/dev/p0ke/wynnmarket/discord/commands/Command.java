package dev.p0ke.wynnmarket.discord.commands;

import org.javacord.api.event.message.MessageCreateEvent;

import java.util.List;

public interface Command {

    public List<String> getNames();

    public void execute(MessageCreateEvent event, String[] args);

}
