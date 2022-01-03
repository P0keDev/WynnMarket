package dev.p0ke.wynnmarket.discord;

import dev.p0ke.wynnmarket.data.instances.MarketItem;
import dev.p0ke.wynnmarket.discord.commands.*;
import dev.p0ke.wynnmarket.discord.listeners.CommandListener;
import dev.p0ke.wynnmarket.discord.managers.ChannelManager;
import dev.p0ke.wynnmarket.discord.util.ItemUtil;
import org.javacord.api.DiscordApi;
import org.javacord.api.DiscordApiBuilder;
import org.javacord.api.entity.activity.ActivityType;
import org.javacord.api.entity.channel.ServerTextChannel;
import org.javacord.api.entity.intent.Intent;
import org.javacord.api.entity.message.embed.EmbedBuilder;
import org.javacord.api.entity.user.User;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class DiscordManager {

    private static DiscordApi client;

    public static void start(String token) {
        client = new DiscordApiBuilder().setToken(token).setAllIntentsExcept(Intent.GUILD_PRESENCES).login().join();

        CommandListener commandListener = new CommandListener();
        commandListener.registerPrefix("$");
        commandListener.registerCommand(new FollowItemCommand());
        commandListener.registerCommand(new UnfollowItemCommand());
        commandListener.registerCommand(new FilterCommand());
        commandListener.registerCommand(new FollowListCommand());
        commandListener.registerCommand(new RegisterChannelCommand());
        commandListener.registerCommand(new UnregisterChannelCommand());
        commandListener.registerCommand(new SearchCommand());
        //commandListener.registerCommand(new TestCommand());

        client.addListener(commandListener);

        ChannelManager.setupList();
    }

    public static void logItem(MarketItem item) {
        for (String id : ChannelManager.getLogChannels()) {
            if (!client.getServerTextChannelById(id).isPresent()) continue;
            ServerTextChannel channel = client.getServerTextChannelById(id).get();
            channel.sendMessage(ItemUtil.getItemString(item));
        }

        for (String id : ChannelManager.getFollowerChannels()) {
            if (!client.getServerTextChannelById(id).isPresent()) continue;
            ServerTextChannel channel = client.getServerTextChannelById(id).get();
            List<String> mentions = new ArrayList<>();
            List<String> users = ChannelManager.getUsersForItem(id, item);
            if (users.isEmpty()) continue;
            for (String userId : users) {
                if (!channel.getServer().getMemberById(userId).isPresent()) continue;
                mentions.add(channel.getServer().getMemberById(userId).get().getMentionTag());
            }

            if (mentions.isEmpty()) continue;

            channel.sendMessage("Followed by: " + String.join(", ", mentions) + "\n" + ItemUtil.getItemString(item))
                    .thenAccept(message -> {
                        message.addReaction("\u274C");
                        message.addReactionAddListener(reactionEvent -> {
                            if (!reactionEvent.getEmoji().equalsEmoji("\u274C")) return;

                            User user = reactionEvent.getUser().get();
                            if (!message.getMentionedUsers().contains(user)) return;

                            if (ChannelManager.blockItem(channel.getIdAsString(), user.getIdAsString(), item))
                                message.reply(user.getMentionTag() + ": Successfully blocked item!");
                        }).removeAfter(12, TimeUnit.HOURS);
                    });
        }
    }

    public static void infoMessage(String title, String content) {
        EmbedBuilder embed = new EmbedBuilder()
                .setTitle(title)
                .setDescription(content)
                .setColor(new Color(0, 100, 0));

        for (String id : ChannelManager.getInfoChannels()) {
            if (!client.getServerTextChannelById(id).isPresent()) continue;
            ServerTextChannel channel = client.getServerTextChannelById(id).get();
            channel.sendMessage(embed);
        }
    }

    public static void setStatus(String status) {
        client.updateActivity(ActivityType.WATCHING, status);
    }

    public static void clearStatus() {
        client.unsetActivity();
    }

}
