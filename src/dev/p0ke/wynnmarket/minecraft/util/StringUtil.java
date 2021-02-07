package dev.p0ke.wynnmarket.minecraft.util;

import java.util.regex.Pattern;

import com.github.steveice10.opennbt.tag.builtin.StringTag;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class StringUtil {
	
	private static final Pattern FORMATTING_CODE_PATTERN = Pattern.compile("(?i)\u00a7[0-9A-FK-OR]");
	
	private static JsonParser parser = new JsonParser();
	
	public static String parseStringTag(StringTag tag) {
		return parseText(tag.getValue());
	}
	
	public static String parseText(String input) {
		String text = "";
		
		try {
			JsonObject lineJson = parser.parse(input).getAsJsonObject();
			if (lineJson.has("text")) text += lineJson.get("text").getAsString();
			if (lineJson.has("extra")) {
				JsonArray extra = lineJson.get("extra").getAsJsonArray();
				for (int j = 0; j < extra.size(); j++) {
					JsonObject extraEntry = extra.get(j).getAsJsonObject();
					if (extraEntry.has("text")) text += extraEntry.get("text").getAsString();
				}
			}
		} catch (Exception e) { e.printStackTrace(); }
		
		return text;
	}
	
	public static String removeFormatting(String text) {
		return (text == null) ? "" : FORMATTING_CODE_PATTERN.matcher(text).replaceAll("");
	}

}
