package dev.p0ke.wynnmarket.minecraft.util;

import java.util.HashMap;
import java.util.Map;

public class ActionIdUtil {
	
	private static Map<Integer, Integer> actionIds = new HashMap<>();
	
	public static int getNewID(int window) {
		int id = 1;
		if (actionIds.containsKey(window))
			id = actionIds.get(window);
		
		actionIds.put(window, id + 1);
		
		return id;
	}
	
	public static void reset() {
		actionIds = new HashMap<>();
	}

}
