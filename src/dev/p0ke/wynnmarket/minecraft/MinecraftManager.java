package dev.p0ke.wynnmarket.minecraft;

import com.github.steveice10.mc.protocol.data.game.entity.player.Hand;
import com.github.steveice10.mc.protocol.data.game.window.ClickItemParam;
import com.github.steveice10.mc.protocol.data.game.window.WindowAction;
import com.github.steveice10.mc.protocol.packet.ingame.client.ClientChatPacket;
import com.github.steveice10.mc.protocol.packet.ingame.client.player.ClientPlayerChangeHeldItemPacket;
import com.github.steveice10.mc.protocol.packet.ingame.client.player.ClientPlayerUseItemPacket;
import com.github.steveice10.mc.protocol.packet.ingame.client.window.ClientCloseWindowPacket;
import com.github.steveice10.mc.protocol.packet.ingame.client.window.ClientWindowActionPacket;
import com.github.steveice10.packetlib.Session;
import dev.p0ke.wynnmarket.data.instances.MarketItem;
import dev.p0ke.wynnmarket.discord.DiscordManager;
import dev.p0ke.wynnmarket.minecraft.auth.MinecraftAccount;
import dev.p0ke.wynnmarket.minecraft.enums.RarityFilter;
import dev.p0ke.wynnmarket.minecraft.event.EventBus;
import dev.p0ke.wynnmarket.minecraft.event.Listener;
import dev.p0ke.wynnmarket.minecraft.listeners.*;
import dev.p0ke.wynnmarket.minecraft.util.ActionIdUtil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class MinecraftManager {
    private static final long ACCOUNT_INTERVAL = 1000 * 60 * 60 * 6; // 6 hours

    private static final String CLIENT_ID = "19655257-1b8e-407e-9328-ef0b5faa355a";

    private static String npcId;
    private static List<MinecraftAccount> accounts;
    private static int accountIndex = -1;
    private static MinecraftAccount account;

    private static final EventBus eventBus = new EventBus();
    private static final List<Listener> listeners = new ArrayList<>();

    private static long lastLoginTime;
    private static final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
    private static boolean lobbySuccess = false;

    private static MarketHandler market;

    public static void startClient(String users[], String passes[], int[] classes, String npc) {
        npcId = npc;

        accounts = new ArrayList<>();
        for (int i = 0; i < users.length; i++) {
            accounts.add(new MinecraftAccount(users[i], passes[i], classes[i]));
        }

        startNextClient();
    }

    private static void startNextClient() {
        accountIndex = (accountIndex+1) % accounts.size();
        account = accounts.get(accountIndex);

        account.login(CLIENT_ID);

        eventBus.registerClient(account.getClient());
        resetListeners();

        account.getClient().connect();
        startLobbyChecker();

        lastLoginTime = System.currentTimeMillis();
    }

    public static void resetListeners() {
        listeners.forEach(Listener::finish);
        listeners.clear();

        listeners.add(new WindowHandler());
        listeners.add(new WorldJoinHandler());
        listeners.add(new ResourcePackHandler());
        listeners.add(market = new MarketHandler(npcId));
        listeners.add(new ClassSelectHandler(account.getClassIndex()));
        listeners.add(new ChatHandler());

        eventBus.clearListeners();
        listeners.forEach(eventBus::registerListener);
    }

    public static synchronized List<MarketItem> searchItem(String search, RarityFilter rarity) {
        if (search != null && search.equalsIgnoreCase("cancel")) return null; // prevent infinite loop
        return market.searchItems(search, rarity);
    }

    public static void reconnect() {
        account.logout();
        startNextClient();

        DiscordManager.clearStatus();
    }

    public static void rejoinWorld() {
        if (System.currentTimeMillis() - lastLoginTime > ACCOUNT_INTERVAL) {
            reconnect();
            return;
        }

        resetListeners();
        sendMessage("/hub");
        startLobbyChecker();

        DiscordManager.clearStatus();
    }

    public static void sendMessage(String message) {
        account.getClient().send(new ClientChatPacket(message));
    }

    public static void useItem(int hotbarSlot) {
        account.getClient().send(new ClientPlayerChangeHeldItemPacket(hotbarSlot));
        account.getClient().send(new ClientPlayerUseItemPacket(Hand.MAIN_HAND));
    }

    public static void clickWindow(int windowId, int slot) {
        int action = ActionIdUtil.getNewID(windowId);
        account.getClient().send(new ClientWindowActionPacket(windowId, action, slot, WindowAction.CLICK_ITEM,
                ClickItemParam.LEFT_CLICK, null, Collections.singletonMap(slot, null)));
    }

    public static void closeWindow(int windowId) {
        account.getClient().send(new ClientCloseWindowPacket(windowId));
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
        return account.getClient();
    }

}
