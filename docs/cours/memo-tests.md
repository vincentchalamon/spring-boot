# Mémo : JUnit et AssertJ pour un développeur PHPUnit

Aide-mémoire, complété au fil des chapitres. Détails et exemples : [étape 1.1](chapitre-01/etape-1.1.md).

Références officielles : https://docs.junit.org/current/user-guide/ et https://assertj.github.io/doc/

---

## Structure d'un test

```java
package com.example.shop.price;                      // même package que la classe testée

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;

class PriceTest {                                    // pas de public, pas d'extends ; nom en ...Test

    @Test
    void createsValidPrice() {                       // nom = comportement attendu
        Price price = new Price(12.5, "EUR");

        assertThat(price.getAmount()).isEqualTo(12.5);
    }
}
```

## Correspondances

| PHPUnit | JUnit + AssertJ |
|---|---|
| `extends TestCase` | rien |
| `#[Test]` ou `testXxx()` | `@Test` |
| `assertSame($expected, $actual)` | `assertThat(actual).isEqualTo(expected)` |
| `assertEquals` sur objets | `isEqualTo` (appelle `equals`) |
| `assertNotEquals` | `isNotEqualTo` |
| `assertTrue($x)` | `assertThat(x).isTrue()` |
| `assertStringContainsString` | `assertThat(s).contains("...")` |
| `expectException(Foo::class)` | `assertThatThrownBy(() -> ...).isInstanceOf(Foo.class)` |
| `expectExceptionMessage` | `.hasMessage("...")` ou `.hasMessageContaining("...")` |
| `phpunit.xml` | rien : Surefire trouve les classes `*Test` |

## Exceptions

```java
assertThatThrownBy(() -> new Price(-1, "EUR"))      // lambda : exécutée par AssertJ
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("Montant négatif");
```

Sans `() ->`, l'exception part avant l'assertion.

## Lancer les tests

- tous : `./mvnw test` ;
- VSCode : triangle vert dans la marge, ou vue "Testing" (icône de fiole).
