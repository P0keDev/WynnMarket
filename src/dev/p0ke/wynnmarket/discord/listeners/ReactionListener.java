package dev.p0ke.wynnmarket.discord.listeners;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.javacord.api.entity.message.Message;
import org.javacord.api.entity.user.User;
import org.javacord.api.event.message.reaction.ReactionAddEvent;
import org.javacord.api.listener.message.reaction.ReactionAddListener;

import com.google.common.collect.EvictingQueue;

import dev.p0ke.wynnmarket.data.instances.MarketItem;
import dev.p0ke.wynnmarket.discord.managers.ChannelManager;

public class ReactionListener implements ReactionAddListener {
	
	private static EvictingQueue<ImmutablePair<String, MarketItem>> messages = EvictingQueue.create(100);

	@Override
	public void onReactionAdd(ReactionAddEvent event) {
		if (!event.getReaction().get().getEmoji().equalsEmoji("\u274C")) return;
		Message message = event.getMessage().get();
		User user = event.getUser().get();
		ImmutablePair<String, MarketItem> messageData = getMessage(message.getIdAsString());
		if (messageData == null) return;
		
		if (!message.getMentionedUsers().contains(user)) return;
		
		if (ChannelManager.blockItem(event.getChannel().getIdAsString(), user.getIdAsString(), messageData.getValue()))
			event.getChannel().sendMessage(user.getMentionTag() + ": Successfully blocked item!");
		
	}
	
	private static ImmutablePair<String, MarketItem> getMessage(String id) {
		for (ImmutablePair<String, MarketItem> p : messages) {
			if (p.getKey().equals(id)) return p;
		}
		return null;
	}
	
	public static void addMessage(String id, MarketItem item) {
		messages.add(new ImmutablePair<String, MarketItem>(id, item));
	}

}
