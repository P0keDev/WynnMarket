package dev.p0ke.wynnmarket.minecraft.listeners;

import com.github.steveice10.mc.protocol.packet.ingame.server.window.ServerWindowItemsPacket;
import dev.p0ke.wynnmarket.discord.DiscordManager;
import dev.p0ke.wynnmarket.minecraft.MinecraftManager;
import dev.p0ke.wynnmarket.minecraft.event.Listener;
import dev.p0ke.wynnmarket.minecraft.event.PacketHandler;

public class ClassSelectHandler extends Listener {

    private final int classIndex;

    private boolean selected = false;

    public ClassSelectHandler(int index) {
        classIndex = index;
    }

    @PacketHandler
    public void onWindowItems(ServerWindowItemsPacket itemsPacket) {
        if (selected) return;

        String name = WindowHandler.getWindowName(itemsPacket.getWindowId());
        if (name == null || !name.contains("Select a Character")) return;

        int classSlot = classIndex + 4 * ((classIndex - 1) / 5); // transform class # to slot id
        MinecraftManager.clickWindow(itemsPacket.getWindowId(), classSlot);

        DiscordManager.infoMessage("Class Selection", "Selecting class: " + classIndex);

        // class selected flag
        selected = true;
    }

    @Override
    public void finish() {
    }
}
