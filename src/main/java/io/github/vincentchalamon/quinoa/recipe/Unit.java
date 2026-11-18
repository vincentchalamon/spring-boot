package io.github.vincentchalamon.quinoa.recipe;

public enum Unit {
    GRAM("g"),
    MILLILITER("ml"),
    TEASPOON("tsp"),
    TABLESPOON("tbsp"),
    PIECE("piece"),
    PINCH("pinch"),
    CLOVE("clove"),
    SPRIG("sprig"),
    BRANCH("branch"),
    LEAF("leaf"),
    SLICE("slice"),
    BUNCH("bunch"),
    SACHET("sachet"),
    JAR("jar");

    private final String symbol;

    Unit(String symbol) {
        this.symbol = symbol;
    }

    public String symbol() {
        return symbol;
    }

    public static Unit fromSymbol(String symbol) {
        for (Unit unit : values()) {
            if (unit.symbol.equals(symbol)) {
                return unit;
            }
        }
        throw new IllegalArgumentException("Unknown unit symbol: " + symbol);
    }
}
