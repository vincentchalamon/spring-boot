package io.github.vincentchalamon.quinoa.recipe;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class QuantityTest {
    @Test
    void createsValidQuantity() {
        Quantity quantity = new Quantity(1.5, "kg");

        assertThat(quantity.getValue()).isEqualTo(1.5);
        assertThat(quantity.getUnit()).isEqualTo("kg");
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "0 kg",
            "-2 g",
            "NaN l",
            "Infinity g",
    })
    void rejectsIncorrectValue(String input) {
        assertThatThrownBy(() -> Quantity.parse(input))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Quantity must be a positive number.");
    }

    @Test
    void rejectsNullUnit() {
        assertThatThrownBy(() -> new Quantity(2.5, null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Unit must not be empty.");
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "",
            " ",
    })
    void rejectsInvalidUnit(String unit) {
        assertThatThrownBy(() -> new Quantity(2.5, unit))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Unit must not be empty.");
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "",
            " ",
            "250g",
            "250  ",
            "250  g",
    })
    void rejectsInvalidQuantityFormat(String input) {
        assertThatThrownBy(() -> Quantity.parse(input))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Argument is not correctly formatted, expecting: \"<quantity> <unit>\".");
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "2,5 g",
            "abc g",
    })
    void rejectsInvalidQuantityNumber(String input) {
        assertThatThrownBy(() -> Quantity.parse(input))
                .isInstanceOf(IllegalArgumentException.class)
                .hasCauseInstanceOf(NumberFormatException.class);
    }

    @Test
    void quantitiesWithSameValueAreEqual() {
        assertThat(Quantity.parse("2.5 g")).isEqualTo(new Quantity(2.5, "g"));
    }

    @Test
    void quantitiesWithSameValueHaveSameHashCode() {
        assertThat(Quantity.parse("2.5 g")).hasSameHashCodeAs(new Quantity(2.5, "g"));
    }

    @Test
    void toStringReturnsCorrectFormat() {
        assertThat(new Quantity(2.5, "g").toString()).isEqualTo("2.5 g");
    }

    @Test
    void doubleArithmeticIsNotExact() {
        assertThat(new Quantity(0.1 + 0.2, "g")).isEqualTo(new Quantity(0.30000000000000004, "g"));
    }
}
