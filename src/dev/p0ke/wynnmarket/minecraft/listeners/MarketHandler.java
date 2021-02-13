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
import com.github.steveice10.mc.protocol.packet.ingame.server.window.ServerSetSlotPacket;
import com.github.steveice10.mc.protocol.packet.ingame.server.window.ServerWindowItemsPacket;
import com.github.steveice10.packetlib.event.session.PacketReceivedEvent;

import dev.p0ke.wynnmarket.data.managers.MarketItemManager;
import dev.p0ke.wynnmarket.discord.BotManager;
import dev.p0ke.wynnmarket.minecraft.ClientManager;
import dev.p0ke.wynnmarket.minecraft.util.StringUtil;

public class MarketHandler extends Listener {

	@SuppressWarnings("unused")
	private static final String BLUE_NPC_TEXTURE = "eyJ0aW1lc3RhbXAiOjE1NDQ0MTY1MTgzNTIsInByb2ZpbGVJZCI6IjdlZGE1Y2JiODRkMDQzZGI4YjZiYWE3YTc1YTVhZWU4IiwicHJvZmlsZU5hbWUiOiJDcnVua2Fjb2xhIiwic2lnbmF0dXJlUmVxdWlyZWQiOnRydWUsInRleHR1cmVzIjp7IlNLSU4iOnsidXJsIjoiaHR0cDovL3RleHR1cmVzLm1pbmVjcmFmdC5uZXQvdGV4dHVyZS82YjUyZmFkNmM3NjhlMjQ2NjM4OGY1MTEyYjE3ODAzNTIzYjk4ZWNhZjQ4NGViMGM3MTE5YWQ2MDVlYTVkZDRiIn19fQ==";
	private static final String RED_NPC_TEXTURE = "eyJ0aW1lc3RhbXAiOjE1NDQ0MTY0ODc4MTUsInByb2ZpbGVJZCI6IjdlZGE1Y2JiODRkMDQzZGI4YjZiYWE3YTc1YTVhZWU4IiwicHJvZmlsZU5hbWUiOiJDcnVua2Fjb2xhIiwic2lnbmF0dXJlUmVxdWlyZWQiOnRydWUsInRleHR1cmVzIjp7IlNLSU4iOnsidXJsIjoiaHR0cDovL3RleHR1cmVzLm1pbmVjcmFmdC5uZXQvdGV4dHVyZS9lMjJhZjMxYTgyNTRhYjg4ZDQ1ODBlNDc3Njg0NGMwNDM0N2QzOWU5ODYzMmZkYWZhMWI4N2Y5ZDBmYjcxZThmIn19fQ==";

	private ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();

	private List<UUID> npcUuids = new ArrayList<>();
	private List<Integer> npcIds = new ArrayList<>();
	private boolean marketOpen = false;
	private boolean schedulerRunning = false;
	private long lastMarketUpdate;
	private int marketWindowId;

	@Override
	public void packetReceived(PacketReceivedEvent event) {
		try {
			if (event.getPacket() instanceof ServerPlayerPositionRotationPacket) {
				ServerPlayerPositionRotationPacket positionPacket = (ServerPlayerPositionRotationPacket) event.getPacket();
				ClientManager.getClient().getSession().send(new ClientTeleportConfirmPacket(positionPacket.getTeleportId()));
				ClientManager.getClient().getSession().send(new ClientPlayerPositionPacket(true, positionPacket.getX(), positionPacket.getY(), positionPacket.getZ()));
			}

			if (event.getPacket() instanceof ServerPlayerListEntryPacket) {
				ServerPlayerListEntryPacket listEntryPacket = (ServerPlayerListEntryPacket) event.getPacket();

				if (listEntryPacket.getAction() == PlayerListEntryAction.ADD_PLAYER) {
					for (PlayerListEntry entry : listEntryPacket.getEntries()) {
						Property textures = entry.getProfile().getProperty("textures");
						if (textures != null && textures.getValue().equals(RED_NPC_TEXTURE))
							npcUuids.add(entry.getProfile().getId());
					}
				}
			}

			if (event.getPacket() instanceof ServerSpawnPlayerPacket) {
				ServerSpawnPlayerPacket spawnPacket = (ServerSpawnPlayerPacket) event.getPacket();
				if (npcUuids.contains(spawnPacket.getUuid()))
					npcIds.add(spawnPacket.getEntityId());
			}

			if (event.getPacket() instanceof ServerEntityPositionPacket) {
				ServerEntityPositionPacket positionPacket = (ServerEntityPositionPacket) event.getPacket();
				if (!marketOpen && npcIds.contains(positionPacket.getEntityId())) {
					ClientPlayerInteractEntityPacket interactPacket = new ClientPlayerInteractEntityPacket(positionPacket.getEntityId(), InteractAction.INTERACT, Hand.MAIN_HAND, false);
					ClientManager.getClient().getSession().send(interactPacket);

					if (!schedulerRunning) {
						schedulerRunning = true;
						scheduler.scheduleAtFixedRate(this::checkMarket, 1, 1, TimeUnit.MINUTES);
						lastMarketUpdate = System.currentTimeMillis();
					}
				}
			}

			if (event.getPacket() instanceof ServerWindowItemsPacket) {
				ServerWindowItemsPacket itemsPacket = (ServerWindowItemsPacket) event.getPacket();
				String name = StringUtil.removeFormatting(WindowHandler.getWindowName(itemsPacket.getWindowId()));
				if (name.contains("Trade Market")) {
					marketOpen = true;
					lastMarketUpdate = System.currentTimeMillis();
					marketWindowId = itemsPacket.getWindowId();

					MarketItemManager.scanPage(itemsPacket.getItems());
				}
			}

			if (event.getPacket() instanceof ServerSetSlotPacket) {
				ServerSetSlotPacket slotPacket = (ServerSetSlotPacket) event.getPacket();
				if (marketOpen && slotPacket.getWindowId() == marketWindowId) {
					lastMarketUpdate = System.currentTimeMillis();

					if (slotPacket.getSlot() < 54 && slotPacket.getSlot() % 9 < 7)
						MarketItemManager.scanItem(slotPacket.getItem());
				}
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void checkMarket() {
		long current = System.currentTimeMillis();

		if (current - lastMarketUpdate > 60000) {
			System.out.println("Missing market updates, rejoining a world");
			BotManager.logMessage("Market Timeout", "No market updates in last minute, rejoining world");

			scheduler.shutdown();
			ClientManager.rejoinWorld();
		}
	}

	public void finish() {
		scheduler.shutdown();
	}

}
