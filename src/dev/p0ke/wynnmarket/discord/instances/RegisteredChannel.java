package dev.p0ke.wynnmarket.discord.instances;

import java.util.ArrayList;
import java.util.List;

import dev.p0ke.wynnmarket.data.instances.MarketItem;
import dev.p0ke.wynnmarket.discord.instances.filters.ItemFilter;

public class RegisteredChannel {

	private String id;
	private boolean log;
	private List<FollowedItem> followedItems;

	public RegisteredChannel(String id, boolean log) {
		this.id = id;
		this.log = log;

		this.followedItems = new ArrayList<>();
	}

	public String getId() {
		return id;
	}

	public boolean isLog() {
		return log;
	}

	public boolean addItem(String id, String name) {
		FollowedItem fi = new FollowedItem(name, id);
		if (followedItems.contains(fi)) return false;
		followedItems.add(fi);
		return true;
	}

	public boolean removeItem(String id, String name) {
		FollowedItem fi = new FollowedItem(name, id);
		return followedItems.remove(fi);
	}

	public boolean blockItem(String id, MarketItem item) {
		FollowedItem fi = getItemByUserAndName(id, item.getCleanedName());
		if (fi == null) return false;
		fi.blockItem(item);
		return true;
	}

	public boolean addItemFilter(String id, String name, ItemFilter filter) {
		FollowedItem fi = getItemByUserAndName(id, name);
		if (fi == null) return false;
		fi.addFilter(filter);
		return true;
	}

	public boolean clearItemFilters(String id, String name) {
		FollowedItem fi = getItemByUserAndName(id, name);
		if (fi == null) return false;
		fi.clearFilters();
		return true;
	}

	public List<String> getUsersForItem(MarketItem item) {
		List<String> users = new ArrayList<>();
		for (FollowedItem fi : followedItems) {
			if (fi.test(item))
				users.add(fi.getUser());
		}
		return users;
	}

	public List<String> getItemsForUser(String id) {
		List<String> items = new ArrayList<>();
		for (FollowedItem fi : followedItems) {
			if (fi.getUser().equals(id))
				items.add(fi.toString());
		}
		return items;
	}

	private FollowedItem getItemByUserAndName(String id, String name) {
		for (FollowedItem item : followedItems) {
			if (item.getUser().equals(id) && item.getName().equalsIgnoreCase(name))
				return item;
		}
		return null;
	}

}
