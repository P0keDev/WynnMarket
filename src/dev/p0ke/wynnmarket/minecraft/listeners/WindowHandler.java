package dev.p0ke.wynnmarket.minecraft.listeners;

import java.util.HashMap;
import java.util.Map;

import com.github.steveice10.mc.protocol.packet.ingame.client.window.ClientCloseWindowPacket;
import com.github.steveice10.mc.protocol.packet.ingame.client.window.ClientConfirmTransactionPacket;
import com.github.steveice10.mc.protocol.packet.ingame.server.window.ServerCloseWindowPacket;
import com.github.steveice10.mc.protocol.packet.ingame.server.window.ServerConfirmTransactionPacket;
import com.github.steveice10.mc.protocol.packet.ingame.server.window.ServerOpenWindowPacket;
import com.github.steveice10.packetlib.event.session.PacketReceivedEvent;

import dev.p0ke.wynnmarket.minecraft.ClientManager;
import dev.p0ke.wynnmarket.minecraft.util.StringUtil;

public class WindowHandler extends Listener {

	private static Map<Integer, String> windows = new HashMap<>();
	private static int currentWindow = -1;

	@Override
	public void packetReceived(PacketReceivedEvent event) {
		if (event.getPacket() instanceof ServerOpenWindowPacket) {
			ServerOpenWindowPacket windowPacket = (ServerOpenWindowPacket) event.getPacket();
			windows.put(windowPacket.getWindowId(), StringUtil.parseText(windowPacket.getName()));
			currentWindow = windowPacket.getWindowId();
		}

		if (event.getPacket() instanceof ServerCloseWindowPacket || event.getPacket() instanceof ClientCloseWindowPacket) {
			currentWindow = -1;
		}

		if (event.getPacket() instanceof ServerConfirmTransactionPacket) {
			ServerConfirmTransactionPacket confirmPacket = (ServerConfirmTransactionPacket) event.getPacket();
			ClientManager.getClient().getSession().send(new ClientConfirmTransactionPacket(confirmPacket.getWindowId(), confirmPacket.getActionId(), true));
		}
	}

	public static String getWindowName(int id) {
		return windows.get(id);
	}

	public static int getCurrentWindow() {
		return currentWindow;
	}

	public void finish() { }

}
