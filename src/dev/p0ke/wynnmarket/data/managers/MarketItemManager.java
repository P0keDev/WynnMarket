package dev.p0ke.wynnmarket.data.managers;

import com.github.steveice10.mc.protocol.data.game.entity.metadata.ItemStack;
import com.google.common.collect.EvictingQueue;

import dev.p0ke.wynnmarket.data.instances.MarketItem;
import dev.p0ke.wynnmarket.discord.BotManager;

public class MarketItemManager {

	private static EvictingQueue<MarketItem> recentItems = EvictingQueue.create(64);

	public static void scanPage(ItemStack[] marketPage) {
		for (int slot = 0; slot < 54; slot++) {
			if (slot % 9 > 6) continue;

			MarketItem item = MarketItem.fromItemStack(marketPage[slot]);
			if (recentItems.contains(item)) continue;
			recentItems.add(item);

			MarketLogManager.logItem(item);
			BotManager.logItem(item);
		}
	}

	public static void scanItem(ItemStack stack) {
		MarketItem item = MarketItem.fromItemStack(stack);
		if (recentItems.contains(item)) return;
		recentItems.add(item);

		MarketLogManager.logItem(item);
		BotManager.logItem(item);
	}

	public static void addItem(MarketItem item) {
		recentItems.add(item);
	}

}
