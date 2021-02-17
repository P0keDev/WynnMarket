package dev.p0ke.wynnmarket.discord.instances.filters;

import dev.p0ke.wynnmarket.data.instances.MarketItem;
import dev.p0ke.wynnmarket.discord.enums.Comparison;

public abstract class ItemFilter {

	protected int value;
	protected Comparison comp;

	public ItemFilter(int value, Comparison comp) {
		this.value = value;
		this.comp = comp;
	}

	public abstract boolean test(MarketItem item);

}
