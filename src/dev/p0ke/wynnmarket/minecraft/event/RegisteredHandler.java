package dev.p0ke.wynnmarket.minecraft.event;

import com.github.steveice10.packetlib.packet.Packet;

import java.lang.reflect.Method;

public class RegisteredHandler {

    private Listener parent;
    private Method handler;

    public RegisteredHandler(Listener listener, Method method) {
        this.parent = listener;
        this.handler = method;
    }

    public Listener getParent() {
        return parent;
    }

    public void invoke(Packet p) {
        try {
            handler.invoke(parent, p);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
