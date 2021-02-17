package dev.p0ke.wynnmarket.discord.instances.filters;

import dev.p0ke.wynnmarket.data.instances.MarketItem;
import dev.p0ke.wynnmarket.discord.enums.Comparison;

public class PriceFilter extends ItemFilter {

	public PriceFilter(int value, Comparison comp) {
		super(value, comp);
	}

	@Override
	public boolean test(MarketItem item) {
		return comp.test(item.getPrice() - value);
	}

	@Override
	public String toString() {
		return comp.symbol + " " + value + " Emeralds";
	}

}
