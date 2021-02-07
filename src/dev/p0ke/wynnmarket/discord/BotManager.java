package dev.p0ke.wynnmarket.discord;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

import org.javacord.api.DiscordApi;
import org.javacord.api.DiscordApiBuilder;
import org.javacord.api.entity.activity.ActivityType;
import org.javacord.api.entity.channel.ServerTextChannel;
import org.javacord.api.entity.intent.Intent;
import org.javacord.api.entity.message.embed.EmbedBuilder;

import dev.p0ke.wynnmarket.data.instances.MarketItem;
import dev.p0ke.wynnmarket.discord.commands.FilterCommand;
import dev.p0ke.wynnmarket.discord.commands.FollowItemCommand;
import dev.p0ke.wynnmarket.discord.commands.FollowListCommand;
import dev.p0ke.wynnmarket.discord.commands.RegisterChannelCommand;
import dev.p0ke.wynnmarket.discord.commands.UnfollowItemCommand;
import dev.p0ke.wynnmarket.discord.commands.UnregisterChannelCommand;
import dev.p0ke.wynnmarket.discord.listeners.CommandListener;
import dev.p0ke.wynnmarket.discord.listeners.ReactionListener;
import dev.p0ke.wynnmarket.discord.managers.ChannelManager;
import dev.p0ke.wynnmarket.util.PriceUtil;

public class BotManager {

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
		//commandListener.registerCommand(new TestCommand());

		client.addListener(commandListener);
		client.addListener(new ReactionListener());

		ChannelManager.setupList();
	}

	public static void logItem(MarketItem item) {
		for (String id : ChannelManager.getLogChannels()) {
			if (!client.getServerTextChannelById(id).isPresent()) continue;
			ServerTextChannel channel = client.getServerTextChannelById(id).get();
			channel.sendMessage(getItemString(item));
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

			channel.sendMessage("Followed by: " + String.join(", ", mentions) + "\n" + getItemString(item)).thenAccept(message -> {
				message.addReaction("\u274C");
				ReactionListener.addMessage(message.getIdAsString(), item);
			});
		}
	}

	public static void logMessage(String title, String content) {
		EmbedBuilder embed = new EmbedBuilder()
				.setTitle(title)
				.setDescription(content)
				.setColor(new Color(0, 100, 0));

		for (String id : ChannelManager.getLogChannels()) {
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

	public static String getItemString(MarketItem item) {
		String itemString = "```";

		itemString += item.getName();
		itemString += " ";

		if (item.getQuantity() > 1)
			itemString += "x" + item.getQuantity() + " ";

		itemString += "(" + PriceUtil.getFormattedPrice(item.getPrice()) + ")";
		itemString += "\n";

		itemString += String.join("\n", item.getLore());

		itemString += "```";

		return itemString;
	}

}
