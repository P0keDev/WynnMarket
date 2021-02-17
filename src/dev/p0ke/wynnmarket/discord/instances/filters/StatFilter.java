package dev.p0ke.wynnmarket.discord.instances.filters;

import dev.p0ke.wynnmarket.data.instances.MarketItem;
import dev.p0ke.wynnmarket.discord.BotManager;
import dev.p0ke.wynnmarket.discord.enums.Comparison;
import dev.p0ke.wynnmarket.discord.enums.StatType;

public class StatFilter extends ItemFilter {

	private StatType stat;

	public StatFilter(StatType stat, int value, Comparison comp) {
		super(value, comp);
		this.stat = stat;
	}

	public boolean test(MarketItem item) {
		for (String line : item.getLore()) {
			line = line.toLowerCase().replace("*", "");
			if (!stat.matches(line)) continue;

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
