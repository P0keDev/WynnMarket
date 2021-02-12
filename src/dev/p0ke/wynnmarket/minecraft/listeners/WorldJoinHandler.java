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
import com.github.steveice10.packetlib.event.session.PacketReceivedEvent;

import dev.p0ke.wynnmarket.discord.BotManager;
import dev.p0ke.wynnmarket.minecraft.ClientManager;
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

	@Override
	public void packetReceived(PacketReceivedEvent event) {
		try {
			if (event.getPacket() instanceof ServerWindowItemsPacket) {
				ServerWindowItemsPacket itemsPacket = (ServerWindowItemsPacket) event.getPacket();
				if (itemsPacket.getWindowId() == 0 && itemsPacket.getItems()[36] != null) {
					String name = ItemParser.getName(itemsPacket.getItems()[36]);
					if (name.contains("Quick Connect")) {
						ClientManager.reportLobbySuccess();
						scheduler.scheduleAtFixedRate(this::useCompass, 10, 25, TimeUnit.SECONDS);
					}
					return;
				}

				String name = WindowHandler.getWindowName(itemsPacket.getWindowId());
				if (name != null && name.contains("Wynncraft Servers")) {
					for (int i = 0; i < 54; i++) {
						ItemStack item = itemsPacket.getItems()[i];
						if (item == null) continue;
						String itemName = StringUtil.removeFormatting(ItemParser.getName(item));
						List<String> itemLore = ItemParser.getLore(item);

						Matcher m = WORLD_PATTERN.matcher(itemName);
						if (!m.find()) continue;

						int players = getPlayerCount(itemLore);
						int worldNumber = Integer.parseInt(m.group(1));

						if (lastWorld != worldNumber && !attemptedWorlds.contains(worldNumber) && players > -1 && players < 50) {
							System.out.println("Attempting to join world " + worldNumber);
							BotManager.logMessage("World Join", "Attempting to join world " + worldNumber);

							ClientManager.clickWindow(itemsPacket.getWindowId(), i);
							attemptedWorlds.add(worldNumber);
							lastWorld = worldNumber;
							return;
						}
					}
				}
			}

			if (event.getPacket() instanceof ServerChatPacket) {
				ServerChatPacket chatPacket = ((ServerChatPacket) event.getPacket());
				if (chatPacket.getType() == MessageType.NOTIFICATION) return;

				String message = StringUtil.parseText(chatPacket.getMessage().toString());
				message = StringUtil.removeFormatting(message);
				// if (!message.isEmpty()) System.out.println("[CHAT] " + message);

				if (message.startsWith("Loading Resource Pack")) {
					scheduler.shutdown();
					attempts = 0;
					BotManager.setStatus("WC" + lastWorld);
					return;
				}

				if (message.startsWith("The server is restarting")) {
					System.out.println("World restart, rejoining");
					BotManager.logMessage("World Restart", "Rejoining...");

					ClientManager.rejoinWorld();
					return;
				}
			}

			if (event.getPacket() instanceof ServerJoinGamePacket) {
				ActionIdUtil.reset();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void useCompass() {
		attempts++;
		System.out.println("Join attempt " + attempts);

		if (attempts >= 5) {
			System.out.println("Attempt limit reached, reconnecting");
			BotManager.logMessage("Reconnecting", "Failed to join a world, reconnecting to Wynn");
			scheduler.shutdown();
			ClientManager.reconnect();
			return;
		}

		ClientManager.useItem(0);
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
