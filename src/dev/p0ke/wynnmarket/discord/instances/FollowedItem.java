package dev.p0ke.wynnmarket.discord.instances;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.lang3.text.WordUtils;

import dev.p0ke.wynnmarket.data.instances.MarketItem;

@SuppressWarnings("deprecation")
public class FollowedItem {

	private String name;
	private List<ItemFilter> filters;
	private String userId;

	private List<MarketItem> blockList;

	public FollowedItem(String name, String userId) {
		this.name = name.toLowerCase();
		this.userId = userId;

		filters = new ArrayList<>();
		blockList = new ArrayList<>();
	}

	public boolean test(MarketItem item) {
		if (!item.getCleanedName().equalsIgnoreCase(name)) return false;

		if (blockList.contains(item.createGenericCopy())) return false;

		for (ItemFilter f : filters) {
			if (!f.test(item)) return false;
		}

		return true;
	}

	public void addFilter(ItemFilter filter) {
		filters.add(filter);
	}

	public void clearFilters() {
		filters.clear();
	}

	public void blockItem(MarketItem item) {
		blockList.add(item.createGenericCopy());
	}

	public String getUser() {
		return userId;
	}

	public String getName() {
		return WordUtils.capitalize(name);
	}

	@Override
	public String toString() {
		String str = getName();
		if (!filters.isEmpty())
			str += " (" + filters.stream().map(Object::toString).collect(Collectors.joining(", ")) + ")";
		return str;
	}

	@Override
	public boolean equals(Object o) {
		if (!(o instanceof FollowedItem)) return false;
		FollowedItem other = (FollowedItem) o;
		return name.equals(other.name) && userId.equals(other.userId);
	}

}
