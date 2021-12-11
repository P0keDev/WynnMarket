package dev.p0ke.wynnmarket.minecraft.util;

import java.util.regex.Pattern;

import com.github.steveice10.mc.protocol.packet.ingame.server.ServerChatPacket;
import com.github.steveice10.opennbt.tag.builtin.StringTag;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.kyori.adventure.text.TextComponent;

public class StringUtil {

	private static final Pattern FORMATTING_CODE_PATTERN = Pattern.compile("(?i)\u00a7[0-9A-FK-OR]");

	private static JsonParser parser = new JsonParser();

	public static String parseStringTag(StringTag tag) {
		return parseText(tag.getValue());
	}

	public static String parseChatMessage(ServerChatPacket packet) {
		if (!(packet.getMessage() instanceof TextComponent)) return "";

		StringBuilder msg = new StringBuilder();
		TextComponent component = (TextComponent) packet.getMessage();
		msg.append(component.content());
		component.children().forEach(c -> {
			if (c instanceof TextComponent)
				msg.append(((TextComponent) c).content());
		});

		return msg.toString();
	}

	public static String parseText(String input) {
		String text = "";

		try {
			JsonElement line = parser.parse(input);
			if (!line.isJsonObject()) return line.getAsString();

			JsonObject lineJson = line.getAsJsonObject();
			if (lineJson.has("text")) text += lineJson.get("text").getAsString();
			if (lineJson.has("extra")) text += readExtraTag(lineJson.get("extra").getAsJsonArray());
		} catch (Exception e) { }

		return text;
	}

	public static String readExtraTag(JsonArray extraArray) throws IllegalStateException, ClassCastException {
		String text = "";
		for (JsonElement entry : extraArray) {
			if (!entry.isJsonObject()) {
				text += entry.getAsString();
				continue;
			}

			JsonObject entryObject = entry.getAsJsonObject();
			if (entryObject.has("text")) text += entryObject.get("text").getAsString();
			if (entryObject.has("extra")) text += readExtraTag(entryObject.get("extra").getAsJsonArray());
		}

		return text;
	}

	public static String removeFormatting(String text) {
		return (text == null) ? "" : FORMATTING_CODE_PATTERN.matcher(text).replaceAll("");
	}

}
