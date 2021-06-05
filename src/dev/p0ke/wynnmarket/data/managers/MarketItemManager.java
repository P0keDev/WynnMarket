package dev.p0ke.wynnmarket.data.managers;

import java.util.List;

import com.github.steveice10.mc.protocol.data.game.entity.metadata.ItemStack;
import com.google.common.collect.EvictingQueue;

import dev.p0ke.wynnmarket.data.instances.MarketItem;
import dev.p0ke.wynnmarket.discord.DiscordManager;

public class MarketItemManager {

	public static final int QUEUE_SIZE = 64; // number of most recent items to track

	private static EvictingQueue<MarketItem> recentItems = EvictingQueue.create(QUEUE_SIZE);

	public static void scanPage(ItemStack[] marketPage) {
		for (int slot = 0; slot < 54; slot++) {
			if (slot % 9 > 6) continue; // skip the 2 right-most columns, they do not contain items

			// scan individual item
			scanItem(marketPage[slot]);
		}
	}

	public static void scanItem(ItemStack stack) {
		// create MarketItem and compare against 64 most recent items.
		// since the entire page is handled every time it updates, but only updates one item
		// at a time, the evicting queue is used to keep track of items that have already
		// been handled
		MarketItem item = MarketItem.fromItemStack(stack);
		if (recentItems.contains(item)) return;
		recentItems.add(item);

		// log to file and through discord
		MarketLogManager.logItem(item);
		DiscordManager.logItem(item);
	}

	public static void addItem(MarketItem item) {
		recentItems.add(item);
	}

	public static void scanSearchPage(ItemStack[] searchPage, List<MarketItem> list) {
		for (int slot = 0; slot < 54; slot++) {
			if (slot % 9 > 6) continue;

			MarketItem item = MarketItem.fromItemStack(searchPage[slot]);
			if (item == null) return; // reached end of results

			list.add(item);
		}
	}

}
