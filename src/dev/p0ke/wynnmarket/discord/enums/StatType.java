package dev.p0ke.wynnmarket.discord.enums;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public enum StatType {

	ATTACK_SPEED("Attack Speed", "([+-]\\d+) tier Attack Speed", "attack speed", "tiers", "attack tiers"),
	MAIN_ATTACK_RAW("Main Attack Raw", "([+-]\\d+) Main Attack Neutral Damage", "main attack raw", "raw melee", "main attack neutral damage"),
	MAIN_ATTACK_PCT("Main Attack %", "([+-]\\d+)% Main Attack Damage", "main attack %", "main attack pct", "melee %", "melee pct", "main attack damage"),
	SPELL_DAMAGE_RAW("Spell Damage Raw", "([+-]\\d+) Neutral Spell Damage", "spell damage raw", "raw spell", "raw spell damage", "neutral spell damage", "sd raw", "raw sd"),
	SPELL_DAMAGE_PCT("Spell Damage %", "([+-]\\d+)% Spell Damage", "spell damage %", "spell damage pct", "spell damage", "spell", "sd"),
	HEALTH_BONUS("Health", "([+-]\\d+) Health", "health", "health bonus"),
	HEALTH_REGEN_RAW("Health Regen Raw", "([+-]\\d+) Health Regen", "health regen raw", "raw health regen", "raw hpr", "hpr raw"),
	HEALTH_REGEN_PCT("Health Regen %", "([+-]\\d+)% Health Regen", "health regen %", "health regen pct", "health regen", "hpr"),
	LIFE_STEAL("Life Steal", "([+-]\\d+)/4s Life Steal", "life steal", "ls"),
	MANA_REGEN("Mana Regen", "([+-]\\d+)/4s Mana Regen", "mana regen", "mr"),
	MANA_STEAL("Mana Steal", "([+-]\\d+)/4s Mana Steal", "mana steal", "ms"),
	EARTH_DAMAGE("Earth Damage", "([+-]\\d+)% Earth Damage", "earth damage", "earth dam"),
	THUNDER_DAMAGE("Thunder Damage", "([+-]\\d+)% Thunder Damage", "thunder damage", "thunder dam"),
	WATER_DAMAGE("Water Damage", "([+-]\\d+)% Water Damage", "water damage", "water dam"),
	FIRE_DAMAGE("Fire Damage", "([+-]\\d+)% Fire Damage", "fire damage", "fire dam"),
	AIR_DAMAGE("Air Damage", "([+-]\\d+)% Air Damage", "air damage", "air dam"),
	EARTH_DEFENCE("Earth Defence", "([+-]\\d+)% Earth Defence", "earth defence", "earth defense", "earth def"),
	THUNDER_DEFENCE("Thunder Defence", "([+-]\\d+)% Thunder Defence", "thunder defence", "thunder defense", "thunder def"),
	WATER_DEFENCE("Water Defence", "([+-]\\d+)% Water Defence", "water defence", "water defense", "water def"),
	FIRE_DEFENCE("Fire Defence", "([+-]\\d+)% Fire Defence", "fire defence", "fire defense", "fire def"),
	AIR_DEFENCE("Air Defence", "([+-]\\d+)% Air Defence", "air defence", "air defense", "air def"),
	EXPLODING("Exploding", "([+-]\\d+)% Exploding", "exploding", "explode"),
	POISON("Poison", "([+-]\\d+)/3s Poison", "poison", "poison damage"),
	THORNS("Thorns", "([+-]\\d+)% Thorns", "thorns"),
	REFLECTION("Reflection", "([+-]\\d+)% Reflection", "reflection", "refl"),
	WALK_SPEED("Walk Speed", "([+-]\\d+)% Walk Speed", "walk speed", "ws"),
	SPRINT("Sprint", "([+-]\\d+)% Sprint", "sprint"),
	SPRINT_REGEN("Sprint Regen", "([+-]\\d+)% Sprint Regen", "sprint regen"),
	JUMP_HEIGHT("Jump Height", "([+-]\\d+) Jump Height", "jump height", "jh"),
	SOUL_POINT_REGEN("Soul Point Regen", "([+-]\\d+)% Soul Point Regen", "soul point regen", "soul points", "spr"),
	LOOT_BONUS("Loot Bonus", "([+-]\\d+)% Loot Bonus", "loot bonus", "loot", "lb"),
	STEALING("Stealing", "([+-]\\d+)% Stealing", "stealing"),
	XP_BONUS("XP Bonus", "([+-]\\d+)% XP Bonus", "xp bonus", "xp"),
	FIRST_SPELL_RAW("1st Spell Cost Raw", "([+-]\\d+) 1st Spell Cost", "(1st|first) spell( cost)? raw"),
	FIRST_SPELL_PCT("1st Spell Cost %", "([+-]\\d+)% 1st Spell Cost", "(1st|first) spell( cost)? (pct|%)"),
	SECOND_SPELL_RAW("2nd Spell Cost Raw", "([+-]\\d+) 2nd Spell Cost", "(2nd|second) spell( cost)? raw"),
	SECOND_SPELL_PCT("2nd Spell Cost %", "([+-]\\d+)% 2nd Spell Cost", "(2nd|second) spell( cost)? (pct|%)"),
	THIRD_SPELL_RAW("3rd Spell Cost Raw", "([+-]\\d+) 3rd Spell Cost", "(3rd|third) spell( cost)? raw"),
	THIRD_SPELL_PCT("3rd Spell Cost %", "([+-]\\d+)% 3rd Spell Cost", "(3rd|third) spell( cost)? (pct|%)"),
	FOURTH_SPELL_RAW("4th Spell Cost Raw", "([+-]\\d+) 1st Spell Cost", "(4th|fourth) spell( cost)? raw"),
	FOURTH_SPELL_PCT("4th Spell Cost %", "([+-]\\d+)% 1st Spell Cost", "(4th|fourth) spell( cost)? (pct|%)"),
	REROLLS("Rerolls", "(?:Normal|Unique|Rare|Legendary|Fabled|Mythic) Item(?: \\[(\\d+)\\])?", "rerolls", "rrs", "rr"),

	PRICE("Price", "", "price", "emeralds", "ems");

	private String name;
	private Pattern regex;
	private String[] shorthands;

	StatType(String name, String regex, String... shorthands) {
		this.name = name;
		this.regex = Pattern.compile(regex);
		this.shorthands = shorthands;
	}

	public String getName() {
		return name;
	}

	public boolean matches(String line) {
		return regex.matcher(line).matches();
	}

	public int getValue(String line) throws NumberFormatException {
		if (!matches(line)) return 0;
		Matcher m = regex.matcher(line);
		return (m.group(1) == null) ? 0 : Integer.parseInt(m.group(1));
	}

	public static StatType fromShorthand(String name) {
		for (StatType type : StatType.values()) {
			for (String sh : type.shorthands) {
				if (name.matches(sh) || name.matches(sh.replace(" ", "")))
					return type;
			}
		}
		return null;
	}

}
