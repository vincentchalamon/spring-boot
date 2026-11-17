# Étape 1.1 - Projet Maven à la main

[Chapitre 1](README.md) > Étape 1.1

## En bref

- **Objectif** : transformer `Quantity` en premier code du projet : un projet Maven, compilé et testé par une seule commande.
- **Apport au projet** : `pom.xml`, Maven Wrapper, `Quantity` dans son package, et ses premiers tests. C'est la première PR de code Java.
- **Durée indicative** : une journée, en deux cycles. Vous pouvez vous arrêter à chaque "point d'arrêt".
- **Branche** : `ch01/01-maven`.

Avancement :

- [x] A1. Créer la branche
- [x] A2. Générer le Maven Wrapper
- [x] A3. Écrire le `pom.xml`
- [x] A4. Déplacer `Quantity` dans son package
- [x] A5. Compiler avec `./mvnw compile`
- [x] B1. Écrire le premier test
- [x] B2. Mettre un test au mauvais endroit
- [x] B3. Tester les règles de `Quantity`
- [x] B4. Révéler le problème des `double`
- [x] B5. Lancer les tests depuis VSCode
- [x] B6. Committer et ouvrir la PR
- [x] Validation en session

Astuce : `Ctrl+Shift+O` affiche le plan du document.

Nouveaux mémos : [Maven](../memo-maven.md) et [tests](../memo-tests.md). Gardez-les ouverts à côté.

---

## Cycle A - Le projet Maven

### Théorie

#### Ce que vous savez déjà, ce qui change

Maven tient le rôle de Composer : il déclare les dépendances, les télécharge et fixe la structure du projet.

Correspondances :

- `composer.json` : `pom.xml` (Project Object Model) ;
- Packagist : **Maven Central** ;
- `vendor/` : `~/.m2/repository/`, **partagé** par tous les projets de la machine ;
- `require-dev` : une dépendance de **scope** `test` ;
- autoload PSR-4 : l'arborescence `src/main/java/` plus la déclaration `package`.

Ce qui change :

- **Maven construit aussi le projet** : il compile, lance les tests et produit un `.jar`. En PHP, il faudrait Composer, PHPUnit et un Makefile réunis.
- **Pas de `composer.lock`** : le `pom.xml` déclare des versions **exactes**, sans plage `^1.2`. Le `pom.xml` sert donc aussi de fichier de verrouillage.
- **Tout passe par le XML** : pas de commande `composer require`, on édite le `pom.xml` à la main.

#### Les coordonnées

Chaque projet et chaque dépendance est identifié par trois coordonnées, souvent abrégées en **GAV** :

- `groupId` : l'organisation, en nom de domaine inversé. Exemple : `org.junit.jupiter`. C'est le `vendor` de `vendor/package`.
- `artifactId` : le nom du projet. Exemple : `junit-jupiter`.
- `version` : `6.1.3`. Le suffixe `-SNAPSHOT` signifie "en développement, pas encore publié".

#### L'arborescence standard

Maven impose une arborescence. Rien à configurer si on la respecte :

```text
pom.xml
src/
  main/java/      code de production
  test/java/      code de test
target/           tout ce que Maven produit (ignoré par git)
```

Dans `src/main/java/`, chaque répertoire correspond à un segment du **package** :

- fichier `src/main/java/com/example/shop/price/Price.java` ;
- première ligne du fichier : `package com.example.shop.price;` ;
- nom complet de la classe : `com.example.shop.price.Price`.

C'est le principe de PSR-4 : namespace = chemin.

Conventions pour les packages :

- tout en minuscules, sans tiret ;
- le préfixe reprend le `groupId` ;
- dans ce projet, un package par fonctionnalité (`recipe`, plus tard `user`...), pas par couche.

#### Le cycle de vie

Maven enchaîne des **phases**, dans un ordre fixe. Les principales :

1. `compile` : compile `src/main/java/` dans `target/classes/` ;
2. `test` : compile `src/test/java/`, puis lance les tests ;
3. `package` : produit le `.jar` dans `target/` ;
4. `verify` : lance les vérifications (qualité, couverture : chapitre 2).

Demander une phase exécute **toutes les précédentes**. `./mvnw test` compile d'abord, `./mvnw verify` fait tout.

`./mvnw verify` est la commande de référence : celle que lancera la CI.

`clean` est à part : il supprime `target/`. `./mvnw clean verify` reconstruit tout depuis zéro.

Chaque phase est réalisée par un **plugin** : `maven-compiler-plugin` pour `compile`, `maven-surefire-plugin` pour `test`... C'est pourquoi les logs affichent `compiler:3.16.0:compile`.

#### Le Maven Wrapper

Problème : sur deux machines, deux versions de Maven peuvent produire deux résultats.

Le **Maven Wrapper** fixe la version de Maven dans le dépôt :

- `mvnw` (et `mvnw.cmd` pour Windows) : un script qui remplace la commande `mvn` ;
- `.mvn/wrapper/maven-wrapper.properties` : la version de Maven à utiliser.

Au premier lancement, `./mvnw` télécharge cette version dans `~/.m2/wrapper/`. Ensuite, il l'utilise directement.

Équivalent : un `composer.phar` versionné dans le dépôt, ou `.sdkmanrc` pour le JDK.

Ces trois fichiers sont **committés**. On tape toujours `./mvnw`, jamais `mvn`.

Pour générer le Wrapper, il faut une fois un Maven installé. Ensuite, ce Maven ne sert plus.

#### Exemple 1 : le `pom.xml` d'un projet `shop`

Ce projet reprend la classe `Price` de l'étape 1.0.

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 https://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>

    <groupId>com.example</groupId>
    <artifactId>shop</artifactId>
    <version>0.1.0-SNAPSHOT</version>

    <properties>
        <maven.compiler.release>25</maven.compiler.release>
        <project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>
    </properties>

    <dependencyManagement>
        <dependencies>
            <dependency>
                <groupId>org.junit</groupId>
                <artifactId>junit-bom</artifactId>
                <version>6.1.3</version>
                <type>pom</type>
                <scope>import</scope>
            </dependency>
        </dependencies>
    </dependencyManagement>

    <dependencies>
        <dependency>
            <groupId>org.junit.jupiter</groupId>
            <artifactId>junit-jupiter</artifactId>
            <scope>test</scope>
        </dependency>
        <dependency>
            <groupId>org.assertj</groupId>
            <artifactId>assertj-core</artifactId>
            <version>3.27.7</version>
            <scope>test</scope>
        </dependency>
    </dependencies>

    <build>
        <pluginManagement>
            <plugins>
                <plugin>
                    <groupId>org.apache.maven.plugins</groupId>
                    <artifactId>maven-compiler-plugin</artifactId>
                    <version>3.16.0</version>
                </plugin>
                <plugin>
                    <groupId>org.apache.maven.plugins</groupId>
                    <artifactId>maven-surefire-plugin</artifactId>
                    <version>3.6.1</version>
                </plugin>
            </plugins>
        </pluginManagement>
    </build>
</project>
```

Lecture bloc par bloc :

1. `<project xmlns=...>` et `<modelVersion>4.0.0</modelVersion>`

   L'en-tête, toujours le même. Le schéma XSD permet à VSCode (extension XML) de compléter et de valider le fichier.

2. `<groupId>`, `<artifactId>`, `<version>`

   Les coordonnées du projet lui-même.

   En PHP : `"name": "example/shop"` et `"version"` dans `composer.json`.

3. `<maven.compiler.release>25</maven.compiler.release>`

   La version de Java ciblée. Le compilateur vérifie aussi que le code n'utilise aucune API absente de Java 25.

   En PHP : `"require": {"php": ">=8.3"}`, mais vérifié à la compilation.

4. `<project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>`

   L'encodage des fichiers source. Sans lui, Maven affiche un avertissement et dépend de la machine.

5. `<dependencyManagement>` avec `junit-bom`

   Un **BOM** (Bill Of Materials) : une liste de versions cohérentes pour une famille d'artefacts. On l'**importe** (`<type>pom</type>`, `<scope>import</scope>`), puis on déclare les artefacts JUnit **sans** version.

   `dependencyManagement` ne télécharge rien : il fixe seulement des versions.

   Spring Boot fournira un BOM pour toutes ses dépendances (chapitre 3).

6. `<dependencies>`

   Les dépendances réellement utilisées.

   - `junit-jupiter` : JUnit 6. Pas de `<version>` : le BOM la fournit.
   - `assertj-core` : AssertJ, des assertions lisibles. Pas de BOM ici, donc une version.
   - `<scope>test</scope>` : disponible pour `src/test/java/` uniquement. Absent du `.jar` final.

   En PHP : `"require-dev": {"phpunit/phpunit": "11.5.0"}`.

7. `<pluginManagement>`

   Fixe la version des plugins. Sans lui, Maven utilise des versions par défaut, liées à la version de Maven et souvent anciennes.

   On ne déclare que les plugins dont on a besoin récents : le compilateur (Java 25) et Surefire (JUnit 6). Les autres (`clean`, `jar`...) gardent leur version par défaut.

#### Exemple 2 : compiler

Arborescence avant compilation :

```text
shop/
  .mvn/wrapper/maven-wrapper.properties
  mvnw
  mvnw.cmd
  pom.xml
  src/main/java/com/example/shop/price/Price.java
```

`Price.java` est celui de l'étape 1.0, avec une ligne de plus en tête :

```java
package com.example.shop.price;

import java.util.Objects;

public final class Price {
```

Compilation (chemins raccourcis) :

```console
$ ./mvnw compile
[INFO] Scanning for projects...
[INFO]
[INFO] --------------------------< com.example:shop >--------------------------
[INFO] Building shop 0.1.0-SNAPSHOT
[INFO]   from pom.xml
[INFO] --------------------------------[ jar ]---------------------------------
[INFO]
[INFO] --- resources:3.4.0:resources (default-resources) @ shop ---
[INFO] skip non existing resourceDirectory .../shop/src/main/resources
[INFO]
[INFO] --- compiler:3.16.0:compile (default-compile) @ shop ---
[INFO] Recompiling the module because of changed source code.
[INFO] Compiling 1 source file with javac [debug release 25] to target/classes
[INFO] ------------------------------------------------------------------------
[INFO] BUILD SUCCESS
[INFO] ------------------------------------------------------------------------
$ find target/classes
target/classes
target/classes/com
target/classes/com/example
target/classes/com/example/shop
target/classes/com/example/shop/price
target/classes/com/example/shop/price/Price.class
```

À retenir :

- `--- compiler:3.16.0:compile` : le plugin, sa version, son but (`goal`) ;
- `release 25` : la propriété `maven.compiler.release` est bien prise en compte ;
- `target/classes/` reproduit l'arborescence des packages.

Le **tout premier** lancement télécharge Maven, puis chaque plugin : plusieurs minutes et des centaines de lignes `Downloading from central`. Les lancements suivants prennent quelques secondes. L'option `-ntp` (`--no-transfer-progress`) masque ces lignes.

### Pièges

- **`mvn` au lieu de `./mvnw`** : vous utilisez le Maven de votre machine, pas celui du projet.
- **Une plage de versions** (`[1.0,2.0)`) : Maven le permet, mais personne ne le fait. Version exacte, toujours.
- **Package et répertoire différents** : `javac` ne vérifie pas que la ligne `package` correspond au répertoire. Le code compile, mais la classe se retrouve ailleurs dans `target/classes/`, et VSCode signale une erreur. Gardez-les identiques.
- **`dependencyManagement` seul** : déclarer JUnit dans `dependencyManagement` ne l'ajoute pas au projet. Il faut aussi l'entrée dans `dependencies`.
- **`Non-readable POM ... input contained no data`** : le `pom.xml` est **vide** sur le disque, le plus souvent parce qu'il n'est pas encore enregistré dans VSCode (un point sur l'onglet). Ce n'est pas un problème de droits : dans ce cas, le message finit par `Permission non accordée`.
- **VSCode n'importe pas le projet Maven** : si le dossier a été ouvert avant l'existence du `pom.xml`, le serveur Java peut le garder en "projet invisible". Symptômes : pas de triangle de test dans la marge, `is not on its project's build path` dans les journaux. Remède : `Ctrl+Shift+P`, "Java: Clean Java Language Server Workspace", puis si besoin "Developer: Reload Window".

### Vérifiez votre compréhension

1. Que fait `./mvnw verify` avant de lancer les vérifications ?
2. Pourquoi `junit-jupiter` n'a-t-il pas de `<version>` dans `dependencies` ?
3. Où sont stockées les dépendances téléchargées ?

---

### Pratique

#### A1. Créer la branche

Durée : 2 min.

```bash
cd ~/Sites/spring-boot
git switch main
git pull
git switch -c ch01/01-maven
```

**Résultat attendu** : `git status` affiche `Sur la branche ch01/01-maven`.

#### A2. Générer le Maven Wrapper

Durée : 10 min.

1. Installez Maven, une seule fois, via SDKMAN :

   ```bash
   sdk install maven 3.9.16
   ```

2. À la racine du dépôt, générez le Wrapper :

   ```bash
   mvn wrapper:wrapper -Dmaven=3.9.16
   ```

3. Vérifiez la version utilisée par le Wrapper :

   ```bash
   ./mvnw -v
   ```

**Résultat attendu** :

```console
$ mvn wrapper:wrapper -Dmaven=3.9.16
[INFO] Scanning for projects...
[INFO] ------------------< org.apache.maven:standalone-pom >-------------------
[INFO] Building Maven Stub Project (No POM) 1
[INFO] --------------------------------[ pom ]---------------------------------
[INFO] --- wrapper:3.3.4:wrapper (default-cli) @ standalone-pom ---
[INFO] Unpacked only-script type wrapper distribution org.apache.maven.wrapper:maven-wrapper-distribution:zip:only-script:3.3.4
[INFO] Configuring .mvn/wrapper/maven-wrapper.properties to use Maven 3.9.16 and download from https://repo.maven.apache.org/maven2
[INFO] ------------------------------------------------------------------------
[INFO] BUILD SUCCESS
[INFO] ------------------------------------------------------------------------
$ ./mvnw -v
Apache Maven 3.9.16 (2bdd9fddda4b155ebf8000e807eb73fd829a51d5)
Maven home: /home/vincent/.m2/wrapper/dists/apache-maven-3.9.16/56ba1f9f
Java version: 25.0.5, vendor: Eclipse Adoptium, runtime: /home/vincent/.sdkman/candidates/java/25.0.5.0+8-tem
Default locale: fr_FR, platform encoding: UTF-8
OS name: "linux", version: "7.2.15-76070215-generic", arch: "amd64", family: "unix"
```

Trois fichiers sont apparus : `mvnw`, `mvnw.cmd`, `.mvn/wrapper/maven-wrapper.properties`.

Le `Maven home` est sous `~/.m2/wrapper/` : c'est bien le Maven du Wrapper, pas celui de SDKMAN.

#### A3. Écrire le `pom.xml`

Durée : 20 min. Modèle : exemple 1.

À la racine du dépôt, écrivez `pom.xml` en adaptant l'exemple.

Ce qui change : les coordonnées. Recommandation :

- `groupId` : `io.github.vincentchalamon` (un domaine que vous contrôlez, via votre compte GitHub) ;
- `artifactId` : `obi-wan-quinoa-api` (le nom du `README.md`) ;
- `version` : `0.1.0-SNAPSHOT`.

Vous pouvez en choisir d'autres. Le `groupId` sert de préfixe au package en A4.

Le reste (propriétés, dépendances, plugins) est identique à l'exemple.

**Résultat attendu** : `./mvnw validate` affiche `BUILD SUCCESS`.

#### A4. Déplacer `Quantity` dans son package

Durée : 10 min. Modèle : exemple 2.

Package recommandé : `io.github.vincentchalamon.quinoa.recipe`.

- `io.github.vincentchalamon` : le `groupId` ;
- `quinoa` : l'application, sans tiret ;
- `recipe` : la fonctionnalité.

```bash
mkdir -p src/main/java/io/github/vincentchalamon/quinoa/recipe
cp sandbox/1.0/Quantity.java src/main/java/io/github/vincentchalamon/quinoa/recipe/
```

Ajoutez la ligne `package` en tête du fichier copié.

`QuantityDemo.java` et `ReadQuantity.java` restent dans `sandbox/1.0/` : le projet n'a pas besoin de `main`.

#### A5. Compiler avec `./mvnw compile`

Durée : 10 min, plus le temps du premier téléchargement.

```bash
./mvnw compile
find target/classes
git status
```

**Résultat attendu** :

- `BUILD SUCCESS` et une ligne `Compiling 1 source file with javac [debug release 25]` ;
- `Quantity.class` sous `target/classes/io/github/vincentchalamon/quinoa/recipe/` ;
- `git status` ne montre pas `target/` (déjà dans le `.gitignore`).

VSCode détecte le `pom.xml` et importe le projet : une notification peut apparaître en bas à droite. Ouvrez `Quantity.java` : aucune erreur ne doit être soulignée.

Committez (point de sauvegarde) :

```bash
git add pom.xml mvnw mvnw.cmd .mvn src
git commit -m "build: add Maven project and wrapper"
```

> **Point d'arrêt** : le projet compile. Le cycle B ajoute les tests.

---

## Cycle B - Les tests

### Théorie

#### Ce que vous savez déjà, ce qui change

Vous connaissez PHPUnit. JUnit et AssertJ reprennent les mêmes idées.

Correspondances :

- `TestCase` à étendre : rien à étendre, une classe ordinaire ;
- méthode `testXxx()` ou `#[Test]` : annotation `@Test` ;
- `$this->assertSame($expected, $actual)` : `assertThat(actual).isEqualTo(expected)` ;
- `$this->expectException(...)` : `assertThatThrownBy(() -> ...)` ;
- `phpunit.xml` : rien, Surefire trouve les tests tout seul.

Ce qui change :

- **JUnit lance les tests, AssertJ vérifie** : deux bibliothèques. JUnit a ses propres assertions, mais AssertJ se lit mieux et donne de meilleurs messages. Spring Boot l'inclut d'office.
- **Classe et méthodes sans `public`** : JUnit n'en a pas besoin. La visibilité package suffit.
- **Même package que la classe testée** : `QuantityTest` est dans `src/test/java/`, mais dans le **même package** que `Quantity`. Il a donc accès aux membres de visibilité package.
- **Nommage** : Surefire lance les classes dont le nom finit par `Test` (ou `Tests`). `QuantitySpec` serait ignoré sans erreur.

#### Deux syntaxes nouvelles

**`import static`** : importe une méthode statique, pour l'appeler sans le nom de sa classe.

```java
import static org.assertj.core.api.Assertions.assertThat;

assertThat(price).isEqualTo(copy);           // au lieu de Assertions.assertThat(...)
```

En PHP : `use function App\Helper\format;`

**Une lambda** : `() -> new Price(-1, "EUR")` est un bout de code passé en argument, exécuté plus tard.

En PHP : `fn() => new Price(-1, 'EUR')`.

`assertThatThrownBy` en a besoin : si on écrivait `new Price(-1, "EUR")` directement, l'exception partirait **avant** l'appel, et le test échouerait. Les lambdas sont détaillées à l'étape 1.3.

#### Exemple 3 : `PriceTest`

Fichier `src/test/java/com/example/shop/price/PriceTest.java` :

```java
package com.example.shop.price;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;

class PriceTest {

    @Test
    void createsValidPrice() {
        Price price = new Price(12.5, "EUR");

        assertThat(price.getAmount()).isEqualTo(12.5);
        assertThat(price.getCurrency()).isEqualTo("EUR");
    }

    @Test
    void rejectsNegativeAmount() {
        assertThatThrownBy(() -> new Price(-1, "EUR"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Montant négatif");
    }

    @Test
    void parsesText() {
        assertThat(Price.parse("12.50 EUR")).isEqualTo(new Price(12.5, "EUR"));
    }

    @Test
    void pricesWithSameValuesAreEqual() {
        Price price = new Price(12.5, "EUR");
        Price copy = new Price(12.5, "EUR");

        assertThat(price).isEqualTo(copy);
        assertThat(price).hasSameHashCodeAs(copy);
    }

    @Test
    void doubleArithmeticIsNotExact() {
        Price sum = new Price(0.1 + 0.2, "EUR");

        assertThat(sum).isNotEqualTo(new Price(0.3, "EUR"));
        assertThat(sum.getAmount()).isEqualTo(0.30000000000000004);
    }
}
```

Lecture ligne par ligne :

1. `package com.example.shop.price;`

   Le même package que `Price`, mais sous `src/test/java/`.

2. `import static org.assertj.core.api.Assertions.assertThat;`

   Rend `assertThat` utilisable seul.

3. `import org.junit.jupiter.api.Test;`

   L'annotation `@Test`.

4. `class PriceTest {`

   Sans `public`, sans `extends`. Le nom finit par `Test` : Surefire le trouvera.

5. `@Test void createsValidPrice() {`

   Un test. Le nom décrit le comportement attendu, en anglais, sans préfixe `test`.

   En PHP : `public function testCreatesValidPrice(): void`

6. `assertThat(price.getAmount()).isEqualTo(12.5);`

   On part de la valeur **obtenue**, puis on enchaîne les vérifications. L'ordre est l'inverse de PHPUnit (`assertSame($expected, $actual)`).

7. `assertThatThrownBy(() -> new Price(-1, "EUR"))`

   Exécute la lambda et capture l'exception.

   - `.isInstanceOf(IllegalArgumentException.class)` : le type de l'exception. `.class` désigne la classe elle-même (en PHP : `IllegalArgumentException::class`).
   - `.hasMessageContaining("Montant négatif")` : une partie du message.

   Le test échoue si aucune exception n'est levée.

8. `assertThat(Price.parse("12.50 EUR")).isEqualTo(new Price(12.5, "EUR"));`

   `isEqualTo` appelle `equals` : le test repose sur votre `equals`.

9. `assertThat(price).hasSameHashCodeAs(copy);`

   Vérifie le contrat `equals` / `hashCode` de l'étape 1.0.

10. `doubleArithmeticIsNotExact`

    Un test qui **documente une limite** : `0.1 + 0.2` ne vaut pas `0.3` en `double`. Il passe aujourd'hui. À l'étape 1.2, avec `BigDecimal`, cette limite disparaîtra et le test changera.

Exécution (chemins raccourcis) :

```console
$ ./mvnw -ntp test
...
[INFO] --- compiler:3.16.0:testCompile (default-testCompile) @ shop ---
[INFO] Recompiling the module because of changed dependency.
[INFO] Compiling 1 source file with javac [debug release 25] to target/test-classes
[INFO]
[INFO] --- surefire:3.6.1:test (default-test) @ shop ---
[INFO] Using auto detected provider org.apache.maven.surefire.junitplatform.JUnitPlatformProvider
[INFO]
[INFO] -------------------------------------------------------
[INFO]  T E S T S
[INFO] -------------------------------------------------------
[INFO] Running com.example.shop.price.PriceTest
[INFO] Tests run: 5, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.102 s -- in com.example.shop.price.PriceTest
[INFO]
[INFO] Results:
[INFO]
[INFO] Tests run: 5, Failures: 0, Errors: 0, Skipped: 0
[INFO]
[INFO] ------------------------------------------------------------------------
[INFO] BUILD SUCCESS
[INFO] ------------------------------------------------------------------------
```

#### Exemple 4 : un test qui échoue

Dans `doubleArithmeticIsNotExact`, remplaçons `isNotEqualTo` par `isEqualTo`. Ce serait le test "naïf" :

```console
$ ./mvnw -ntp -q test
[ERROR] Tests run: 5, Failures: 1, Errors: 0, Skipped: 0, Time elapsed: 0.120 s <<< FAILURE! -- in com.example.shop.price.PriceTest
[ERROR] com.example.shop.price.PriceTest.doubleArithmeticIsNotExact -- Time elapsed: 0.011 s <<< FAILURE!
org.opentest4j.AssertionFailedError:

expected: 0.3 EUR
 but was: 0.30000000000000004 EUR
	at com.example.shop.price.PriceTest.doubleArithmeticIsNotExact(PriceTest.java:43)

[ERROR] Failures:
[ERROR]   PriceTest.doubleArithmeticIsNotExact:43
expected: 0.3 EUR
 but was: 0.30000000000000004 EUR
[ERROR] Tests run: 5, Failures: 1, Errors: 0, Skipped: 0
[ERROR] Failed to execute goal org.apache.maven.plugins:maven-surefire-plugin:3.6.1:test (default-test) on project shop: There are test failures.
```

Comment lire :

- `PriceTest.doubleArithmeticIsNotExact:43` : le test et la ligne ;
- `expected` / `but was` : AssertJ affiche les deux objets avec leur `toString`. D'où l'intérêt d'un `toString` lisible ;
- `-q` (`--quiet`) n'affiche que les erreurs.

Le build échoue : `package` et `verify` ne s'exécutent pas.

#### Exemple 5 : un test au mauvais endroit

Déplaçons `PriceTest.java` dans `src/main/java/` (extrait, chemins raccourcis) :

```console
$ ./mvnw -ntp -q compile
[ERROR] COMPILATION ERROR :
[ERROR] .../src/main/java/com/example/shop/price/PriceTest.java:[3,35] package org.assertj.core.api does not exist
[ERROR] .../src/main/java/com/example/shop/price/PriceTest.java:[6,29] package org.junit.jupiter.api does not exist
[ERROR] .../src/main/java/com/example/shop/price/PriceTest.java:[10,6] cannot find symbol
  symbol:   class Test
  location: class com.example.shop.price.PriceTest
```

C'est le **scope** `test` en action : JUnit et AssertJ n'existent pas pour le code de production. `[3,35]` se lit "ligne 3, colonne 35".

En PHP, rien n'empêche le code de production d'utiliser PHPUnit tant que `vendor/` le contient.

### Pièges

- **Ordre des arguments** : `assertThat(obtenu).isEqualTo(attendu)`. Inversés, le test passe ou échoue pareil, mais le message `expected` / `but was` ment.
- **`assertThatThrownBy(new Price(...))`** sans `() ->` : l'exception part avant l'assertion.
- **Nom de classe** : une classe de test qui ne finit pas par `Test` n'est pas lancée, sans erreur. Vérifiez le nombre `Tests run`.
- **Test d'un seul cas** : un test par règle. Un test qui vérifie trois règles s'arrête à la première qui échoue et cache les autres.

### Vérifiez votre compréhension

1. Pourquoi `assertThatThrownBy` prend-il une lambda, et pas directement `new Price(-1, "EUR")` ?
2. Pourquoi `PriceTest` n'est-il pas `public` ?
3. Que vérifie `isEqualTo` sur deux objets `Price` ?

---

### Pratique

#### B1. Écrire le premier test

Durée : 15 min. Modèle : exemple 3, `createsValidPrice`.

Créez `QuantityTest.java` dans `src/test/java/`, dans le même package que `Quantity`. Un seul test pour commencer : une quantité valide garde sa valeur et son unité.

```bash
./mvnw -ntp test
```

**Résultat attendu** : `Tests run: 1, Failures: 0, Errors: 0, Skipped: 0` et `BUILD SUCCESS`.

#### B2. Mettre un test au mauvais endroit

Durée : 5 min. Modèle : exemple 5.

Erreur volontaire :

1. déplacez `QuantityTest.java` dans le répertoire de `Quantity.java`, sous `src/main/java/` ;
2. lancez `./mvnw -ntp -q compile` ;
3. lisez l'erreur, puis remettez le fichier à sa place ;
4. relancez `./mvnw -ntp test`.

**Résultat attendu** : à l'étape 2, `package org.junit.jupiter.api does not exist`. À l'étape 4, `BUILD SUCCESS`.

#### B3. Tester les règles de `Quantity`

Durée : 45 min. Modèle : exemple 3.

Un test par règle. Cas à couvrir :

1. valeur nulle (`0`) refusée ;
2. valeur négative refusée ;
3. `NaN` refusé ;
4. infini refusé ;
5. unité `null` refusée ;
6. unité vide ou faite d'espaces refusée ;
7. `parse` d'un texte valide ;
8. `parse` d'un texte mal formé (`"250g"`) ;
9. `parse` d'un nombre invalide (`"abc g"`), avec la cause conservée ;
10. deux quantités de mêmes valeurs : égales, même `hashCode` ;
11. `toString`.

Pour le cas 9, cherchez dans la Javadoc d'AssertJ la méthode qui vérifie la **cause** d'une exception : https://www.javadoc.io/doc/org.assertj/assertj-core/3.27.7/org/assertj/core/api/AbstractThrowableAssert.html

Les cas 1 à 4 se ressemblent beaucoup : c'est normal. L'étape 1.2 les regroupera en un test paramétré.

**Résultat attendu** : `Tests run: 12, Failures: 0` environ (B1 compris), selon votre découpage.

> **Point d'arrêt** : les règles sont testées. Committez : `git commit -am "test: cover Quantity rules"`. Pour un nouveau fichier, `git add` d'abord.

#### B4. Révéler le problème des `double`

Durée : 15 min. Modèle : exemples 3 et 4, `doubleArithmeticIsNotExact`.

1. Écrivez le test "naïf" : une quantité de `0.1 + 0.2` g est égale à une quantité de `0.3` g.
2. Lancez `./mvnw -ntp -q test` et lisez l'échec.
3. Transformez-le en test qui **documente** la limite, comme l'exemple.

**Résultat attendu** à l'étape 2 :

```console
expected: 0.3 g
 but was: 0.30000000000000004 g
```

Puis, à l'étape 3, `BUILD SUCCESS`.

Pour une recette, `0.30000000000000004 g` est inoffensif. Pour une conversion d'unités ou une somme de prix, l'écart s'accumule. C'est le point de départ de l'étape 1.2.

#### B5. Lancer les tests depuis VSCode

Durée : 10 min.

1. Ouvrez `QuantityTest.java`.
2. Dans la marge, à gauche de `class QuantityTest`, cliquez sur le triangle vert : tous les tests de la classe sont lancés.
3. Ouvrez la vue "Testing" (icône de fiole, barre latérale gauche) : l'arbre des tests s'affiche.
4. Faites échouer un test volontairement, relancez-le seul depuis la marge, puis corrigez.
5. Placez un point d'arrêt dans un test, puis clic droit sur le triangle : "Debug Test".

**Résultat attendu** : tous les tests en vert dans la vue "Testing" ; le débogueur s'arrête au point d'arrêt.

#### B6. Committer et ouvrir la PR

Durée : 15 min.

```bash
./mvnw -ntp clean verify
./mvnw -ntp package
jar tf target/*.jar
```

**Résultat attendu** :

- `BUILD SUCCESS` ;
- le `.jar` contient `Quantity.class`, mais aucune classe de test ni JUnit.

Puis :

1. committez ce qui reste ;
2. poussez la branche : `git push -u origin ch01/01-maven` ;
3. ouvrez la PR : `gh pr create`. Titre en Conventional Commits, description selon le modèle.

Pas encore de CI (chapitre 2) : la revue se fait en session.

> **Point d'arrêt** : la pratique est terminée. Il reste la validation, en session.

---

## Validation

En session, Claude :

- relit la PR ;
- vérifie les points ci-dessous ;
- vous pose les questions "expliquez".

Points vérifiables :

- [x] `./mvnw clean verify` passe depuis un clone propre.
- [x] `./mvnw -v` affiche Maven 3.9.16.
- [x] Le `.jar` ne contient ni test ni dépendance de test.
- [x] Chaque règle de `Quantity` a son test.
- [x] Le test des `double` passe et documente la limite.
- [x] Les tests se lancent depuis VSCode.

Questions "expliquez" :

1. Pourquoi committer `mvnw` et `.mvn/`, alors que `target/` est ignoré ?
2. Pourquoi JUnit est-il introuvable depuis `src/main/java/` ?
3. Que se passe-t-il, phase par phase, quand on lance `./mvnw verify` ?

---

## Documentation

- Maven en 5 minutes : https://maven.apache.org/guides/getting-started/maven-in-five-minutes.html
- Cycle de vie : https://maven.apache.org/guides/introduction/introduction-to-the-lifecycle.html
- Référence du POM : https://maven.apache.org/pom.html
- Scopes et BOM : https://maven.apache.org/guides/introduction/introduction-to-dependency-mechanism.html
- Maven Wrapper : https://maven.apache.org/tools/wrapper/
- Compilateur, option `release` : https://maven.apache.org/plugins/maven-compiler-plugin/examples/set-compiler-release.html
- Surefire et JUnit Platform : https://maven.apache.org/surefire/maven-surefire-plugin/examples/junit-platform.html
- Guide JUnit : https://docs.junit.org/current/user-guide/
- AssertJ : https://assertj.github.io/doc/
- Tests Java dans VSCode : https://code.visualstudio.com/docs/java/java-testing

## Pour aller plus loin (facultatif)

- `./mvnw dependency:tree` : l'arbre des dépendances, transitives comprises. Équivalent de `composer depends` à l'envers.
- `./mvnw help:effective-pom` : le `pom.xml` complet, avec toutes les valeurs par défaut, dont les versions des plugins non déclarés.
- Les versions par défaut des plugins (`clean`, `jar`...) dépendent de Maven. Le plugin Enforcer (chapitre 2) et le parent Spring Boot (chapitre 3) les rendront explicites.
