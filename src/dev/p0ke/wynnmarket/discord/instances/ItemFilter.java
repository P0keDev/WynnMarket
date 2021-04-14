package dev.p0ke.wynnmarket.discord.instances;

import dev.p0ke.wynnmarket.data.instances.MarketItem;
import dev.p0ke.wynnmarket.discord.BotManager;
import dev.p0ke.wynnmarket.discord.enums.Comparison;
import dev.p0ke.wynnmarket.discord.enums.StatType;

public class ItemFilter {

	private StatType stat;
	private int value;
	private Comparison comp;

	public ItemFilter(StatType stat, int value, Comparison comp) {
		this.stat = stat;
		this.value = value;
		this.comp = comp;
	}

	public boolean test(MarketItem item) {
		if (stat == StatType.PRICE)
			return comp.test(item.getPrice() - value);

		for (String line : item.getLore()) {
			line = line.replace("*", "");
			if (line.isEmpty() || !stat.matches(line)) continue;

			try {
				int id = stat.getValue(line);
				return comp.test(id - value);
			} catch (NumberFormatException e) {
				BotManager.logMessage("Filter Error", "Failed to parse ID: " + line + "\nStat: " + stat.getName());
			}
		}
		return true;
	}

	@Override
	public String toString() {
		return comp.symbol + " " + value + " " + stat.getName();
	}

}
