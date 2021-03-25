package dev.p0ke.wynnmarket.minecraft.event;

import java.lang.reflect.Method;

import com.github.steveice10.packetlib.packet.Packet;

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
