package dev.p0ke.wynnmarket.minecraft.event;

import com.github.steveice10.packetlib.Session;
import com.github.steveice10.packetlib.event.session.ConnectedEvent;
import com.github.steveice10.packetlib.event.session.DisconnectedEvent;
import com.github.steveice10.packetlib.event.session.PacketReceivedEvent;
import com.github.steveice10.packetlib.event.session.SessionAdapter;
import com.github.steveice10.packetlib.packet.Packet;
import dev.p0ke.wynnmarket.minecraft.event.packet.ConnectPacket;
import dev.p0ke.wynnmarket.minecraft.event.packet.DisconnectPacket;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class EventBus extends SessionAdapter {

	private Session client;
	private Map<Class<?>, ArrayList<RegisteredHandler>> registeredHandlers;
	private List<Listener> registeredListeners;

	public EventBus(Session client) {
		this.client = client;
		registeredHandlers = new HashMap<>();
		registeredListeners = new ArrayList<>();

		this.client.addListener(this);
	}

	public void registerListener(Listener l) {
		if (registeredListeners.contains(l)) return;
		registeredListeners.add(l);

		for (Method m : l.getClass().getMethods()) {
			if (m.getAnnotation(PacketHandler.class) == null) continue;

			Class<?> paramTypes[] = m.getParameterTypes();
			if (paramTypes.length != 1) {
				System.err.println("Method " + m + " has @PacketHandler annotation, but has the wrong number of parameters.");
				continue;
			}

			Class<?> packet = paramTypes[0];
			if (!Packet.class.isAssignableFrom(packet)) {
				System.err.println("Method " + m + " has @PacketHandler annotation, but has a non-packet parameter.");
				continue;
			}

			registerHandler(packet, m, l);
		}
	}

	private void registerHandler(Class<?> packet, Method method, Listener listener) {
		if (!registeredHandlers.containsKey(packet))
			registeredHandlers.put(packet, new ArrayList<>());

		registeredHandlers.get(packet).add(new RegisteredHandler(listener, method));
	}

	public void unregisterListener(Listener l) {
		if (!registeredListeners.remove(l)) return;

		for (List<RegisteredHandler> handlers : registeredHandlers.values()) {
			handlers.removeIf(h -> h.getParent().equals(l));
		}
	}

	public void clearListeners() {
		registeredHandlers.clear();
		registeredListeners.clear();
	}

	/* Event handling */

	@Override
	public void packetReceived(PacketReceivedEvent event) {
		Packet packet = event.getPacket();

		if (registeredHandlers.containsKey(packet.getClass()))
			registeredHandlers.get(packet.getClass()).forEach(h -> h.invoke(packet));
	}

	@Override
	public void connected(ConnectedEvent event) {
		if (registeredHandlers.containsKey(ConnectPacket.class))
			registeredHandlers.get(ConnectPacket.class).forEach(h -> h.invoke(new ConnectPacket(event)));
	}

	@Override
	public void disconnected(DisconnectedEvent event) {
		if (registeredHandlers.containsKey(DisconnectPacket.class))
			registeredHandlers.get(DisconnectPacket.class).forEach(h -> h.invoke(new DisconnectPacket(event)));
	}

}
