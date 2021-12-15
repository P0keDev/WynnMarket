package dev.p0ke.wynnmarket.minecraft.listeners;

import com.github.steveice10.mc.auth.data.GameProfile.Property;
import com.github.steveice10.mc.protocol.data.game.MessageType;
import com.github.steveice10.mc.protocol.data.game.PlayerListEntry;
import com.github.steveice10.mc.protocol.data.game.PlayerListEntryAction;
import com.github.steveice10.mc.protocol.data.game.entity.player.Hand;
import com.github.steveice10.mc.protocol.data.game.entity.player.InteractAction;
import com.github.steveice10.mc.protocol.packet.ingame.client.player.ClientPlayerInteractEntityPacket;
import com.github.steveice10.mc.protocol.packet.ingame.client.player.ClientPlayerPositionPacket;
import com.github.steveice10.mc.protocol.packet.ingame.client.world.ClientTeleportConfirmPacket;
import com.github.steveice10.mc.protocol.packet.ingame.server.ServerChatPacket;
import com.github.steveice10.mc.protocol.packet.ingame.server.ServerPlayerListEntryPacket;
import com.github.steveice10.mc.protocol.packet.ingame.server.entity.ServerEntityPositionPacket;
import com.github.steveice10.mc.protocol.packet.ingame.server.entity.player.ServerPlayerPositionRotationPacket;
import com.github.steveice10.mc.protocol.packet.ingame.server.entity.spawn.ServerSpawnPlayerPacket;
import com.github.steveice10.mc.protocol.packet.ingame.server.window.ServerCloseWindowPacket;
import com.github.steveice10.mc.protocol.packet.ingame.server.window.ServerSetSlotPacket;
import com.github.steveice10.mc.protocol.packet.ingame.server.window.ServerWindowItemsPacket;
import dev.p0ke.wynnmarket.data.instances.MarketItem;
import dev.p0ke.wynnmarket.data.managers.MarketItemManager;
import dev.p0ke.wynnmarket.discord.DiscordManager;
import dev.p0ke.wynnmarket.minecraft.MinecraftManager;
import dev.p0ke.wynnmarket.minecraft.enums.RarityFilter;
import dev.p0ke.wynnmarket.minecraft.event.Listener;
import dev.p0ke.wynnmarket.minecraft.event.PacketHandler;
import dev.p0ke.wynnmarket.minecraft.util.ItemParser;
import dev.p0ke.wynnmarket.minecraft.util.StringUtil;

import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class MarketHandler extends Listener {

	private static final int MARKET_CHECK_INTERVAL = 60;
	private static final int MARKET_TIMEOUT_INTERVAL = 60;
	private static final int TIMEOUT_THRESHOLD = 3;

	private String npcTexture;

	private ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();

	private List<UUID> npcUuids = new ArrayList<>();
	private List<Integer> npcIds = new ArrayList<>();
	private boolean marketOpen = false;
	private boolean schedulerRunning = false;
	private long lastMarketUpdate;
	private int timeouts = 0;
	private int marketWindowId;
	private int lastWindowId = -1;

	private boolean searching = false;
	private String searchName = null;
	private RarityFilter searchRarity = null;
	private List<MarketItem> searchResults;

	public MarketHandler(String npc) {
		npcTexture = npc;
	}

	@PacketHandler
	public void onPlayerPosition(ServerPlayerPositionRotationPacket positionPacket) {
		MinecraftManager.getClient().send(new ClientTeleportConfirmPacket(positionPacket.getTeleportId()));
		MinecraftManager.getClient().send(new ClientPlayerPositionPacket(true, positionPacket.getX(), positionPacket.getY(), positionPacket.getZ()));
	}

	@PacketHandler
	public void onListEntry(ServerPlayerListEntryPacket listEntryPacket) {
		if (listEntryPacket.getAction() != PlayerListEntryAction.ADD_PLAYER) return;

		for (PlayerListEntry entry : listEntryPacket.getEntries()) {
			Property textures = entry.getProfile().getProperty("textures");
			if (textures == null) continue;
			String skinData = new String(Base64.getDecoder().decode(textures.getValue()));
			if (skinData.contains(npcTexture))
				npcUuids.add(entry.getProfile().getId());
		}
	}

	@PacketHandler
	public void onSpawn(ServerSpawnPlayerPacket spawnPacket) {
		if (npcUuids.contains(spawnPacket.getUuid()))
			npcIds.add(spawnPacket.getEntityId());
	}

	@PacketHandler
	public void onEntityPosition(ServerEntityPositionPacket positionPacket) {
		if (marketOpen || !npcIds.contains(positionPacket.getEntityId())) return;

		ClientPlayerInteractEntityPacket interactPacket = new ClientPlayerInteractEntityPacket(positionPacket.getEntityId(), InteractAction.INTERACT, Hand.MAIN_HAND, false);
		MinecraftManager.getClient().send(interactPacket);

		if (!schedulerRunning) {
			schedulerRunning = true;
			scheduler.scheduleAtFixedRate(this::checkMarket, MARKET_CHECK_INTERVAL, MARKET_CHECK_INTERVAL, TimeUnit.SECONDS);
			lastMarketUpdate = System.currentTimeMillis();
		}
	}

	@PacketHandler
	public void onWindowItems(ServerWindowItemsPacket itemsPacket) {
		String name = StringUtil.removeFormatting(WindowHandler.getWindowName(itemsPacket.getWindowId()));

		if (lastWindowId == itemsPacket.getWindowId()) return; // avoid duplicate packets
		lastWindowId = itemsPacket.getWindowId();

		if (name.contains("Trade Market")) {
			if (!marketOpen) DiscordManager.infoMessage("Market Opened", "Successfully opened market.");

			marketOpen = true;
			lastMarketUpdate = System.currentTimeMillis();
			marketWindowId = itemsPacket.getWindowId();
			MarketItemManager.scanPage(itemsPacket.getItems());
			return;
		}

		if (name.contains("Filter Items") && searching) {
			// 3 options: only by name, only by rarity, or by both
			// if the name filter is in slot 0 or 1, we're ready to execute the search
			// if it isn't but the rarity filter is in slot 0, we either click the name filter (if defined) or execute
			// if neither are in slot 0, we click the rarity filter if one is defined, or the name filter

			int slot = (searchRarity != null) ? searchRarity.slot : 3; // 3 = name filter slot
			if (ItemParser.getName(itemsPacket.getItems()[0]).contains("Name Contains")
					|| ItemParser.getName(itemsPacket.getItems()[1]).contains("Name Contains")) {
				slot = 53; // search button
			} else if (searchRarity != null && ItemParser.getName(itemsPacket.getItems()[0]).contains(searchRarity.name)) {
				slot = (searchName != null) ? 3 : 53; // 3 = name filter, 53 = search button
			}

			MinecraftManager.clickWindow(itemsPacket.getWindowId(), slot); // 3 = name filter slot
			return;
		}

		if (name.contains("Search Results") && searching) {
			synchronized (this) {
				MarketItemManager.scanSearchPage(itemsPacket.getItems(), searchResults);
				searching = false;
				notifyAll();
				MinecraftManager.closeWindow(itemsPacket.getWindowId());
				marketOpen = false;
				return;
			}
		}
	}

	@PacketHandler
	public void onWindowClose(ServerCloseWindowPacket closePacket) {
		if (marketOpen && !searching) marketOpen = false;
	}

	@PacketHandler
	public void onSetSlot(ServerSetSlotPacket slotPacket) {
		if (!marketOpen || slotPacket.getWindowId() != marketWindowId) return;

		lastMarketUpdate = System.currentTimeMillis();

		if (slotPacket.getSlot() < 54 && slotPacket.getSlot() % 9 < 7)
			MarketItemManager.scanItem(slotPacket.getItem());
	}

	@PacketHandler
	public void onChat(ServerChatPacket chatPacket) {
		if (chatPacket.getType() == MessageType.NOTIFICATION) return;

		String message = StringUtil.parseChatMessage(chatPacket);
		message = StringUtil.removeFormatting(message);

		if (message.startsWith("Type the item name") && searchName != null)
			MinecraftManager.sendMessage(searchName);
	}

	public synchronized List<MarketItem> searchItems(String search, RarityFilter rarity) {
		if (!marketOpen) return null;
		if (search == null && rarity == null) return null;

		searching = true;
		searchName = search;
		searchRarity = rarity;
		searchResults = new ArrayList<>();

		MinecraftManager.clickWindow(marketWindowId, 35);
		while (searching) {
			try {
				wait();
			} catch (InterruptedException e) { }
		}

		searchName = null;
		searchRarity = null;

		return searchResults;
	}

	private void checkMarket() {
		long current = System.currentTimeMillis();

		if (current - lastMarketUpdate > MARKET_TIMEOUT_INTERVAL*1000) {
			timeouts++;

			if (timeouts >= TIMEOUT_THRESHOLD) {
				System.out.println("Missing market updates, rejoining a world");
				DiscordManager.infoMessage("Market Timeout", "No market updates within threshold, rejoining world");

				scheduler.shutdown();
				MinecraftManager.rejoinWorld();
			} else {
				DiscordManager.infoMessage("Missing Market Update", "Timeout #" + timeouts);
			}
			return;
		}

		timeouts = 0;
	}

	public synchronized void finish() {
		scheduler.shutdown();
		searching = false;
		notifyAll();
	}

}
