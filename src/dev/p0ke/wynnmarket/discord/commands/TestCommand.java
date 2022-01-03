package dev.p0ke.wynnmarket.discord.commands;

import dev.p0ke.wynnmarket.minecraft.MinecraftManager;
import org.javacord.api.event.message.MessageCreateEvent;

import java.util.Arrays;
import java.util.List;

public class TestCommand implements Command {

    @Override
    public List<String> getNames() {
        return Arrays.asList("test");
    }

    @Override
    public void execute(MessageCreateEvent event, String[] args) {
        MinecraftManager.resetListeners();
        MinecraftManager.sendMessage("/hub");
    }

}
