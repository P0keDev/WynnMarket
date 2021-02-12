package dev.p0ke.wynnmarket.discord.commands;

import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.text.WordUtils;
import org.javacord.api.event.message.MessageCreateEvent;

import dev.p0ke.wynnmarket.discord.enums.Comparison;
import dev.p0ke.wynnmarket.discord.instances.filters.ItemFilter;
import dev.p0ke.wynnmarket.discord.instances.filters.RerollFilter;
import dev.p0ke.wynnmarket.discord.instances.filters.StatFilter;
import dev.p0ke.wynnmarket.discord.managers.ChannelManager;

@SuppressWarnings("deprecation")
public class FilterCommand implements Command {

	private static final Pattern FORMAT = Pattern.compile("(?<Item>.+) (?<Comparator>[!=<>]+) (?<Value>-?\\d+) (?<Stat>.+)");

	@Override
	public List<String> getNames() {
		return Arrays.asList("filter");
	}

	@Override
	public void execute(MessageCreateEvent event, String[] args) {
		if (!ChannelManager.isFollowerChannel(event.getChannel().getIdAsString())) {
			event.getChannel().sendMessage("This channel is not registered!");
			return;
		}

		String arg = StringUtils.substringAfter(String.join(" ", args), " ").toLowerCase();

		if (arg.endsWith("clear")) {
			String item = StringUtils.substringBeforeLast(arg, " ");
			if (ChannelManager.clearItemFilters(event.getChannel().getIdAsString(), event.getMessageAuthor().getIdAsString(), item)) {
				event.getChannel().sendMessage("Successfully cleared filters from " + WordUtils.capitalize(item) + "!");
			} else {
				event.getChannel().sendMessage("Failed to clear filters! Are you following that item?");
			}
			return;
		}

		Matcher m = FORMAT.matcher(arg);

		if (!m.matches()) {
			event.getChannel().sendMessage("Usage: `$filter <item> <comparison> <value> <stat>`");
			return;
		}

		String item = m.group("Item");
		Comparison comp = Comparison.fromSymbol(m.group("Comparator"));
		int value = Integer.parseInt(m.group("Value"));
		String stat = m.group("Stat");

		if (comp == null) {
			event.getChannel().sendMessage("Valid comparisons are: = != < > <= >=");
			return;
		}

		ItemFilter filter;
		if (stat.equalsIgnoreCase("rerolls")) {
			filter = new RerollFilter(value, comp);
		} else {
			filter = new StatFilter(stat, value, comp);
		}

		if (ChannelManager.addItemFilter(event.getChannel().getIdAsString(), event.getMessageAuthor().getIdAsString(), item, filter)) {
			event.getChannel().sendMessage("Successfully added filter to " + WordUtils.capitalize(item) + "!");
		} else {
			event.getChannel().sendMessage("Failed to add filter! Are you following that item?");
		}

	}

}