package dev.p0ke.wynnmarket.minecraft.listeners;

import com.github.steveice10.mc.protocol.packet.ingame.client.window.ClientCloseWindowPacket;
import com.github.steveice10.mc.protocol.packet.ingame.server.window.ServerCloseWindowPacket;
import com.github.steveice10.mc.protocol.packet.ingame.server.window.ServerOpenWindowPacket;
import dev.p0ke.wynnmarket.minecraft.event.Listener;
import dev.p0ke.wynnmarket.minecraft.event.PacketHandler;
import dev.p0ke.wynnmarket.minecraft.util.StringUtil;

import java.util.HashMap;
import java.util.Map;

public class WindowHandler extends Listener {

    private static Map<Integer, String> windows = new HashMap<>();
    private static int currentWindow = -1;

    @PacketHandler
    public void onWindowOpen(ServerOpenWindowPacket windowPacket) {
        windows.put(windowPacket.getWindowId(), StringUtil.parseText(windowPacket.getName()));
        currentWindow = windowPacket.getWindowId();
    }

    @PacketHandler
    public void onServerClose(ServerCloseWindowPacket closePacket) {
        currentWindow = -1;
    }

    @PacketHandler
    public void onClientClose(ClientCloseWindowPacket closePacket) {
        currentWindow = -1;
    }

    public static String getWindowName(int id) {
        return windows.get(id);
    }

    public static int getCurrentWindow() {
        return currentWindow;
    }

    public void finish() {
    }

}
