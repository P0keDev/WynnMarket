package dev.p0ke.wynnmarket.minecraft.listeners;

import com.github.steveice10.mc.protocol.data.game.entity.metadata.ItemStack;
import com.github.steveice10.mc.protocol.packet.ingame.server.window.ServerWindowItemsPacket;
import dev.p0ke.wynnmarket.minecraft.MinecraftManager;
import dev.p0ke.wynnmarket.minecraft.event.Listener;
import dev.p0ke.wynnmarket.minecraft.event.PacketHandler;

import java.util.Arrays;

public class ClassSelectHandler extends Listener {

    private int[] classIndices;
    private static int currentIndex = 0;

    public ClassSelectHandler(int[] indices) {
        classIndices = (indices.length > 0) ? indices : new int[]{ 1 };
    }

    @PacketHandler
    public void onWindowItems(ServerWindowItemsPacket itemsPacket) {
        String name = WindowHandler.getWindowName(itemsPacket.getWindowId());
        if (name == null || !name.contains("Select a Class")) return;

        System.out.println("In class select");

        // get next class in order
        int index = classIndices[currentIndex];
        int classSlot = index + 4*((index-1)/5); // transform class # to slot id
        ItemStack item = itemsPacket.getItems()[classSlot];
        if (item == null) {
            System.out.println("Item not loaded");
        }
        MinecraftManager.clickWindow(itemsPacket.getWindowId(), classSlot);

        // increment index
        currentIndex = (currentIndex+1) % classIndices.length;
    }

    @Override
    public void finish() { }
}
