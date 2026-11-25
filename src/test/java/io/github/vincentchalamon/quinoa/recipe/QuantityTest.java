package io.github.vincentchalamon.quinoa.recipe;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.math.BigDecimal;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

class QuantityTest {
    @Test
    void createsValidQuantity() {
        Quantity quantity = new Quantity(new BigDecimal("1.5"), Unit.GRAM);

        assertThat(quantity.value()).isEqualTo(new BigDecimal("1.5"));
        assertThat(quantity.unit()).isEqualTo(Unit.GRAM);
    }

    @ParameterizedTest
    @CsvSource({
        "0 g, 0",
        "-2 g, -2",
    })
    void rejectsNonPositiveValue(String input, String expectedValue) {
        assertThatThrownBy(() -> Quantity.parse(input))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Quantity must be a positive number, got \"%s\".".formatted(expectedValue));
    }

    @Test
    void rejectsNullUnit() {
        assertThatThrownBy(() -> new Quantity(new BigDecimal("2.5"), null))
                .isInstanceOf(NullPointerException.class)
                .hasMessage("Unit cannot be null.");
    }

    @Test
    void rejectsNullQuantity() {
        assertThatThrownBy(() -> new Quantity(null, Unit.GRAM))
                .isInstanceOf(NullPointerException.class)
                .hasMessage("Value cannot be null.");
    }

    @Test
    void rejectsInvalidUnit() {
        assertThatThrownBy(() -> Quantity.parse("2.5 kg")).isInstanceOf(IllegalArgumentException.class);
    }

    @ParameterizedTest
    @ValueSource(
            strings = {
                "", " ", "250g", "250  ", "250  g",
            })
    void rejectsInvalidInputFormat(String input) {
        assertThatThrownBy(() -> Quantity.parse(input))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Argument is not correctly formatted, expecting: \"<quantity> <unit>\", got \"%s\"."
                        .formatted(input));
    }

    @ParameterizedTest
    @ValueSource(
            strings = {
                "2,5 g",
                "abc g",
                "NaN l",
                "Infinity g",
            })
    void rejectsInvalidQuantityNumber(String input) {
        assertThatThrownBy(() -> Quantity.parse(input)).isInstanceOf(NumberFormatException.class);
    }

    @Test
    void quantitiesWithSameValueAreEqual() {
        assertThat(Quantity.parse("2 g")).isEqualTo(new Quantity(new BigDecimal("2.0"), Unit.GRAM));
    }

    @Test
    void quantitiesWithSameValueHaveSameHashCode() {
        assertThat(Quantity.parse("2 g")).hasSameHashCodeAs(new Quantity(new BigDecimal("2.0"), Unit.GRAM));
    }

    @Test
    void toStringReturnsCorrectFormat() {
        assertThat(new Quantity(new BigDecimal("100"), Unit.GRAM)).hasToString("100 g");
    }

    @Test
    void bigDecimalArithmeticIsExact() {
        Quantity sum = new Quantity(new BigDecimal("0.1"), Unit.GRAM);
        sum = new Quantity(sum.value().add(new BigDecimal("0.2")), sum.unit());

        assertThat(sum).hasToString("0.3 g");
        assertThat(sum).isEqualTo(new Quantity(new BigDecimal("0.3"), Unit.GRAM));
    }
}
