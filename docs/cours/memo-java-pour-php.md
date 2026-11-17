# Mémo : Java pour un développeur PHP

Aide-mémoire de syntaxe, complété au fil des chapitres. Chaque section met en regard PHP et Java ; les exemples sont génériques (sans lien avec le code du projet).

Sections : [Fichier et programme](#fichier-et-programme) - [Variables et types](#variables-et-types) - [Chaînes](#chaînes) - [Opérateurs et comparaisons](#opérateurs-et-comparaisons) - [Contrôle de flux](#contrôle-de-flux) - [Tableaux](#tableaux) - [Méthodes](#méthodes) - [Classes et objets](#classes-et-objets) - [Exceptions](#exceptions) - [Égalité et affichage d'un objet](#égalité-et-affichage-dun-objet)

Référence officielle : [Learn Java (dev.java)](https://dev.java/learn/) et [The Java Tutorials](https://docs.oracle.com/javase/tutorial/).

---

## Fichier et programme

| PHP | Java |
|---|---|
| `<?php` en tête de fichier | rien |
| `namespace App\Weather;` | `package com.example.weather;` (en minuscules, reflète l'arborescence des répertoires) |
| `use App\Weather\City;` | `import com.example.weather.City;` |
| plusieurs classes par fichier possibles | une classe `public` par fichier, nommée comme le fichier |
| code au niveau racine du script | méthode `main` (voir ci-dessous) |
| `php script.php` | `java Script.java` (direct) ou `javac` puis `java` (voir [étape 0.1](chapitre-00/etape-0.1.md), cycle B) |
| `fwrite(STDERR, "...")` | `System.err.println("...")` |
| `exit(1);` | `System.exit(1);` |

```java
package com.example.weather;

import java.util.List;

public class Weather {

    public static void main(String[] args) {
        System.out.println("Bonjour");
    }
}
```

Forme compacte (Java 25, scripts uniquement, sans `package`) :

```java
void main() {
    IO.println("Bonjour");
}
```

## Variables et types

Java est **statiquement typé** : chaque variable a un type fixé à la compilation.

```java
int count = 3;                 // type avant le nom, pas de $
String city = "Lille";
var temperature = 21.5;        // type déduit (double) ; reste un double pour toujours
final int max = 10;            // non réaffectable (proche d'une constante locale)
```

| PHP | Java | Remarque |
|---|---|---|
| `int` | `int` (32 bits), `long` (64 bits, suffixe `L` : `3L`) | `int` de PHP = 64 bits = `long` Java |
| `float` | `double` | `float` Java existe mais n'est presque jamais utilisé |
| `bool` | `boolean` | `true` / `false` |
| `string` | `String` (objet, majuscule) | |
| caractère = chaîne d'un caractère | `char` : `'a'` (apostrophes) | `'abc'` ne compile pas |
| `null` | `null` | uniquement pour les objets : un `int` ne peut pas être `null` |
| `?int` | `Integer` (type "enveloppe", *wrapper*) | voir chapitre 1 |
| `array` | tableaux, `List`, `Map`, `Set` | voir [Tableaux](#tableaux) et chapitre 1 |

Types **primitifs** (`int`, `long`, `double`, `boolean`, `char`...) : valeur brute, minuscule, jamais `null`. Tout le reste est **objet** (type en majuscule) et peut être `null`.

## Chaînes

```java
String name = "Vincent";
String greeting = "Bonjour " + name + " !";      // concaténation : +
String formatted = "%s a %d ans".formatted(name, 42);   // équivalent de sprintf
int size = name.length();                        // méthode : parenthèses
String upper = name.toUpperCase();
boolean same = name.equals("Vincent");           // comparaison de contenu : equals, jamais ==
String text = """
        Bloc de texte
        sur plusieurs lignes
        """;                                     // équivalent du heredoc (Java 15+)
```

| PHP | Java |
|---|---|
| `"Bonjour $name"` (interpolation) | pas d'interpolation : `+` ou `.formatted(...)` |
| `strlen($s)` | `s.length()` |
| `strtoupper($s)` | `s.toUpperCase()` |
| `str_contains($s, 'x')` | `s.contains("x")` |
| `$a === $b` | `a.equals(b)` |
| `$s === ''` | `s.isEmpty()` (ou `s.isBlank()` pour ignorer les espaces) |
| `explode(' ', $s)` | `s.split(" ")` : renvoie un `String[]` ; l'argument est une **expression régulière** |
| `(float) $s` | `Double.parseDouble(s)` : lance `NumberFormatException` si `s` n'est pas un nombre ; point décimal uniquement |
| `(int) $s` | `Integer.parseInt(s)` : idem |

## Opérateurs et comparaisons

| PHP | Java | Remarque |
|---|---|---|
| `$a . $b` | `a + b` | sur des `String` |
| `$a == $b` (avec conversion) | n'existe pas | Java ne convertit jamais implicitement `"1"` en `1` |
| `$a === $b` sur des scalaires | `a == b` sur des primitifs | `int`, `double`, `boolean`, `char` |
| `$a == $b` sur des objets (mêmes valeurs) | `a.equals(b)` | |
| `$a === $b` sur des objets (même instance) | `a == b` sur des objets | **piège** : `==` sur deux `String` compare les instances |
| `intdiv(7, 2)` | `7 / 2` | entre deux entiers, `/` est une division **entière** (`3`) |
| `7 / 2` | `7 / 2.0` | `3.5` |
| `$x ?? 'défaut'` | pas d'opérateur ; voir `Optional` (chapitre 1) ou `Objects.requireNonNullElse(x, "défaut")` | |
| `$c ? 'a' : 'b'` | `c ? "a" : "b"` | identique |
| `&&`, `\|\|`, `!` | `&&`, `\|\|`, `!` | identique ; pas de `and` / `or` |

## Contrôle de flux

```java
if (count > 10) {
    System.out.println("beaucoup");
} else if (count > 0) {
    System.out.println("un peu");
} else {
    System.out.println("aucun");
}

for (int i = 0; i < 3; i++) {             // for classique, comme en PHP
    System.out.println(i);
}

for (String city : cities) {              // foreach ($cities as $city)
    System.out.println(city);
}

while (count > 0) {
    count--;
}

String label = switch (day) {             // switch expression, proche de match() en PHP 8
    case "samedi", "dimanche" -> "week-end";
    default -> "semaine";
};
```

La condition d'un `if` doit être un `boolean` : `if (count)` ou `if (name)` ne compilent pas (pas de "truthy" comme en PHP). Écrire `if (count > 0)`, `if (name != null && !name.isEmpty())`.

## Tableaux

Taille fixe, rarement utilisés directement (on préfère `List`, chapitre 1), mais `main` en reçoit un.

```java
String[] cities = {"Lille", "Lyon"};
String first = cities[0];                 // indice hors limites : ArrayIndexOutOfBoundsException
int size = cities.length;                 // champ, sans parenthèses
```

| PHP | Java |
|---|---|
| `count($tab)` | `tab.length` (tableau), `list.size()` (collection) |
| `$tab[] = 'x';` | impossible sur un tableau (taille fixe) ; `list.add("x")` sur une `List` |
| `$argv[0]` = nom du script | `args[0]` = **premier argument** |

## Méthodes

```java
public class Calculator {

    public int add(int a, int b) {        // visibilité, type de retour, nom, paramètres typés
        return a + b;
    }

    public static double half(double value) {   // static : appel sans instance
        return value / 2;
    }

    public void log(String message) {     // void : ne renvoie rien
        System.out.println(message);
    }
}
```

| PHP | Java |
|---|---|
| `public function add(int $a, int $b): int` | `public int add(int a, int b)` (le type de retour est **avant** le nom) |
| `$calc->add(1, 2)` | `calc.add(1, 2)` |
| `Calculator::half(3)` | `Calculator.half(3)` |
| paramètre par défaut `int $b = 0` | n'existe pas ; écrire une **surcharge** : deux méthodes de même nom, paramètres différents |
| arguments nommés | n'existent pas |

## Classes et objets

```java
public class City {

    private final String name;            // propriété (champ) ; final = non réaffectable après construction
    private int population;

    public City(String name, int population) {   // constructeur : même nom que la classe, pas de type de retour
        this.name = name;
        this.population = population;
    }

    public String getName() {
        return name;                       // this. facultatif quand il n'y a pas d'ambiguïté
    }
}

City lille = new City("Lille", 236_000);   // _ autorisé dans les nombres pour la lisibilité
String n = lille.getName();
```

| PHP | Java |
|---|---|
| `__construct(...)` | méthode du même nom que la classe |
| promotion de propriétés `public function __construct(private string $name)` | n'existe pas sur une classe ; voir `record` (chapitre 1, étape 1.2) |
| `$this->name` | `this.name` |
| `readonly` | `final` sur le champ |
| `extends`, `implements` | identiques |
| `abstract`, `interface` | identiques |
| `final class` | `final class` |
| visibilité par défaut : `public` | sans mot-clé : visibilité *package* (accessible dans le même package uniquement) |

## Exceptions

```java
try {
    int value = Integer.parseInt("abc");          // équivalent de (int) "abc", mais qui échoue
} catch (NumberFormatException e) {
    System.out.println("Pas un nombre : " + e.getMessage());
} finally {
    System.out.println("toujours exécuté");
}

throw new IllegalArgumentException("La quantité doit être positive");
```

`NumberFormatException` hérite d'`IllegalArgumentException` : un `catch (IllegalArgumentException e)` attrape les deux.

Proche de PHP. Différence majeure : les exceptions **vérifiées** (*checked*, dérivées d'`Exception` hors `RuntimeException`) doivent être soit attrapées, soit déclarées dans la signature (`throws IOException`), sinon le code ne compile pas. Détails au chapitre 1.

## Égalité et affichage d'un objet

En PHP, `==` compare les propriétés de deux objets. En Java, il faut redéfinir les trois méthodes héritées d'`Object` (voir [étape 1.0](chapitre-01/etape-1.0.md), cycle B).

```java
@Override                                   // #[\Override] en PHP 8.3
public boolean equals(Object other) {       // paramètre Object, signature imposée
    if (this == other) {
        return true;
    }
    if (!(other instanceof City city)) {    // instanceof avec variable : city est déjà typée
        return false;
    }

    return population == city.population && name.equals(city.name);
}

@Override
public int hashCode() {
    return Objects.hash(name, population);  // mêmes champs que equals (import java.util.Objects)
}

@Override
public String toString() {                  // __toString()
    return name + " (" + population + ")";
}
```

| Méthode | Par défaut | Règle |
|---|---|---|
| `equals` | identité (`==`) | comparer les champs ; `Double.compare(a, b) == 0` pour un `double` |
| `hashCode` | lié à l'identité | égaux selon `equals` : même `hashCode` |
| `toString` | `City@6379eb` | texte lisible |

À l'étape 1.2, un `record` génère ces trois méthodes.
