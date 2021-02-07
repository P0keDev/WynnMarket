package dev.p0ke.wynnmarket.minecraft.listeners;

import com.github.steveice10.mc.protocol.data.game.ResourcePackStatus;
import com.github.steveice10.mc.protocol.packet.ingame.client.ClientResourcePackStatusPacket;
import com.github.steveice10.mc.protocol.packet.ingame.server.ServerResourcePackSendPacket;
import com.github.steveice10.packetlib.event.session.PacketReceivedEvent;

import dev.p0ke.wynnmarket.minecraft.ClientManager;

public class ResourcePackHandler extends Listener {
	
	public void packetReceived(PacketReceivedEvent event) {
		if (event.getPacket() instanceof ServerResourcePackSendPacket) {
			ClientManager.getClient().getSession().send(new ClientResourcePackStatusPacket(ResourcePackStatus.DECLINED));
		}
	}
	
	public void finish() { }

}
