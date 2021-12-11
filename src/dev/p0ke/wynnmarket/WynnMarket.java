package dev.p0ke.wynnmarket;

import dev.p0ke.wynnmarket.data.managers.MarketLogManager;
import dev.p0ke.wynnmarket.discord.DiscordManager;
import dev.p0ke.wynnmarket.minecraft.MinecraftManager;
import org.apache.commons.lang3.math.NumberUtils;

import java.io.FileInputStream;
import java.util.Arrays;
import java.util.Properties;

public class WynnMarket {

	private static final String LOGIN_FILE = "login.properties";

	private static String DISCORD_TOKEN;
	private static String MC_USER;
	private static String MC_PASS;
	private static String NPC_ID;
	private static int[] CLASSES;

	public static void main(String[] args) {
		if (!loadLoginInfo(LOGIN_FILE)) return;

		DiscordManager.start(DISCORD_TOKEN);
		MarketLogManager.start();
		MinecraftManager.startClient(MC_USER, MC_PASS, NPC_ID, CLASSES);
	}

	public static boolean loadLoginInfo(String filename) {
		try {
			Properties login = new Properties();
			login.load(new FileInputStream(filename));

			if (!login.containsKey("discordtoken") || !login.containsKey("mcuser") || !login.containsKey("mcpass")) {
				System.out.println("Missing information in login.properties");
				return false;
			}

			DISCORD_TOKEN = login.getProperty("discordtoken");
			MC_USER = login.getProperty("mcuser");
			MC_PASS = login.getProperty("mcpass");
			NPC_ID = login.getProperty("npc");
			CLASSES = Arrays.stream(login.getProperty("classes", "").split(","))
					.map(String::trim).filter(NumberUtils::isParsable).mapToInt(Integer::parseInt).toArray();
		} catch (Exception e) {
			System.out.println("Failed to load login credentials:");
			e.printStackTrace();
			return false;
		}
		return true;
	}

}
