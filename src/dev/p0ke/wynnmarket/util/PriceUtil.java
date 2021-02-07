package dev.p0ke.wynnmarket.util;

public class PriceUtil {
	
	public static String getFormattedPrice(int price) {
		int stx = price / (64*64*64);
		price %= (64*64*64);
		
		int le = price / (64*64);
		price %= (64*64);
		
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
