package dev.p0ke.wynnmarket.minecraft.listeners;

import com.github.steveice10.mc.protocol.data.game.ResourcePackStatus;
import com.github.steveice10.mc.protocol.packet.ingame.client.ClientResourcePackStatusPacket;
import com.github.steveice10.mc.protocol.packet.ingame.server.ServerResourcePackSendPacket;

import dev.p0ke.wynnmarket.minecraft.MinecraftManager;
import dev.p0ke.wynnmarket.minecraft.event.Listener;
import dev.p0ke.wynnmarket.minecraft.event.PacketHandler;

public class ResourcePackHandler extends Listener {

	@PacketHandler
	public void onServerResourcePack(ServerResourcePackSendPacket resourcePacket) {
		MinecraftManager.getClient().getSession().send(new ClientResourcePackStatusPacket(ResourcePackStatus.DECLINED));
	}

	public void finish() { }

}
