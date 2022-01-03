package dev.p0ke.wynnmarket.discord.util;

import dev.p0ke.wynnmarket.data.instances.MarketItem;

public class ItemUtil {

    public static String getItemString(MarketItem item) {
        String itemString = "```";

        itemString += item.getName();
        itemString += " ";

        if (item.getQuantity() > 1)
            itemString += "x" + item.getQuantity() + " ";

        itemString += "(" + getFormattedPrice(item.getPrice()) + ")";
        itemString += "\n";

        itemString += String.join("\n", item.getLore());

        itemString += "```";

        return itemString;
    }

    public static String getFormattedPrice(int price) {
        int stx = price / (64 * 64 * 64);
        price %= (64 * 64 * 64);

        int le = price / (64 * 64);
        price %= (64 * 64);

        int eb = price / 64;
        price %= 64;

        String formattedPrice = "";
        if (stx > 0) formattedPrice += stx + "stx ";
        if (le > 0) formattedPrice += le + "le ";
        if (eb > 0) formattedPrice += eb + "eb ";
        if (price > 0) formattedPrice += price + "e";

        return formattedPrice.trim();
    }

}
