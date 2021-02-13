package dev.p0ke.wynnmarket.discord.enums;

import java.util.function.IntPredicate;

public enum Comparison implements IntPredicate {

    LESS_THAN("<", a -> a < 0),
    LESS_THAN_OR_EQUAL("<=", a -> a <= 0),
    EQUAL("=", a -> a == 0),
    GREATER_THAN_OR_EQUAL(">=", a -> a >= 0),
    GREATER_THAN(">", a -> a > 0),
    NOT_EQUAL("!=", a -> a != 0);

    public final String symbol;
    private final IntPredicate check;

    Comparison(String symbol, IntPredicate check) {
        this.symbol = symbol;
        this.check = check;
    }

    @Override
    public boolean test(int value) {
        return check.test(value);
    }

    public static Comparison fromSymbol(String symbol) {
    	for (Comparison c : Comparison.values()) {
    		if (c.symbol.equals(symbol)) return c;
    	}
    	return null;
    }

}
