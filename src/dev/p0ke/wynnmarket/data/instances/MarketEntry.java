package dev.p0ke.wynnmarket.data.instances;

public class MarketEntry {
	
	private MarketItem item;
	private long time;
	
	public MarketEntry(MarketItem item) {
		this.item = item;
		time = System.currentTimeMillis();
	}
	
	public MarketItem getItem() {
		return item;
	}
	
	public long getTime() {
		return time;
	}

}
