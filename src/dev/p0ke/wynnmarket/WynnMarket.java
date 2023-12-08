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
    private static String[] MC_USERS;
    private static String[] MC_PASSES;
    private static int[] CLASSES;
    private static String NPC_ID;


    public static void main(String[] args) {
        if (!loadLoginInfo(LOGIN_FILE)) return;

        DiscordManager.start(DISCORD_TOKEN);
        MarketLogManager.start();
        MinecraftManager.startClient(MC_USERS, MC_PASSES, CLASSES, NPC_ID);
    }

    public static boolean loadLoginInfo(String filename) {
        try {
            Properties login = new Properties();
            login.load(new FileInputStream(filename));

            if (!login.containsKey("discordtoken") || !login.containsKey("mcusers") || !login.containsKey("mcpasses")) {
                System.out.println("Missing information in login.properties");
                return false;
            }

            DISCORD_TOKEN = login.getProperty("discordtoken");
            MC_USERS = Arrays.stream(login.getProperty("mcusers", "").split(","))
                    .map(String::trim).toArray(String[]::new);
            MC_PASSES = Arrays.stream(login.getProperty("mcpasses", "").split(","))
                    .map(String::trim).toArray(String[]::new);
            CLASSES = Arrays.stream(login.getProperty("classes", "").split(","))
                    .map(String::trim).filter(NumberUtils::isParsable).mapToInt(Integer::parseInt).toArray();
            NPC_ID = login.getProperty("npc");

            if (MC_USERS.length != MC_PASSES.length || MC_USERS.length != CLASSES.length) {
                System.out.println("Mismatched login info in login.properties!");
                return false;
            }
        } catch (Exception e) {
            System.out.println("Failed to load login credentials:");
            e.printStackTrace();
            return false;
        }
        return true;
    }

}
