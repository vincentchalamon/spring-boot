# Mémo : Maven pour un développeur Composer

Aide-mémoire, complété au fil des chapitres. Détails et exemples : [étape 1.1](chapitre-01/etape-1.1.md).

Référence officielle : https://maven.apache.org/guides/

---

## Correspondances

| Composer | Maven |
|---|---|
| `composer.json` | `pom.xml` |
| `composer.lock` | aucun : versions exactes dans `pom.xml` |
| `vendor/` | `~/.m2/repository/` (partagé par tous les projets) |
| Packagist | Maven Central |
| `require-dev` | `<scope>test</scope>` |
| autoload PSR-4 | `src/main/java/` + ligne `package` |
| `composer.phar` versionné | Maven Wrapper (`mvnw`, `.mvn/`) |

## Commandes

Toujours `./mvnw`, jamais `mvn`.

| Commande | Effet |
|---|---|
| `./mvnw compile` | compile `src/main/java/` |
| `./mvnw test` | compile, puis lance les tests |
| `./mvnw package` | tout, puis produit le `.jar` dans `target/` |
| `./mvnw verify` | tout, puis les vérifications : la commande de référence |
| `./mvnw clean verify` | idem, en repartant de zéro |
| `./mvnw -v` | versions de Maven et du JDK |
| `./mvnw dependency:tree` | arbre des dépendances |
| `./mvnw test -Dtest='FooTest#bar'` | un seul test |

Options :

- `-ntp` : masque la progression des téléchargements ;
- `-q` : n'affiche que les erreurs.

## Phases

Une phase exécute toutes les précédentes :

```text
validate -> compile -> test -> package -> verify -> install
```

`clean` est à part : il supprime `target/`.

## Squelette de `pom.xml`

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 https://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>

    <groupId>com.example</groupId>                     <!-- domaine inversé -->
    <artifactId>shop</artifactId>
    <version>0.1.0-SNAPSHOT</version>                  <!-- SNAPSHOT : en développement -->

    <properties>
        <maven.compiler.release>25</maven.compiler.release>
        <project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>
    </properties>

    <dependencyManagement>                             <!-- fixe des versions, ne télécharge rien -->
        <dependencies>
            <dependency>                               <!-- BOM : liste de versions importée -->
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
            <artifactId>junit-jupiter</artifactId>     <!-- version fournie par le BOM -->
            <scope>test</scope>
        </dependency>
    </dependencies>
</project>
```

## Arborescence

```text
pom.xml
mvnw, mvnw.cmd, .mvn/wrapper/     committés
src/main/java/<package>/          production
src/test/java/<package>/          tests, même package
target/                           produit par Maven, ignoré par git
```
