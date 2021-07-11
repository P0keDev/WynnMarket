package dev.p0ke.wynnmarket.minecraft;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import com.github.steveice10.mc.auth.exception.request.RequestException;
import com.github.steveice10.mc.protocol.MinecraftProtocol;
import com.github.steveice10.mc.protocol.data.game.entity.player.Hand;
import com.github.steveice10.mc.protocol.data.game.window.ClickItemParam;
import com.github.steveice10.mc.protocol.data.game.window.WindowAction;
import com.github.steveice10.mc.protocol.packet.ingame.client.ClientChatPacket;
import com.github.steveice10.mc.protocol.packet.ingame.client.player.ClientPlayerChangeHeldItemPacket;
import com.github.steveice10.mc.protocol.packet.ingame.client.player.ClientPlayerUseItemPacket;
import com.github.steveice10.mc.protocol.packet.ingame.client.window.ClientCloseWindowPacket;
import com.github.steveice10.mc.protocol.packet.ingame.client.window.ClientWindowActionPacket;
import com.github.steveice10.packetlib.Client;
import com.github.steveice10.packetlib.tcp.TcpSessionFactory;

import dev.p0ke.wynnmarket.data.instances.MarketItem;
import dev.p0ke.wynnmarket.discord.DiscordManager;
import dev.p0ke.wynnmarket.minecraft.enums.RarityFilter;
import dev.p0ke.wynnmarket.minecraft.event.EventBus;
import dev.p0ke.wynnmarket.minecraft.event.Listener;
import dev.p0ke.wynnmarket.minecraft.listeners.MarketHandler;
import dev.p0ke.wynnmarket.minecraft.listeners.ResourcePackHandler;
import dev.p0ke.wynnmarket.minecraft.listeners.WindowHandler;
import dev.p0ke.wynnmarket.minecraft.listeners.WorldJoinHandler;
import dev.p0ke.wynnmarket.minecraft.util.ActionIdUtil;

public class MinecraftManager {

	private static Client client;
	private static String username;
	private static String password;
	private static String npcId;

	private static EventBus eventBus;
	private static List<Listener> listeners = new ArrayList<>();

	private static ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
	private static boolean lobbySuccess = false;

	private static MarketHandler market;

	public static void startClient(String user, String pass, String npc) {
		username = user;
		password = pass;
		npcId = npc;

		startClient();
	}

	private static void startClient() {
		MinecraftProtocol protocol = null;
		try {
			protocol = new MinecraftProtocol(username, password);
			System.out.println("Successfully authenticated user.");
		} catch (RequestException e) {
			e.printStackTrace();
			return;
		}

		client = new Client("lobby.wynncraft.com", 25565, protocol, new TcpSessionFactory());
		eventBus = new EventBus(client);

		connectClient();
	}

	private static void connectClient() {
		resetListeners();
		client.getSession().connect();
		startLobbyChecker();
	}

	public static void resetListeners() {
		listeners.forEach(l -> l.finish());
		listeners.clear();

		listeners.add(new WindowHandler());
		listeners.add(new WorldJoinHandler());
		listeners.add(new ResourcePackHandler());
		listeners.add(market = new MarketHandler(npcId));

		eventBus.clearListeners();
		listeners.forEach(l -> eventBus.registerListener(l));
	}

	public static synchronized List<MarketItem> searchItem(String search, RarityFilter rarity) {
		if (search != null && search.equalsIgnoreCase("cancel")) return null; // prevent infinite loop
		return market.searchItems(search, rarity);
	}

	public static void reconnect() {
		client.getSession().disconnect("Finished");
		startClient();

		DiscordManager.clearStatus();
	}

	public static void rejoinWorld() {
		resetListeners();
		sendMessage("/hub");
		startLobbyChecker();

		DiscordManager.clearStatus();
	}

	public static void sendMessage(String message) {
		client.getSession().send(new ClientChatPacket(message));
	}

	public static void useItem(int hotbarSlot) {
		client.getSession().send(new ClientPlayerChangeHeldItemPacket(hotbarSlot));
		client.getSession().send(new ClientPlayerUseItemPacket(Hand.MAIN_HAND));
	}

	public static void clickWindow(int windowId, int slot) {
		int action = ActionIdUtil.getNewID(windowId);
		client.getSession().send(new ClientWindowActionPacket(windowId, action, slot, null, WindowAction.CLICK_ITEM, ClickItemParam.LEFT_CLICK));
	}

	public static void closeWindow(int windowId) {
		client.getSession().send(new ClientCloseWindowPacket(windowId));
	}

	public static void startLobbyChecker() {
		lobbySuccess = false;
		scheduler.schedule(MinecraftManager::checkLobbySuccess, 30, TimeUnit.SECONDS);
	}

	public static void reportLobbySuccess() {
		lobbySuccess = true;
	}

	public static void checkLobbySuccess() {
		if (lobbySuccess == true) return;
		reconnect();
	}

	public static Client getClient() {
		return client;
	}

}
