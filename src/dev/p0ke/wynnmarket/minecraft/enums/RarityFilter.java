package dev.p0ke.wynnmarket.minecraft.enums;

public enum RarityFilter {

	CRAFTED(24, "Crafted Items"),
	NORMAL(30, "Normal Items"),
	UNIQUE(31, "Unique Items"),
	RARE(32, "Rare Items"),
	LEGENDARY(33, "Legendary Items"),
	SET(39, "Set Items"),
	FABLED(40, "Fabled Items"),
	MYTHIC(41, "Mythic Items");

	public int slot;
	public String name;

	RarityFilter(int slot, String name) {
		this.slot = slot;
		this.name = name;
	}

	public static RarityFilter parseString(String s) {
		switch (s.toLowerCase()) {
			case "crafted":
			case "crafteds":
			case "craft":
			case "c":
				return CRAFTED;
			case "normal":
			case "normals":
			case "n":
				return NORMAL;
			case "unique":
			case "uniques":
			case "u":
				return UNIQUE;
			case "rare":
			case "rares":
			case "r":
				return RARE;
			case "legendary":
			case "legendaries":
			case "leg":
			case "l":
				return LEGENDARY;
			case "set":
			case "sets":
			case "s":
				return SET;
			case "fabled":
			case "fableds":
			case "f":
				return FABLED;
			case "mythic":
			case "mythics":
			case "m":
				return MYTHIC;
			default:
				return null;
		}
	}

}
