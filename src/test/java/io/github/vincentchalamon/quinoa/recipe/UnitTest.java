package io.github.vincentchalamon.quinoa.recipe;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.EnumSource;

class UnitTest {
    @ParameterizedTest
    @CsvSource({"g, GRAM", "ml, MILLILITER", "tsp, TEASPOON", "branch, BRANCH", "leaf, LEAF", "jar, JAR"})
    void findBySymbol(String symbol, Unit expectedUnit) {
        assertThat(Unit.fromSymbol(symbol)).isEqualTo(expectedUnit);
    }

    @ParameterizedTest
    @EnumSource(Unit.class)
    void symbolRoundTrips(Unit unit) {
        assertThat(Unit.fromSymbol(unit.symbol())).isEqualTo(unit);
    }

    @Test
    void rejectsUnknownSymbol() {
        assertThatThrownBy(() -> Unit.fromSymbol("kg"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("kg");
    }

    @Test
    void ignoresSymbolCase() {
        assertThatThrownBy(() -> Unit.fromSymbol("G"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("G");
    }
}
