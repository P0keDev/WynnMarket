package dev.p0ke.wynnmarket.data.instances;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;

import com.github.steveice10.mc.protocol.data.game.entity.metadata.ItemStack;

import dev.p0ke.wynnmarket.minecraft.util.ItemParser;
import dev.p0ke.wynnmarket.minecraft.util.StringUtil;

public class MarketItem {

	private static final Pattern PRICE_PATTERN = Pattern.compile(" - (?:(\\d+) x )?(\\d+)².*");
	private static final Pattern TIER_PATTERN = Pattern.compile("(Normal|Unique|Rare|Legendary|Fabled|Mythic) Item.*");

	private String name;
	private int price;
	private int quantity;
	private List<String> lore;

	public MarketItem(String name, int quantity, int price, List<String> lore) {
		this.name = name;
		this.quantity = quantity;
		this.price = price;
		this.lore = lore;
	}

	public String getName() {
		return name;
	}

	public String getCleanedName() {
		return StringUtils.substringBefore(name, "[").replace("Unidentified", "").trim();
	}

	public int getQuantity() {
		return quantity;
	}

	public int getPrice() {
		return price;
	}

	public List<String> getLore() {
		return lore;
	}

	public boolean isUnidentified() {
		return name.startsWith("Unidentified");
	}

	public MarketItem createGenericCopy() {
		return new MarketItem(this.name, 1, 0, this.lore);
	}

	@Override
	public String toString() {
		return name + " (" + price + "E) " + ((quantity > 1) ? ("x" + quantity + "\n") : ("\n")) + String.join("\n", lore);
	}

	@Override
	public boolean equals(Object o) {
		if (!(o instanceof MarketItem)) return false;

		MarketItem other = (MarketItem) o;
		if (!name.equals(other.name)) return false;

		if (price != other.price) return false;
		if (quantity != other.quantity) return false;

		if (lore.size() != other.lore.size()) return false;

		for (int i = 0; i < lore.size(); i++) {
			if (!lore.get(i).equals(other.lore.get(i))) return false;
		}

		return true;
	}

	public static MarketItem fromItemStack(ItemStack item) {
		String name = StringUtil.removeFormatting(ItemParser.getName(item)).replace("ÀÀÀ", " ").replace("À", "");
		List<String> itemLore = ItemParser.getLore(item);

		List<String> lore = new ArrayList<>();
		int price = 0;
		int quantity = 1;

		Matcher m = PRICE_PATTERN.matcher(itemLore.get(2).replace(",", ""));
		if (m.matches()) {
			price = Integer.parseInt(m.group(2));
			if (m.group(1) != null) quantity = Integer.parseInt(m.group(1));
		}

		for (int i = 3; i < itemLore.size(); i++) {
			String line = itemLore.get(i);
			if (line.isEmpty() && lore.isEmpty()) continue;

			lore.add(line.replace("ÀÀÀ", " ").replace("À", ""));

			Matcher m1 = TIER_PATTERN.matcher(line);
			if (m1.matches()) break;
		}

		return new MarketItem(name, quantity, price, lore);
	}

}
