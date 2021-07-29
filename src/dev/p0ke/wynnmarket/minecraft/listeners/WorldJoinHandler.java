package dev.p0ke.wynnmarket.minecraft.listeners;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.github.steveice10.mc.protocol.data.game.MessageType;
import com.github.steveice10.mc.protocol.data.game.entity.metadata.ItemStack;
import com.github.steveice10.mc.protocol.packet.ingame.server.ServerChatPacket;
import com.github.steveice10.mc.protocol.packet.ingame.server.ServerJoinGamePacket;
import com.github.steveice10.mc.protocol.packet.ingame.server.window.ServerWindowItemsPacket;

import dev.p0ke.wynnmarket.discord.DiscordManager;
import dev.p0ke.wynnmarket.minecraft.MinecraftManager;
import dev.p0ke.wynnmarket.minecraft.event.Listener;
import dev.p0ke.wynnmarket.minecraft.event.PacketHandler;
import dev.p0ke.wynnmarket.minecraft.util.ActionIdUtil;
import dev.p0ke.wynnmarket.minecraft.util.ItemParser;
import dev.p0ke.wynnmarket.minecraft.util.StringUtil;

public class WorldJoinHandler extends Listener {

	private static final Pattern PLAYERCOUNT_PATTERN = Pattern.compile("✔ (\\d+)/\\d+ Players Online");
	private static final Pattern WORLD_PATTERN = Pattern.compile("World (\\d+)");

	private ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();

	private List<Integer> attemptedWorlds = new ArrayList<>();
	private int attempts = 0;
	private static int lastWorld = 0;

	@PacketHandler
	public void onWindowItems(ServerWindowItemsPacket itemsPacket) {
		// inventory
		if (itemsPacket.getWindowId() == 0 && itemsPacket.getItems()[36] != null) {
			String name = ItemParser.getName(itemsPacket.getItems()[36]);
			if (name.contains("Quick Connect")) {
				MinecraftManager.reportLobbySuccess();
				scheduler.scheduleAtFixedRate(this::useCompass, 10, 25, TimeUnit.SECONDS);
			}
			return;
		}

		// compass menu
		String name = WindowHandler.getWindowName(itemsPacket.getWindowId());
		if (name == null || !name.contains("Wynncraft Servers")) return;

		for (int i = 0; i < 54; i++) {
			ItemStack item = itemsPacket.getItems()[i];
			if (item == null) continue;
			String itemName = StringUtil.removeFormatting(ItemParser.getName(item));
			List<String> itemLore = ItemParser.getLore(item);

			Matcher m = WORLD_PATTERN.matcher(itemName);
			if (!m.find()) continue;

			int players = getPlayerCount(itemLore);
			int worldNumber = Integer.parseInt(m.group(1));

			if (lastWorld != worldNumber && !attemptedWorlds.contains(worldNumber) && players > -1 && players < 40) {
				System.out.println("Attempting to join world " + worldNumber);
				DiscordManager.logMessage("World Join", "Attempting to join world " + worldNumber);

				MinecraftManager.clickWindow(itemsPacket.getWindowId(), i);
				attemptedWorlds.add(worldNumber);
				lastWorld = worldNumber;
				return;
			}
		}
	}

	@PacketHandler
	public void onChat(ServerChatPacket chatPacket) {
		if (chatPacket.getType() == MessageType.NOTIFICATION) return;

		String message = StringUtil.parseText(chatPacket.getMessage().toString());
		message = StringUtil.removeFormatting(message);

		if (message.startsWith("Your class has automatically been selected")) {
			scheduler.shutdown();
			attempts = 0;
			DiscordManager.setStatus("WC" + lastWorld);
			return;
		}

		if (message.startsWith("The server is restarting")) {
			System.out.println("World restart, rejoining");
			DiscordManager.logMessage("World Restart", "Rejoining...");

			MinecraftManager.rejoinWorld();
			return;
		}
	}

	@PacketHandler
	public void onJoin(ServerJoinGamePacket joinPacket) {
		ActionIdUtil.reset();
	}

	private void useCompass() {
		attempts++;
		System.out.println("Join attempt " + attempts);

		if (attempts >= 5) {
			System.out.println("Attempt limit reached, reconnecting");
			DiscordManager.logMessage("Reconnecting", "Failed to join a world, reconnecting to Wynn");
			scheduler.shutdown();
			MinecraftManager.reconnect();
			return;
		}

		MinecraftManager.useItem(0);
	}

	public void finish() {
		scheduler.shutdown();
	}

	private static int getPlayerCount(List<String> lore) {
		for (String line : lore) {
			Matcher m = PLAYERCOUNT_PATTERN.matcher(line);
			if (m.matches()) return Integer.parseInt(m.group(1));
		}
		return -1;
	}

}
