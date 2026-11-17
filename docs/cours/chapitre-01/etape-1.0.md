# Étape 1.0 - Syntaxe de base

[Chapitre 1](README.md) > Étape 1.0

## En bref

- **Objectif** : écrire de petits programmes Java complets, puis une première classe de valeur, avant d'aborder Maven.
- **Apport au projet** : `Quantity` en classe classique, la première version du fil rouge. Elle entre dans le projet Maven à l'étape 1.1.
- **Durée indicative** : une journée, en deux cycles. Vous pouvez vous arrêter à chaque "point d'arrêt".
- **Exercices** : dans `sandbox/1.0/`, vérifiés par Claude en local, jamais committés.

Avancement :

- [x] A1. Prédire dans jshell
- [x] A2. Écrire `ReadQuantity.java`
- [x] A3. Tester les entrées invalides
- [x] B1. Écrire la classe `Quantity`
- [x] B2. Ajouter `parse`
- [x] B3. Écrire `QuantityDemo.java`
- [x] B4. Ajouter `toString`
- [x] B5. Ajouter `equals`, puis `hashCode`
- [x] B6. Chercher dans la Javadoc
- [x] Validation en session

Astuce : `Ctrl+Shift+O` affiche le plan du document.

Le [mémo Java pour PHP](../memo-java-pour-php.md) résume toute la syntaxe de cette étape. Gardez-le ouvert à côté.

---

## Cycle A - Un programme qui lit ses arguments

### Théorie

#### Ce que vous savez déjà, ce qui change

Les conditions, les boucles et les opérateurs ressemblent beaucoup à PHP. Ce qui change :

- **Types statiques** : chaque variable a un type fixé à la compilation.
- **Primitifs et objets** : `int`, `double`, `boolean` sont des valeurs brutes, jamais `null`. `String` est un objet.
- **Pas de conversion implicite** : `"250"` ne devient jamais `250` tout seul. On convertit explicitement.
- **Pas de "truthy"** : `if (count)` ne compile pas. On écrit `if (count > 0)`.
- **Division entière** : `7 / 2` vaut `3` entre deux `int`.

#### Convertir un texte en nombre

En PHP, `(float) "abc"` donne `0.0`, sans erreur.

En Java, la conversion **échoue** si le texte n'est pas un nombre :

```java
double value = Double.parseDouble("250");    // 250.0
int count = Integer.parseInt("3");           // 3
double bad = Double.parseDouble("abc");      // lance NumberFormatException
```

À retenir :

- le séparateur décimal est toujours le **point**, quelle que soit la langue de la machine ;
- une exception non attrapée arrête le programme.

Pour réagir à l'échec, on l'attrape avec `try` / `catch`, comme en PHP.

#### Exemple 1 : un ticket de caisse

Avant de l'exécuter, lisez-le et **prédisez** la sortie des quatre commandes plus bas.

Fichier `Receipt.java` :

```java
public class Receipt {

    public static void main(String[] args) {
        if (args.length == 0) {
            System.err.println("Usage : java Receipt.java \"<article> <prix>\" ...");
            System.exit(1);
        }

        double total = 0;
        int count = 0;

        for (String line : args) {
            String[] parts = line.split(" ");

            if (parts.length != 2) {
                System.err.println("Ignoré, format attendu \"<article> <prix>\" : " + line);
                continue;
            }

            try {
                double price = Double.parseDouble(parts[1]);
                System.out.println(parts[0] + " : " + price);
                total += price;
                count++;
            } catch (NumberFormatException e) {
                System.err.println("Ignoré, prix invalide : " + parts[1]);
            }
        }

        System.out.println(count + " " + plural(count, "article") + ", total : " + total);
    }

    static String plural(int count, String word) {
        return count > 1 ? word + "s" : word;
    }
}
```

Lecture ligne par ligne :

1. `System.err.println("Usage : ...");`

   Écrit sur la **sortie d'erreur**, pas sur la sortie standard.

   En PHP : `fwrite(STDERR, "Usage : ...\n");`

   Le `\"` échappe un guillemet dans une chaîne, comme en PHP.

2. `System.exit(1);`

   Arrête le programme avec le code de sortie `1` (échec).

   En PHP : `exit(1);`

   Sans cet appel, le programme se termine avec le code `0` (succès), même après un message d'erreur.

3. `double total = 0;` puis `int count = 0;`

   Deux variables typées, initialisées. `0` est un `int`, converti sans perte en `double` : le compilateur l'accepte.

4. `for (String line : args) {`

   Parcourt le tableau des arguments.

   En PHP : `foreach ($argv as $line) {`

   Le type de l'élément (`String`) est déclaré dans la boucle.

5. `String[] parts = line.split(" ");`

   Découpe la chaîne sur chaque espace. Le résultat est un **tableau** de `String`.

   En PHP : `$parts = explode(' ', $line);`

   Piège : l'argument de `split` est une **expression régulière**, pas une simple chaîne. Sans conséquence pour un espace, mais `split(".")` ne fait pas ce que vous croyez.

6. `if (parts.length != 2) {`

   `length` est un champ du tableau, sans parenthèses.

7. `continue;`

   Passe à l'élément suivant, comme en PHP.

8. `try {` ... `} catch (NumberFormatException e) {`

   Si `parseDouble` échoue, l'exécution saute dans le `catch`.

   Même syntaxe qu'en PHP. Le type de l'exception est obligatoire, et la variable n'a pas de `$`.

9. `total += price;` puis `count++;`

   Identiques à PHP.

10. `count + " " + plural(count, "article") + ...`

    Le `+` concatène dès qu'une `String` est impliquée. `count` est converti en texte automatiquement : c'est la seule conversion implicite de Java.

11. `static String plural(int count, String word) {`

    Une méthode utilitaire. Le type de retour (`String`) est **avant** le nom.

    `static` : `main` est `static`, elle ne peut appeler sans objet que des méthodes `static`.

    Pas de visibilité : la méthode est visible dans le **package** seulement (voir le mémo, "Classes et objets").

    En PHP : `private static function plural(int $count, string $word): string`

12. `return count > 1 ? word + "s" : word;`

    L'opérateur ternaire, identique à PHP.

Exécution :

```console
$ java Receipt.java "pain 1.20" "lait 0.95"
pain : 1.2
lait : 0.95
2 articles, total : 2.15
$ java Receipt.java "pain 1.20" "lait" "beurre 2,10" "café 4.5"
pain : 1.2
Ignoré, format attendu "<article> <prix>" : lait
Ignoré, prix invalide : 2,10
café : 4.5
2 articles, total : 5.7
$ java Receipt.java; echo "code de sortie : $?"
Usage : java Receipt.java "<article> <prix>" ...
code de sortie : 1
$ java Receipt.java pain 1.20
Ignoré, format attendu "<article> <prix>" : pain
Ignoré, format attendu "<article> <prix>" : 1.20
0 article, total : 0.0
```

À retenir :

- `1.20` s'affiche `1.2` : un `double` n'a pas de "nombre de décimales". C'est une valeur, pas un texte.
- `2,10` est refusé : la virgule n'est pas un séparateur décimal pour `parseDouble`.
- Dernière commande : sans guillemets, le **shell** découpe `pain 1.20` en deux arguments. Java n'y est pour rien.

### Pièges

- **Les guillemets** : `java Receipt.java pain 1.20` passe deux arguments, `"pain 1.20"` en passe un.
- **Double espace** : `"250  g".split(" ")` donne trois éléments, dont une chaîne vide au milieu.
- **Format et langue** : `"%.2f".formatted(2.5)` donne `"2,50"` sur votre machine (locale française). `parseDouble` attend toujours un point. Un nombre formaté puis relu peut donc échouer.
- **Message sans `System.exit`** : un programme qui affiche une erreur mais se termine normalement renvoie `0`. Un script ou une CI le croira réussi.

### Vérifiez votre compréhension

1. Que vaut `Double.parseDouble("abc")` ?
2. Que contient `args` après `java Receipt.java pain 1.20` ?
3. Pourquoi `plural` doit-elle être `static` ?

---

### Pratique

#### A1. Prédire dans jshell

Durée : 15 min.

Lancez `jshell`. Pour chaque ligne :

1. notez votre prédiction (valeur et type) ;
2. tapez la ligne ;
3. comparez.

```text
"250" + 1
250 + 1 + " g"
"g " + 250 + 1
(int) 2.9
Integer.MAX_VALUE + 1
Double.parseDouble(" 2.5 ")
Double.parseDouble("2,5")
"250 g".split(" ")
"250  g".split(" ")
Double.parseDouble("NaN")
```

Les deux dernières lignes serviront en B6.

`/exit` pour quitter.

#### A2. Écrire `ReadQuantity.java`

Durée : 30 min. Modèle : exemple 1.

Dans `sandbox/1.0/`, écrivez `ReadQuantity.java`. Il lit **un seul** argument, par exemple `"250 g"`, et affiche la valeur et l'unité.

Règles :

1. exactement un argument, sinon message d'usage et code de sortie `1` ;
2. l'argument contient exactement deux parties séparées par un espace ;
3. la valeur est un nombre (`double`) ;
4. la valeur est strictement positive ;
5. chaque erreur affiche un message sur `System.err` et termine avec le code `1`.

Ce qui change par rapport au modèle :

- un seul argument : pas de boucle ;
- une erreur arrête le programme au lieu d'ignorer l'élément ;
- une règle de plus (valeur strictement positive).

```bash
mkdir -p ~/Sites/spring-boot/sandbox/1.0
cd ~/Sites/spring-boot/sandbox/1.0
java ReadQuantity.java "250 g"
java ReadQuantity.java "0.5 l"
```

**Résultat attendu** :

```console
$ java ReadQuantity.java "250 g"
Valeur : 250.0
Unité : g
$ java ReadQuantity.java "0.5 l"
Valeur : 0.5
Unité : l
```

Vos messages peuvent différer. Le comportement, non.

#### A3. Tester les entrées invalides

Durée : 10 min.

Prédisez d'abord le message et le code de sortie de chaque commande. Puis lancez-les :

```bash
java ReadQuantity.java "abc g"; echo "code : $?"
java ReadQuantity.java "0 g"; echo "code : $?"
java ReadQuantity.java "250g"; echo "code : $?"
java ReadQuantity.java 250 g; echo "code : $?"
java ReadQuantity.java; echo "code : $?"
```

**Résultat attendu** : un message d'erreur et `code : 1` pour chacune. La quatrième (sans guillemets) tombe dans la règle 1 : le shell a passé deux arguments.

Si une commande renvoie `code : 0` ou affiche une pile d'exception (`Exception in thread "main"`), une règle manque.

> **Point d'arrêt** : le cycle A est terminé. Gardez `ReadQuantity.java` : son code part dans la classe `Quantity` au cycle B.

---

## Cycle B - Une classe de valeur

### Théorie

#### Ce que vous savez déjà, ce qui change

Vous écrivez des objets valeur en PHP : `final readonly class Price`, validés dans le constructeur. Le principe est identique en Java.

Ce qui change :

- les champs sont **déclarés** avec leur type ; pas de promotion de propriétés dans le constructeur ;
- `final` sur un champ correspond à `readonly` ;
- le constructeur porte le **nom de la classe** ;
- `@Override` correspond à `#[\Override]` de PHP 8.3 : le compilateur vérifie que la méthode redéfinit bien une méthode existante ;
- surtout : **l'égalité ne vient pas toute seule** (voir plus bas).

#### Exceptions : lancer, et la hiérarchie

On lance une exception avec `throw new ...`, comme en PHP.

Pour un argument invalide : `IllegalArgumentException`, l'équivalent d'`InvalidArgumentException`.

Les exceptions forment une hiérarchie de classes. `NumberFormatException` **hérite** d'`IllegalArgumentException` :

- un `catch (IllegalArgumentException e)` attrape aussi les `NumberFormatException` ;
- un seul `catch` suffit donc pour "texte invalide" et "nombre invalide".

Ces deux exceptions sont **non vérifiées** (*unchecked*) : rien à déclarer dans la signature. Les exceptions vérifiées, propres à Java, arrivent à l'étape 1.2.

#### `equals`, `hashCode`, `toString`

En PHP, `$a == $b` compare les propriétés de deux objets de même classe. L'égalité de valeur est gratuite.

En Java, toute classe hérite d'`Object` trois méthodes :

- `equals(Object)` : par défaut, compare l'**identité**, comme `==` ;
- `hashCode()` : par défaut, un nombre lié à l'identité ;
- `toString()` : par défaut, `NomDeClasse@3f99bd52`.

Pour une classe de valeur, on les **redéfinit** toutes les trois.

Le contrat à respecter : deux objets égaux selon `equals` doivent avoir le même `hashCode`.

Pourquoi : un `HashSet` (ou une clé de `HashMap`) cherche d'abord par `hashCode`, puis confirme avec `equals`. Si les `hashCode` diffèrent, `equals` n'est jamais appelé et les deux objets "égaux" sont gardés tous les deux.

À l'étape 1.2, un `record` générera ces trois méthodes. Les écrire une fois à la main montre ce qu'il fait pour vous.

#### Exemple 2 : la classe `Price`

Fichier `Price.java` :

```java
import java.util.Objects;

public final class Price {

    private final double amount;
    private final String currency;

    public Price(double amount, String currency) {
        if (amount < 0) {
            throw new IllegalArgumentException("Montant négatif : " + amount);
        }
        if (currency == null || currency.length() != 3) {
            throw new IllegalArgumentException("Devise sur 3 lettres attendue : " + currency);
        }

        this.amount = amount;
        this.currency = currency;
    }

    public static Price parse(String text) {
        String[] parts = text.split(" ");

        if (parts.length != 2) {
            throw new IllegalArgumentException("Format attendu \"<montant> <devise>\" : " + text);
        }

        double amount = Double.parseDouble(parts[0]);

        return new Price(amount, parts[1]);
    }

    public double getAmount() {
        return amount;
    }

    public String getCurrency() {
        return currency;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof Price price)) {
            return false;
        }

        return Double.compare(amount, price.amount) == 0 && currency.equals(price.currency);
    }

    @Override
    public int hashCode() {
        return Objects.hash(amount, currency);
    }

    @Override
    public String toString() {
        return amount + " " + currency;
    }
}
```

Lecture ligne par ligne :

1. `import java.util.Objects;`

   Comme `use` en PHP. `Objects` sert pour `hashCode`.

   Les classes de `java.lang` (`String`, `Double`, `IllegalArgumentException`...) n'ont pas besoin d'import.

2. `public final class Price {`

   `final` : personne ne peut hériter de `Price`. Comme `final class` en PHP.

3. `private final double amount;`

   Un champ privé, non réaffectable après le constructeur.

   En PHP : `private readonly float $amount;`

4. `public Price(double amount, String currency) {`

   Le constructeur : même nom que la classe, pas de type de retour.

   En PHP : `public function __construct(float $amount, string $currency)`

5. `if (currency == null || currency.length() != 3) {`

   `null` est vérifié en premier. Si `currency` est `null`, `||` n'évalue pas la suite, et `length()` n'est pas appelé sur `null`.

   Pas besoin de vérifier `amount` contre `null` : un `double` ne peut pas l'être.

6. `throw new IllegalArgumentException("...");`

   Refuse l'objet invalide : il n'est jamais créé.

7. `this.amount = amount;`

   `this.amount` est le champ, `amount` le paramètre.

   En PHP : `$this->amount = $amount;`

   Le compilateur vérifie que chaque champ `final` est affecté exactement une fois dans le constructeur.

8. `public static Price parse(String text) {`

   Une **méthode de fabrique** statique : elle construit un `Price` à partir d'un texte.

   En PHP : un "named constructor", comme `DateTimeImmutable::createFromFormat()`.

   Appel : `Price.parse("12.50 EUR")`.

   Elle laisse passer la `NumberFormatException` de `parseDouble` : l'appelant la reçoit.

9. `public double getAmount() {`

   Un accesseur. Le préfixe `get` est la convention JavaBeans, que Spring et Jackson utiliseront.

10. `@Override public boolean equals(Object other) {`

    Le paramètre est un `Object`, pas un `Price` : on redéfinit la méthode héritée d'`Object`, sa signature est imposée.

11. `if (this == other) { return true; }`

    Raccourci : un objet est égal à lui-même.

12. `if (!(other instanceof Price price)) { return false; }`

    `instanceof` avec un nom de variable : si `other` est un `Price`, il est disponible sous le nom `price`, déjà typé.

    Couvre aussi `null` : `null instanceof Price` vaut `false`.

    En PHP : `if (!$other instanceof Price) { return false; }`

13. `Double.compare(amount, price.amount) == 0 && currency.equals(price.currency)`

    Compare les champs un par un :

    - `double` : `Double.compare`, qui gère les cas particuliers (`NaN`, `-0.0`) mieux que `==` ;
    - `String` : `equals`, jamais `==`.

    `price.amount` : un champ `private` est accessible depuis **toute instance de la même classe**. Comme en PHP.

14. `return Objects.hash(amount, currency);`

    Calcule un `hashCode` à partir des **mêmes champs** que `equals`. C'est ce qui garantit le contrat.

15. `return amount + " " + currency;`

    Le texte affiché par `System.out.println(price)` et par la concaténation `"Lu : " + price`.

    En PHP : `__toString()`.

#### Exemple 3 : le programme `PriceDemo`

Fichier `PriceDemo.java`, dans le **même répertoire** que `Price.java` :

```java
import java.util.HashSet;
import java.util.Set;

public class PriceDemo {

    public static void main(String[] args) {
        if (args.length != 1) {
            System.err.println("Usage : java PriceDemo.java \"<montant> <devise>\"");
            System.exit(1);
        }

        try {
            Price price = Price.parse(args[0]);
            System.out.println("Lu : " + price);

            Price copy = new Price(price.getAmount(), price.getCurrency());
            System.out.println("== : " + (price == copy));
            System.out.println("equals : " + price.equals(copy));

            Set<Price> prices = new HashSet<>();
            prices.add(price);
            prices.add(copy);
            System.out.println("Prix distincts : " + prices.size());
        } catch (IllegalArgumentException e) {
            System.err.println("Prix invalide : " + e.getMessage());
            System.exit(1);
        }
    }
}
```

Lecture ligne par ligne (le reste est déjà connu) :

1. `Price copy = new Price(...);`

   Un **second objet**, avec les mêmes valeurs.

2. `(price == copy)`

   Les parenthèses sont nécessaires : sans elles, `+` serait évalué avant `==`.

3. `Set<Price> prices = new HashSet<>();`

   Un ensemble sans doublons. `<Price>` indique le type des éléments (les génériques, étape 1.3). `<>` laisse le compilateur le déduire.

   En PHP, pas d'équivalent direct : un tableau dont les clés seraient les objets.

4. `catch (IllegalArgumentException e)`

   Attrape les erreurs du constructeur **et** celles de `parseDouble` (`NumberFormatException` en hérite).

5. `e.getMessage()`

   En PHP : `$e->getMessage()`.

`java PriceDemo.java` trouve `Price.java` tout seul dans le même répertoire : le lanceur compile les deux fichiers.

Exécution :

```console
$ java PriceDemo.java "12.50 EUR"
Lu : 12.5 EUR
== : false
equals : true
Prix distincts : 1
$ java PriceDemo.java "-3 EUR"
Prix invalide : Montant négatif : -3.0
$ java PriceDemo.java "12,50 EUR"
Prix invalide : For input string: "12,50"
$ java PriceDemo.java "12.50 euros"
Prix invalide : Devise sur 3 lettres attendue : euros
$ java PriceDemo.java "12.50"
Prix invalide : Format attendu "<montant> <devise>" : 12.50
$ java PriceDemo.java "NaN EUR"
Lu : NaN EUR
== : false
equals : true
Prix distincts : 1
```

À retenir :

- `==` : deux objets distincts. `equals` : même valeur. `HashSet` : un seul élément.
- La dernière commande est un **bug** : `NaN` (Not a Number) est accepté comme montant. `NaN < 0` vaut `false`, la validation laisse passer. Vous le corrigerez dans `Quantity` en B6.

#### Exemple 4 : ce qui se passe quand on oublie

Chaque variante modifie une seule chose dans `Price.java`. Sortie réelle de `java PriceDemo.java "12.50 EUR"` :

Sans `toString` :

```console
Lu : Price@c4f81163
```

Sans `hashCode` (mais avec `equals`) :

```console
equals : true
Prix distincts : 2
```

Sans `equals` ni `hashCode` :

```console
equals : false
Prix distincts : 2
```

Avec `amount = amount;` au lieu de `this.amount = amount;` :

```console
$ javac Price.java
Price.java:18: error: variable amount might not have been initialized
    }
    ^
1 error
```

Le paramètre `amount` masque le champ : la ligne affecte le paramètre à lui-même. Le champ `final` n'est jamais affecté, et le compilateur le signale à la fin du constructeur (ligne 18).

En PHP, `$amount = $amount;` passerait sans bruit, et le bug n'apparaîtrait qu'à l'exécution.

#### Lire une Javadoc

La Javadoc est la documentation de référence de chaque classe du JDK. Vous y chercherez seul en B6.

Point d'entrée, Java 25 : https://docs.oracle.com/en/java/javase/25/docs/api/

Exemple, la page de `Integer` : https://docs.oracle.com/en/java/javase/25/docs/api/java.base/java/lang/Integer.html

Structure d'une page de classe, de haut en bas :

1. **En-tête** : le module (`java.base`), le package (`java.lang`), puis la hiérarchie (`Object` > `Number` > `Integer`).
2. **Description** de la classe.
3. **Field Summary** : les constantes (`MAX_VALUE`, `MIN_VALUE`).
4. **Method Summary** : un tableau de toutes les méthodes. Des onglets filtrent : "Static Methods", "Instance Methods".
5. **Method Details** : une fiche par méthode.

Fiche de `parseInt(String s)` :

- **signature** : `public static int parseInt(String s) throws NumberFormatException` ;
- **description** : ce que fait la méthode, les cas particuliers ;
- **Parameters**, **Returns** ;
- **Throws** : les exceptions possibles, et quand.

Dans VSCode, survolez une méthode : la même Javadoc s'affiche dans une infobulle.

Méthode de recherche :

1. ouvrir la page de la classe concernée (`String`, `Double`...) ;
2. dans "Method Summary", lire la colonne de description ;
3. ouvrir la fiche de la méthode candidate, lire "Returns" et "Throws".

En haut à droite, le champ "Search" trouve une classe ou une méthode par son nom.

### Pièges

- **`equals` sans `hashCode`** : le code compile, `equals` répond `true`, mais un `HashSet` garde les doublons (exemple 4).
- **Paramètre qui masque le champ** : `value = value;` au lieu de `this.value = value;` (exemple 4).
- **`equals(Price other)`** : avec un paramètre `Price` au lieu d'`Object`, vous écrivez une **autre** méthode, que `HashSet` n'appelle jamais. `@Override` transforme ce piège en erreur de compilation (`method does not override or implement a method from a supertype`) : mettez-le toujours.
- **`NaN` et `Infinity`** : `parseDouble` les accepte, et ils passent les comparaisons `<` ou `<=` sans déclencher la validation.

### Vérifiez votre compréhension

1. Pourquoi deux `Price` égaux selon `equals` doivent-ils avoir le même `hashCode` ?
2. Pourquoi le paramètre d'`equals` est-il un `Object` ?
3. Pourquoi un seul `catch (IllegalArgumentException e)` suffit-il dans `PriceDemo` ?

---

### Pratique

Tous les fichiers vont dans `sandbox/1.0/`, à côté de `ReadQuantity.java`.

Règles de `Quantity`, pour toutes les sous-étapes :

- une valeur `double`, strictement positive ;
- une unité `String`, obligatoire et non vide (`" "` est refusé) ;
- la classe est immuable.

#### B1. Écrire la classe `Quantity`

Durée : 20 min. Modèle : exemple 2, du début jusqu'aux accesseurs.

Écrivez `Quantity.java` : deux champs (`value`, `unit`), le constructeur avec validation, deux accesseurs.

Ce qui change par rapport au modèle :

- les noms (`value`, `unit`, `getValue`, `getUnit`) ;
- la règle de la valeur : strictement positive ;
- la règle de l'unité : non `null` et non vide. Le mémo, section "Chaînes", donne la méthode qui ignore les espaces.

Erreur volontaire :

1. écrivez `value = value;` au lieu de `this.value = value;` ;
2. lancez `javac Quantity.java` ;
3. lisez le message, puis corrigez.

```bash
javac Quantity.java
```

**Résultat attendu** : après correction, aucune sortie. Un fichier `Quantity.class` apparaît : supprimez-le (`rm Quantity.class`), le lanceur `java` n'en a pas besoin.

#### B2. Ajouter `parse`

Durée : 15 min. Modèle : exemple 2, méthode `parse`.

Ajoutez `public static Quantity parse(String text)` : le découpage de `ReadQuantity.java`, qui lance une `IllegalArgumentException` au lieu d'appeler `System.exit`.

Ce qui change : `parse` ne vérifie que le **format**. La validation de la valeur et de l'unité reste dans le constructeur, et `parse` l'appelle.

Pourquoi : une seule source de vérité pour les règles. `new Quantity(-1, "g")` et `Quantity.parse("-1 g")` doivent échouer de la même façon.

**Résultat attendu** : `javac Quantity.java` ne signale aucune erreur.

#### B3. Écrire `QuantityDemo.java`

Durée : 20 min. Modèle : exemple 3.

Écrivez `QuantityDemo.java` en adaptant `PriceDemo.java`.

```bash
java QuantityDemo.java "250 g"
```

**Résultat attendu** :

```console
$ java QuantityDemo.java "250 g"
Lu : Quantity@6379eb
== : false
equals : false
Quantités distinctes : 2
```

Le nombre après `@` sera différent chez vous. Ces résultats sont normaux à ce stade : B4 et B5 les corrigent.

> **Point d'arrêt** : la classe et le programme fonctionnent. Il reste les trois méthodes d'`Object` et la Javadoc.

#### B4. Ajouter `toString`

Durée : 5 min. Modèle : exemple 2, `toString`.

**Résultat attendu** : `Lu : 250.0 g`.

#### B5. Ajouter `equals`, puis `hashCode`

Durée : 20 min. Modèle : exemple 2, `equals` et `hashCode`.

1. Ajoutez **seulement** `equals`.
2. Prédisez les trois dernières lignes, puis lancez `java QuantityDemo.java "250 g"`.
3. Expliquez le résultat avec la théorie ("`equals`, `hashCode`, `toString`").
4. Ajoutez `hashCode`, relancez.

**Résultat attendu** après l'étape 4 :

```console
$ java QuantityDemo.java "250 g"
Lu : 250.0 g
== : false
equals : true
Quantités distinctes : 1
```

#### B6. Chercher dans la Javadoc

Durée : 20 min. Sans modèle : c'est l'exercice.

Constat :

```bash
java QuantityDemo.java "NaN g"
java QuantityDemo.java "Infinity g"
```

Si votre constructeur teste seulement `value <= 0`, les deux sont acceptés (exemple 3, dernière commande).

Votre besoin : refuser `NaN` et les infinis.

1. Ouvrez la Javadoc de la classe `Double` (méthode de recherche, section "Lire une Javadoc").
2. Cherchez une méthode statique qui répond à ce besoin en un seul appel.
3. Utilisez-la dans le constructeur.

**Résultat attendu** :

```console
$ java QuantityDemo.java "NaN g"
Quantité invalide : Valeur strictement positive attendue : NaN
$ java QuantityDemo.java "Infinity g"
Quantité invalide : Valeur strictement positive attendue : Infinity
```

Votre message peut différer.

Vérifiez enfin toutes les entrées invalides :

```bash
for q in "0 g" "-1 g" "abc g" "250g" "250  g" "250 " "NaN g"; do java QuantityDemo.java "$q"; echo "code : $?"; done
```

**Résultat attendu** : un message d'erreur et `code : 1` pour chacune, jamais de pile d'exception.

> **Point d'arrêt** : la pratique est terminée. Il reste la validation, en session.

---

## Validation

En session, Claude :

- relit vos fichiers de `sandbox/1.0/` ;
- vérifie les points ci-dessous ;
- vous donne un besoin à résoudre seul dans la Javadoc ;
- vous pose les questions "expliquez".

Points vérifiables :

- [x] `ReadQuantity.java` respecte les cinq règles de A2.
- [x] `java QuantityDemo.java "250 g"` donne le résultat attendu de B5.
- [x] Toutes les entrées invalides de B6 donnent un message et le code `1`.
- [x] Les deux champs de `Quantity` sont `private final`.
- [x] La validation n'existe qu'à un seul endroit : le constructeur.
- [x] `@Override` est présent sur les trois méthodes d'`Object`.

Questions "expliquez" :

1. Pourquoi `equals` sans `hashCode` donne-t-il deux éléments dans le `HashSet` ?
2. Pourquoi `parse` appelle-t-elle le constructeur au lieu de valider elle-même ?
3. Pourquoi `Double.parseDouble("2,5")` échoue-t-il, alors que `"%.2f".formatted(2.5)` donne `"2,50"` ?

---

## Documentation

- Les bases du langage (dev.java) : https://dev.java/learn/language-basics/
- Classes et objets (dev.java) : https://dev.java/learn/classes-objects/
- Exceptions (dev.java) : https://dev.java/learn/exceptions/
- Javadoc Java 25 : https://docs.oracle.com/en/java/javase/25/docs/api/
- `Object.equals` et `hashCode`, le contrat : https://docs.oracle.com/en/java/javase/25/docs/api/java.base/java/lang/Object.html
- `Double` : https://docs.oracle.com/en/java/javase/25/docs/api/java.base/java/lang/Double.html
- `String.split` : https://docs.oracle.com/en/java/javase/25/docs/api/java.base/java/lang/String.html#split(java.lang.String)
- `instanceof` avec variable (JEP 394) : https://openjdk.org/jeps/394

## Pour aller plus loin (facultatif)

- Effective Java (Joshua Bloch), 3e édition, items 10 à 12 : `equals`, `hashCode`, `toString`. La référence sur le sujet.
- `Objects.requireNonNull` : la façon idiomatique de refuser `null` en une ligne, dans la Javadoc de `java.util.Objects`.
- Pourquoi `Double.compare` et pas `==` : la Javadoc de `Double.equals` détaille les cas de `NaN` et `-0.0`.
