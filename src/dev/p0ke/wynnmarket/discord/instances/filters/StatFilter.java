package dev.p0ke.wynnmarket.discord.instances.filters;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.text.WordUtils;

import dev.p0ke.wynnmarket.discord.BotManager;
import dev.p0ke.wynnmarket.discord.enums.Comparison;

@SuppressWarnings("deprecation")
public class StatFilter extends ItemFilter {

	private String stat;

	public StatFilter(String stat, int value, Comparison comp) {
		super(value, comp);
		this.stat = stat.toLowerCase();
	}

	public boolean test(String line) {
		line = line.toLowerCase();
		if (!line.contains(stat)) return true;

		try {
			String idString = StringUtils.substringBefore(line, " ")
					.replace("*", "").replace("/4s", "").replace("/3s", "").replace("%", "").replace("+", "");
			int id = Integer.parseInt(idString);

			return comp.test(id - value);
		} catch (NumberFormatException e) {
			BotManager.logMessage("Filter Error", "Failed to parse ID: " + line);
		}
		return true;
	}

	@Override
	public String toString() {
		return comp.symbol + " " + value + " " + WordUtils.capitalize(stat);
	}

}
