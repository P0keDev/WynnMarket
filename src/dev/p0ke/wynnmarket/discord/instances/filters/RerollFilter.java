package dev.p0ke.wynnmarket.discord.instances.filters;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import dev.p0ke.wynnmarket.discord.enums.Comparison;

public class RerollFilter extends ItemFilter {

	private static final Pattern REROLL_PATTERN = Pattern.compile("(?:Normal|Unique|Rare|Legendary|Fabled|Mythic) Item(?: \\[(\\d+)\\])?");

	public RerollFilter(int value, Comparison comp) {
		super(value, comp);
	}

	@Override
	public boolean test(String line) {
		Matcher m = REROLL_PATTERN.matcher(line);
		if (!m.matches()) return true;

		int rerolls = (m.group(1) == null) ? 0 : Integer.parseInt(m.group(1));
		return comp.test(rerolls - value);
	}

	@Override
	public String toString() {
		return comp.symbol + " " + value + " Rerolls";
	}

}
