package dev.p0ke.wynnmarket.minecraft;

import com.github.steveice10.mc.auth.exception.request.RequestException;
import com.github.steveice10.mc.auth.service.AuthenticationService;
import com.github.steveice10.mc.auth.service.MsaAuthenticationService;
import com.github.steveice10.mc.auth.service.SessionService;
import com.github.steveice10.mc.protocol.MinecraftConstants;
import com.github.steveice10.mc.protocol.MinecraftProtocol;
import com.github.steveice10.mc.protocol.data.game.entity.player.Hand;
import com.github.steveice10.mc.protocol.data.game.window.ClickItemParam;
import com.github.steveice10.mc.protocol.data.game.window.WindowAction;
import com.github.steveice10.mc.protocol.packet.ingame.client.ClientChatPacket;
import com.github.steveice10.mc.protocol.packet.ingame.client.player.ClientPlayerChangeHeldItemPacket;
import com.github.steveice10.mc.protocol.packet.ingame.client.player.ClientPlayerUseItemPacket;
import com.github.steveice10.mc.protocol.packet.ingame.client.window.ClientCloseWindowPacket;
import com.github.steveice10.mc.protocol.packet.ingame.client.window.ClientWindowActionPacket;
import com.github.steveice10.packetlib.Session;
import com.github.steveice10.packetlib.tcp.TcpClientSession;
import dev.p0ke.wynnmarket.data.instances.MarketItem;
import dev.p0ke.wynnmarket.discord.DiscordManager;
import dev.p0ke.wynnmarket.minecraft.enums.RarityFilter;
import dev.p0ke.wynnmarket.minecraft.event.EventBus;
import dev.p0ke.wynnmarket.minecraft.event.Listener;
import dev.p0ke.wynnmarket.minecraft.listeners.*;
import dev.p0ke.wynnmarket.minecraft.util.ActionIdUtil;

import java.net.Proxy;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class MinecraftManager {

    private static final String CLIENT_ID = "19655257-1b8e-407e-9328-ef0b5faa355a";

    private static Session client;
    private static String username;
    private static String password;
    private static String npcId;
    private static int[] classIndices;

    private static EventBus eventBus;
    private static List<Listener> listeners = new ArrayList<>();

    private static ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
    private static boolean lobbySuccess = false;

    private static MarketHandler market;

    public static void startClient(String user, String pass, String npc, int[] classes) {
        username = user;
        password = pass;
        npcId = npc;
        classIndices = classes;

        startClient();
    }

    private static void startClient() {
        MinecraftProtocol protocol;
        try {
            AuthenticationService authService = new MsaAuthenticationService(CLIENT_ID);
            authService.setUsername(username);
            authService.setPassword(password);
            authService.setProxy(Proxy.NO_PROXY);
            authService.login();

            protocol = new MinecraftProtocol(authService.getSelectedProfile(), authService.getAccessToken());
            System.out.println("Successfully authenticated user.");
        } catch (RequestException e) {
            e.printStackTrace();
            return;
        }

        SessionService sessionService = new SessionService();
        sessionService.setProxy(Proxy.NO_PROXY);

        client = new TcpClientSession("lobby.wynncraft.com", 25565, protocol, null);
        client.setFlag(MinecraftConstants.SESSION_SERVICE_KEY, sessionService);

        eventBus = new EventBus(client);

        connectClient();
    }

    private static void connectClient() {
        resetListeners();
        client.connect();
        startLobbyChecker();
    }

    public static void resetListeners() {
        listeners.forEach(Listener::finish);
        listeners.clear();

        listeners.add(new WindowHandler());
        listeners.add(new WorldJoinHandler());
        listeners.add(new ResourcePackHandler());
        listeners.add(market = new MarketHandler(npcId));
        listeners.add(new ClassSelectHandler(classIndices));
        listeners.add(new ChatHandler());

        eventBus.clearListeners();
        listeners.forEach(l -> eventBus.registerListener(l));
    }

    public static synchronized List<MarketItem> searchItem(String search, RarityFilter rarity) {
        if (search != null && search.equalsIgnoreCase("cancel")) return null; // prevent infinite loop
        return market.searchItems(search, rarity);
    }

    public static void reconnect() {
        client.disconnect("Finished");
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
        client.send(new ClientChatPacket(message));
    }

    public static void useItem(int hotbarSlot) {
        client.send(new ClientPlayerChangeHeldItemPacket(hotbarSlot));
        client.send(new ClientPlayerUseItemPacket(Hand.MAIN_HAND));
    }

    public static void clickWindow(int windowId, int slot) {
        int action = ActionIdUtil.getNewID(windowId);
        client.send(new ClientWindowActionPacket(windowId, action, slot, WindowAction.CLICK_ITEM,
                ClickItemParam.LEFT_CLICK, null, Collections.singletonMap(slot, null)));
    }

    public static void closeWindow(int windowId) {
        client.send(new ClientCloseWindowPacket(windowId));
    }

    public static void startLobbyChecker() {
        lobbySuccess = false;
        scheduler.schedule(MinecraftManager::checkLobbySuccess, 30, TimeUnit.SECONDS);
    }

    public static void reportLobbySuccess() {
        lobbySuccess = true;
    }

    public static void checkLobbySuccess() {
        if (lobbySuccess) return;
        reconnect();
    }

    public static Session getClient() {
        return client;
    }

}
