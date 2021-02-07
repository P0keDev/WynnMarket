package dev.p0ke.wynnmarket.minecraft.listeners;

import com.github.steveice10.packetlib.event.session.SessionAdapter;

public abstract class Listener extends SessionAdapter {
	
	public abstract void finish();

}
