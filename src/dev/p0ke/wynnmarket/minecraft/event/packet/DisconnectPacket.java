package dev.p0ke.wynnmarket.minecraft.event.packet;

import com.github.steveice10.packetlib.event.session.DisconnectedEvent;
import com.github.steveice10.packetlib.io.NetInput;
import com.github.steveice10.packetlib.io.NetOutput;
import com.github.steveice10.packetlib.packet.Packet;

import java.io.IOException;

public class DisconnectPacket implements Packet {

    private DisconnectedEvent event;

    public DisconnectPacket(DisconnectedEvent event) {
        this.event = event;
    }

    public DisconnectedEvent getEvent() {
        return event;
    }

    @Override
    public void read(NetInput in) throws IOException {
    }

    @Override
    public void write(NetOutput out) throws IOException {
    }

    @Override
    public boolean isPriority() {
        return false;
    }

}
