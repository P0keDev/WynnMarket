package dev.p0ke.wynnmarket.minecraft.event.packet;

import com.github.steveice10.packetlib.event.session.ConnectedEvent;
import com.github.steveice10.packetlib.io.NetInput;
import com.github.steveice10.packetlib.io.NetOutput;
import com.github.steveice10.packetlib.packet.Packet;

import java.io.IOException;

public class ConnectPacket implements Packet {

    private ConnectedEvent event;

    public ConnectPacket(ConnectedEvent event) {
        this.event = event;
    }

    public ConnectedEvent getEvent() {
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
