package dev.p0ke.wynnmarket.minecraft.listeners;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import com.github.steveice10.mc.auth.data.GameProfile.Property;
import com.github.steveice10.mc.protocol.data.game.PlayerListEntry;
import com.github.steveice10.mc.protocol.data.game.PlayerListEntryAction;
import com.github.steveice10.mc.protocol.data.game.entity.player.Hand;
import com.github.steveice10.mc.protocol.data.game.entity.player.InteractAction;
import com.github.steveice10.mc.protocol.packet.ingame.client.player.ClientPlayerInteractEntityPacket;
import com.github.steveice10.mc.protocol.packet.ingame.client.player.ClientPlayerPositionPacket;
import com.github.steveice10.mc.protocol.packet.ingame.client.world.ClientTeleportConfirmPacket;
import com.github.steveice10.mc.protocol.packet.ingame.server.ServerPlayerListEntryPacket;
import com.github.steveice10.mc.protocol.packet.ingame.server.entity.ServerEntityPositionPacket;
import com.github.steveice10.mc.protocol.packet.ingame.server.entity.player.ServerPlayerPositionRotationPacket;
import com.github.steveice10.mc.protocol.packet.ingame.server.entity.spawn.ServerSpawnPlayerPacket;
import com.github.steveice10.mc.protocol.packet.ingame.server.window.ServerCloseWindowPacket;
import com.github.steveice10.mc.protocol.packet.ingame.server.window.ServerSetSlotPacket;
import com.github.steveice10.mc.protocol.packet.ingame.server.window.ServerWindowItemsPacket;

import dev.p0ke.wynnmarket.data.managers.MarketItemManager;
import dev.p0ke.wynnmarket.discord.BotManager;
import dev.p0ke.wynnmarket.minecraft.ClientManager;
import dev.p0ke.wynnmarket.minecraft.event.Listener;
import dev.p0ke.wynnmarket.minecraft.event.PacketHandler;
import dev.p0ke.wynnmarket.minecraft.util.StringUtil;

public class MarketHandler extends Listener {

	private static final String BLUE_NPC_TEXTURE = "eyJ0aW1lc3RhbXAiOjE1NDQ0MTY1MTgzNTIsInByb2ZpbGVJZCI6IjdlZGE1Y2JiODRkMDQzZGI4YjZiYWE3YTc1YTVhZWU4IiwicHJvZmlsZU5hbWUiOiJDcnVua2Fjb2xhIiwic2lnbmF0dXJlUmVxdWlyZWQiOnRydWUsInRleHR1cmVzIjp7IlNLSU4iOnsidXJsIjoiaHR0cDovL3RleHR1cmVzLm1pbmVjcmFmdC5uZXQvdGV4dHVyZS82YjUyZmFkNmM3NjhlMjQ2NjM4OGY1MTEyYjE3ODAzNTIzYjk4ZWNhZjQ4NGViMGM3MTE5YWQ2MDVlYTVkZDRiIn19fQ==";
	private static final String RED_NPC_TEXTURE = "eyJ0aW1lc3RhbXAiOjE1NDQ0MTY0ODc4MTUsInByb2ZpbGVJZCI6IjdlZGE1Y2JiODRkMDQzZGI4YjZiYWE3YTc1YTVhZWU4IiwicHJvZmlsZU5hbWUiOiJDcnVua2Fjb2xhIiwic2lnbmF0dXJlUmVxdWlyZWQiOnRydWUsInRleHR1cmVzIjp7IlNLSU4iOnsidXJsIjoiaHR0cDovL3RleHR1cmVzLm1pbmVjcmFmdC5uZXQvdGV4dHVyZS9lMjJhZjMxYTgyNTRhYjg4ZDQ1ODBlNDc3Njg0NGMwNDM0N2QzOWU5ODYzMmZkYWZhMWI4N2Y5ZDBmYjcxZThmIn19fQ==";

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

	public MarketHandler(String npcColor) {
		npcTexture = (npcColor.equalsIgnoreCase("red")) ? RED_NPC_TEXTURE : BLUE_NPC_TEXTURE;
	}

	@PacketHandler
	public void onPlayerPosition(ServerPlayerPositionRotationPacket positionPacket) {
		ClientManager.getClient().getSession().send(new ClientTeleportConfirmPacket(positionPacket.getTeleportId()));
		ClientManager.getClient().getSession().send(new ClientPlayerPositionPacket(true, positionPacket.getX(), positionPacket.getY(), positionPacket.getZ()));
	}

	@PacketHandler
	public void onListEntry(ServerPlayerListEntryPacket listEntryPacket) {
		if (listEntryPacket.getAction() != PlayerListEntryAction.ADD_PLAYER) return;

		for (PlayerListEntry entry : listEntryPacket.getEntries()) {
			Property textures = entry.getProfile().getProperty("textures");
			if (textures != null && textures.getValue().equals(npcTexture))
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
		ClientManager.getClient().getSession().send(interactPacket);

		if (!schedulerRunning) {
			schedulerRunning = true;
			scheduler.scheduleAtFixedRate(this::checkMarket, MARKET_CHECK_INTERVAL, MARKET_CHECK_INTERVAL, TimeUnit.SECONDS);
			lastMarketUpdate = System.currentTimeMillis();
		}
	}

	@PacketHandler
	public void onWindowItems(ServerWindowItemsPacket itemsPacket) {
		String name = StringUtil.removeFormatting(WindowHandler.getWindowName(itemsPacket.getWindowId()));
		if (!name.contains("Trade Market")) return;

		marketOpen = true;
		lastMarketUpdate = System.currentTimeMillis();
		marketWindowId = itemsPacket.getWindowId();

		MarketItemManager.scanPage(itemsPacket.getItems());
	}

	@PacketHandler
	public void onWindowClose(ServerCloseWindowPacket closePacket) {
		if (marketOpen) marketOpen = false;
	}

	@PacketHandler
	public void onSetSlot(ServerSetSlotPacket slotPacket) {
		if (!marketOpen || slotPacket.getWindowId() != marketWindowId) return;

		lastMarketUpdate = System.currentTimeMillis();

		if (slotPacket.getSlot() < 54 && slotPacket.getSlot() % 9 < 7)
			MarketItemManager.scanItem(slotPacket.getItem());
	}

	private void checkMarket() {
		long current = System.currentTimeMillis();

		if (current - lastMarketUpdate > MARKET_TIMEOUT_INTERVAL*1000) {
			timeouts++;

			if (timeouts >= TIMEOUT_THRESHOLD) {
				System.out.println("Missing market updates, rejoining a world");
				BotManager.logMessage("Market Timeout", "No market updates within threshold, rejoining world");

				scheduler.shutdown();
				ClientManager.rejoinWorld();
			} else {
				BotManager.logMessage("Missing Market Update", "Timeout #" + timeouts);
			}
			return;
		}

		timeouts = 0;
	}

	public void finish() {
		scheduler.shutdown();
	}

}
