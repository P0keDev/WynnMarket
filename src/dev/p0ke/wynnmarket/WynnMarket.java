package dev.p0ke.wynnmarket;

import java.io.FileInputStream;
import java.util.Properties;

import dev.p0ke.wynnmarket.data.managers.MarketLogManager;
import dev.p0ke.wynnmarket.discord.BotManager;
import dev.p0ke.wynnmarket.minecraft.ClientManager;

public class WynnMarket {

	public static void main(String[] args) {
		try {
			Properties login = new Properties();
			login.load(new FileInputStream("login.properties"));

			if (!login.containsKey("discordtoken") || !login.containsKey("mcuser") || !login.containsKey("mcpass")) {
				System.out.println("Missing information in login.properties");
				return;
			}

			BotManager.start(login.getProperty("discordtoken"));
			MarketLogManager.start();
			ClientManager.startClient(login.getProperty("mcuser"), login.getProperty("mcpass"), login.getProperty("npc", "blue"));
		} catch (Exception e) {
			System.out.println("Failed to load login credentials:");
			e.printStackTrace();
		}
	}

}
