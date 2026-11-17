# Étape 0.1 - JDK et outils en ligne de commande

[Chapitre 0](README.md) > Étape 0.1

## En bref

- **Objectif** : installer le JDK 25 et les outils du projet, puis compiler et exécuter vos premiers programmes Java.
- **Apport au projet** : l'environnement de travail. Aucun fichier du projet n'est créé.
- **Durée indicative** : une demi-journée, en deux cycles. Vous pouvez vous arrêter à chaque "point d'arrêt".
- **Exercices** : dans `sandbox/0.1/`, vérifiés par Claude en local, jamais committés.

Avancement :

- [x] A1. Installer SDKMAN
- [x] A2. Nettoyer `~/.zshrc`
- [x] A3. Installer Temurin 25
- [x] A4. Fixer la version du projet (`.sdkmanrc`)
- [x] A5. Installer httpie
- [x] B1. Préparer le bac à sable
- [x] B2. Écrire `HelloWorld.java`
- [x] B3. Compiler en deux temps
- [x] B4. Écrire `Hello.java` (forme compacte)
- [x] B5. Lire une erreur de compilation
- [x] B6. Prédire dans jshell
- [x] Validation en session

Astuce : dans VSCode, `Ctrl+Shift+O` affiche le plan du document pour naviguer entre les sections.

---

## Cycle A - Installer le JDK

### Théorie

#### JVM, JRE, JDK

La **JVM** (Java Virtual Machine) exécute du bytecode : les fichiers `.class`.

C'est l'équivalent du moteur Zend de PHP, avec une grande différence :

- en PHP, chaque requête repart de zéro ;
- en Java, **le processus vit longtemps**. Une application Spring tourne des jours.

On y reviendra souvent : état partagé, objets uniques pour toute l'application, accès concurrents.

Trois niveaux d'installation :

- **JVM** : exécute le bytecode.
- **JRE** : JVM + bibliothèque standard. De quoi exécuter.
- **JDK** : JRE + outils de développement (`javac`, `jshell`, `jar`...). De quoi développer. C'est ce qu'il vous faut.

#### Distributions et versions

Java est une spécification. **OpenJDK** est l'implémentation de référence, open source.

Plusieurs fournisseurs construisent des binaires à partir du même code : Temurin, Corretto, Zulu... Ils sont quasi identiques.

On choisit **Eclipse Temurin** : fondation neutre, tests de conformité, mises à jour régulières. La CI (chapitre 2) utilisera aussi Temurin : même JDK en local et en CI.

Versions :

- une version **LTS** (support long) tous les deux ans : 17, 21, **25** ;
- les versions intermédiaires (26, 27) ne sont supportées que six mois ;
- `25.0.5` = version 25, 5e mise à jour. Les mises à jour ne cassent rien, comme les patchs PHP 8.3.x.

#### SDKMAN et `JAVA_HOME`

**SDKMAN** installe et change de version de JDK (et de Maven, Gradle...).

Équivalent PHP : phpenv ou asdf, ou `symfony local:php:list` avec un fichier `.php-version`.

Trois niveaux de choix de version :

- **par défaut** pour toute la machine : `sdk default java <version>` ;
- **pour le shell courant** : `sdk use java <version>` ;
- **pour un projet** : un fichier `.sdkmanrc` à la racine, l'équivalent de `.php-version`.

**`JAVA_HOME`** indique aux outils où se trouve le JDK : Maven, l'extension Java de VSCode...

Règle : **un seul responsable** de `JAVA_HOME`. Ici, SDKMAN.

### Pièges

- **Deux JDK qui se battent** : si une ligne `JAVA_HOME` traîne dans `~/.zshrc`, `java` et `javac` peuvent venir de deux JDK différents. Vous compilez en 25 et exécutez en 21, sans le voir.
- **Terminal non rechargé** : une modification de `~/.zshrc` ne s'applique qu'aux **nouveaux** terminaux. VSCode aussi doit être relancé.

### Vérifiez votre compréhension

Répondez de tête, puis vérifiez dans la théorie :

1. Pourquoi faut-il un JDK, et pas seulement un JRE ?
2. Qui doit définir `JAVA_HOME` sur votre machine ?
3. À quoi sert le fichier `.sdkmanrc` ?

---

### Pratique

#### A1. Installer SDKMAN

Fait le 2026-11-16.

#### A2. Nettoyer `~/.zshrc`

Fait le 2026-11-16 : les deux lignes du JDK 21 sont commentées, le bloc SDKMAN est en fin de fichier.

#### A3. Installer Temurin 25

Fait le 2026-11-16 : `25.0.5.0+8-tem`.

Commandes utiles pour plus tard :

- `sdk current java` : version active ;
- `sdk upgrade java` : mise à jour.

#### A4. Fixer la version du projet

Fait le 2026-11-16 : `.sdkmanrc` créé, changement automatique activé.

Ne committez pas `.sdkmanrc` maintenant : il partira dans la PR de l'étape 0.3.

#### A5. Installer httpie

Durée : 5 min.

```bash
sudo apt install --no-install-recommends httpie
```

`--no-install-recommends` évite d'installer des paquets inutiles (bibliothèques JavaScript, en-têtes Python).

**Résultat attendu** : `http --version` affiche un numéro de version.

Vérifiez aussi Docker, nécessaire au chapitre 4 :

```bash
docker run --rm hello-world
```

**Résultat attendu** : un message "Hello from Docker!", sans avoir tapé `sudo`.

> **Point d'arrêt** : le cycle A est terminé. Vous pouvez faire une pause ici.

---

## Cycle B - Compiler et exécuter

### Théorie

#### Compiler, puis exécuter

Java est **compilé** en bytecode, puis exécuté par la JVM :

```text
HelloWorld.java  --(javac)-->  HelloWorld.class  --(java)-->  exécution
```

Trois commandes :

- `javac HelloWorld.java` : compile, et produit `HelloWorld.class`.
- `java HelloWorld` : exécute la **classe** `HelloWorld`. On donne un nom de classe, pas un nom de fichier.
- `java HelloWorld.java` : compile **en mémoire** puis exécute. Aucun `.class` n'est produit. Pratique pour apprendre ; le projet passera par Maven (chapitre 1).

Différence avec PHP : le compilateur **refuse** un programme dont les types sont incohérents. Beaucoup d'erreurs que PHPStan signalerait (au mieux) deviennent bloquantes.

#### Exemple 1 : un programme classique

Avant de l'exécuter, lisez-le et **prédisez** ce qu'il affiche avec et sans argument.

Fichier `Weather.java` :

```java
public class Weather {

    public static void main(String[] args) {
        if (args.length > 0) {
            String city = args[0];
            System.out.println("Il fait beau à " + city + " !");
        } else {
            System.out.println("Il fait beau !");
        }
    }
}
```

Lecture ligne par ligne :

1. `public class Weather {`

   Déclare la classe `Weather`.

   Une classe `public` doit être dans un fichier **du même nom** : `Weather.java`, même casse. Ce n'est pas une convention comme PSR-4 : `javac` refuse de compiler sinon.

2. `public static void main(String[] args) {`

   Le point d'entrée du programme. Cette signature est imposée :

   - `public` : la JVM appelle la méthode depuis l'extérieur ;
   - `static` : la JVM l'appelle sans créer d'objet `Weather` ;
   - `void` : la méthode ne renvoie rien ;
   - `String[] args` : le tableau des arguments de la ligne de commande.

3. `if (args.length > 0) {`

   `length` donne la taille d'un tableau. C'est un **champ** : pas de parenthèses.

   En PHP : `if (count($argv) > 1) {`

4. `String city = args[0];`

   Déclare une variable **typée** : le type avant le nom, pas de `$`.

   En PHP : `$city = $argv[1];`

   Attention : `args[0]` est le **premier argument**. En PHP, `$argv[0]` est le nom du script.

5. `System.out.println("Il fait beau à " + city + " !");`

   Affiche une ligne. Le `+` concatène les chaînes (le `.` de PHP).

   Les chaînes sont **toujours** entre guillemets doubles. `'x'` est un `char` : un seul caractère.

Pourquoi tester `args.length` ? Si `args` est vide, lire `args[0]` ne donne ni `null` ni un warning : une exception `ArrayIndexOutOfBoundsException` arrête le programme.

Exécution :

```console
$ java Weather.java Lille
Il fait beau à Lille !
$ java Weather.java
Il fait beau !
```

#### Exemple 2 : compiler en deux temps

C'est ce que fera Maven sur le vrai projet.

```console
$ javac Weather.java
$ ls
Weather.class  Weather.java
$ java Weather Lille
Il fait beau à Lille !
$ java Weather.class Lille
Erreur : impossible de trouver ou de charger la classe principale Weather.class
Causé par : java.lang.ClassNotFoundException: Weather.class
```

À retenir :

- `javac` produit `Weather.class` : le bytecode.
- `java Weather` prend un **nom de classe**, cherché dans le répertoire courant.
- `java Weather.class` échoue : Java lit `Weather.class` comme un nom complet, la classe `class` du package `Weather`. Il cherche donc un fichier `Weather/class.class`, qui n'existe pas.

#### Exemple 3 : la forme compacte (Java 25)

Depuis Java 25, un petit programme peut se passer de la déclaration de classe.

Fichier `Forecast.java` :

```java
void main(String[] args) {
    if (args.length > 0) {
        IO.println("Demain, du soleil à " + args[0]);
    } else {
        IO.println("Demain, du soleil");
    }
}
```

Ce qui change par rapport à l'exemple 1 :

- pas de `public class ... { }` : le compilateur crée la classe à partir du nom du fichier ;
- `main` sans `public` ni `static` ;
- `String[] args` est facultatif : `void main()` suffit si on n'utilise pas les arguments ;
- `IO.println(...)` remplace `System.out.println(...)`.

```console
$ java Forecast.java Lille
Demain, du soleil à Lille
$ java Forecast.java
Demain, du soleil
```

Cette forme sert aux scripts et à l'apprentissage. Le projet utilisera la forme classique.

#### Exemple 4 : une erreur de compilation

On ajoute dans `Weather.java`, sous `String city = args[0];`, une ligne fausse :

```java
            int temperature = "vingt";
```

```console
$ javac Weather.java
Weather.java:6: error: incompatible types: String cannot be converted to int
            int temperature = "vingt";
                              ^
1 error
```

Comment lire le message :

- `Weather.java:6` : le fichier et la ligne ;
- `incompatible types: String cannot be converted to int` : la nature de l'erreur ;
- le `^` : l'endroit exact du problème.

Aucun `.class` n'est produit : le programme n'existe pas tant qu'il ne compile pas.

En PHP, la même erreur n'apparaît qu'à l'exécution, et seulement si la ligne est atteinte.

#### Exemple 5 : une session jshell

`jshell` est le REPL de Java, l'équivalent de `php -a` ou de PsySH.

On tape une ligne, Entrée l'exécute :

```console
jshell> var city = "Lille"
city ==> "Lille"

jshell> city.length()
$2 ==> 5

jshell> city.toUpperCase()
$3 ==> "LILLE"

jshell> int n = "abc"
|  Error:
|  incompatible types: java.lang.String cannot be converted to int
|  int n = "abc";
|          ^---^

jshell> /vars
|    String city = "Lille"
|    int $2 = 5
|    String $3 = "LILLE"

jshell> /exit
|  Goodbye
```

À retenir :

- `nom ==> valeur` : jshell affiche la valeur de chaque ligne.
- Un résultat non affecté est rangé dans `$2`, `$3`... et reste réutilisable.
- Le `;` final est facultatif dans jshell. Il est obligatoire dans un fichier `.java`.
- `var` laisse le compilateur **déduire** le type. `/vars` montre que `city` est une `String`, et elle le restera : ce n'est pas du typage dynamique.
- `city.length()` : on appelle une méthode avec `.` (le `->` de PHP).
- Tapez `city.` puis **Tab** pour lister les méthodes disponibles.

Autres commandes : `/list` (historique), `/imports`, `/help`. La flèche haut rappelle une ligne.

### Pièges

- **`java HelloWorld.class`** : `java` attend un nom de classe ou un fichier `.java`, jamais un `.class`.
- **Nom de fichier et de classe** : une classe `public` doit être dans un fichier du même nom. La casse compte.
- **`==` sur des objets** : compare l'identité des objets, pas leur contenu, comme `===` sur des objets PHP. La surprise : en Java, une `String` est un objet, alors qu'en PHP une chaîne est une valeur. Pour comparer le contenu : `equals`. Vous le constaterez en B6.

### Vérifiez votre compréhension

1. Après `java Weather.java`, trouve-t-on un fichier `Weather.class` ?
2. Que faut-il taper pour exécuter `Weather.class` ?
3. Que contient `args[0]` après `java Weather.java Lille Lyon` ?

---

### Pratique

#### B1. Préparer le bac à sable

Durée : 5 min.

Les exercices vont dans `sandbox/` à la racine du dépôt. Ce répertoire ne sera jamais committé.

Tant que le `.gitignore` n'existe pas (étape 0.3), on l'exclut **localement** :

```bash
cd ~/Sites/spring-boot
echo "sandbox/" >> .git/info/exclude
mkdir -p sandbox/0.1
```

`.git/info/exclude` fonctionne comme un `.gitignore`, mais il n'est pas versionné.

Si vous avez commencé dans `~/tmp/java-hello`, déplacez vos fichiers :

```bash
mv ~/tmp/java-hello/*.java sandbox/0.1/
```

**Résultat attendu** : `git status` n'affiche pas `sandbox/`.

#### B2. Écrire `HelloWorld.java`

Durée : 15 min. Modèle : exemple 1.

Dans `sandbox/0.1/`, écrivez `HelloWorld.java` en adaptant `Weather.java`.

Ce qui change : le nom du fichier **et** de la classe, le nom de la variable, les messages. La structure reste la même.

```bash
cd ~/Sites/spring-boot/sandbox/0.1
java HelloWorld.java Vincent
java HelloWorld.java
```

**Résultat attendu** :

```console
$ java HelloWorld.java Vincent
Hello, Vincent!
$ java HelloWorld.java
Hello, World!
```

#### B3. Compiler en deux temps

Durée : 10 min. Modèle : exemple 2.

```bash
javac HelloWorld.java
ls
java HelloWorld Vincent
java HelloWorld
```

**Résultat attendu** : `HelloWorld.class` apparaît, et les deux exécutions affichent les mêmes messages qu'en B2.

Par curiosité, `javap -c HelloWorld` affiche les instructions de la JVM. À survoler seulement.

> **Point d'arrêt** : votre premier programme fonctionne.

#### B4. Écrire `Hello.java` (forme compacte)

Durée : 10 min. Modèle : exemple 3.

Écrivez `Hello.java` : le même comportement que `HelloWorld.java`, en forme compacte.

```bash
java Hello.java Vincent
java Hello.java
```

**Résultat attendu** : les mêmes messages qu'en B2.

Comparez les deux fichiers côte à côte.

#### B5. Lire une erreur de compilation

Durée : 5 min. Modèle : exemple 4.

1. Dans `HelloWorld.java`, ajoutez une ligne qui affecte une chaîne à une variable `int`.
2. Lancez `javac HelloWorld.java`.
3. Repérez dans le message : le numéro de ligne, la nature de l'erreur, le `^`.
4. Retirez la ligne et vérifiez que tout compile à nouveau.

#### B6. Prédire dans jshell

Durée : 20 min. Modèle : exemple 5.

Lancez `jshell`. Pour chaque bloc :

1. **notez votre prédiction** (sur papier ou dans `sandbox/0.1/predictions.txt`) ;
2. tapez les lignes ;
3. comparez.

On en discute en session.

Bloc 1, division :

```text
var x = 10
x / 3
x / 3.0
```

Observez la valeur et le type des résultats (`/vars`).

Bloc 2, calcul à virgule :

```text
0.1 + 0.2
```

Même comportement qu'en PHP. Pourquoi est-ce un problème pour des quantités de recette ?

Bloc 3, deux comparaisons de décimaux :

```text
new BigDecimal("1.0").equals(new BigDecimal("1.00"))
new BigDecimal("1.0").compareTo(new BigDecimal("1.00"))
```

`equals` et `compareTo` ne disent pas la même chose. C'est le cœur de l'étape 1.2.

Bloc 4, identité et égalité :

```text
String a = "chat"
String b = new String("chat")
a == b
a.equals(b)
```

Que compare `==`, d'après le résultat ?

Bloc 5, liste non modifiable :

```text
List.of(1, 2, 3).add(4)
```

Lisez le nom de l'exception : il dit tout.

> **Point d'arrêt** : la pratique est terminée. Il reste la validation, en session.

---

## Validation

En session, Claude :

- relit vos fichiers de `sandbox/0.1/` ;
- vérifie les points ci-dessous ;
- vous pose les questions "expliquez".

Points vérifiables, dans un **nouveau** terminal :

- [x] `java -version` et `javac -version` affichent 25.
- [x] `which java` pointe sous `~/.sdkman/`.
- [x] `echo $JAVA_HOME` pointe sous `~/.sdkman/candidates/java/`.
- [x] `http --version` répond.
- [x] `docker run --rm hello-world` fonctionne sans `sudo`.
- [x] `HelloWorld.java` et `Hello.java` donnent les résultats attendus.

Questions "expliquez" :

1. Pourquoi `a == b` et `a.equals(b)` donnent-ils des résultats différents ?
2. Pourquoi `java Weather.class` échoue-t-il ?
3. Pourquoi le compilateur refuse-t-il `int n = "abc"`, alors que PHP l'accepterait jusqu'à l'exécution ?

---

## Documentation

- SDKMAN : https://sdkman.io/usage
- Temurin, versions supportées : https://adoptium.net/support/
- Commande `java` : https://docs.oracle.com/en/java/javase/25/docs/specs/man/java.html
- Commande `javac` : https://docs.oracle.com/en/java/javase/25/docs/specs/man/javac.html
- jshell : https://docs.oracle.com/en/java/javase/25/jshell/introduction-jshell.html
- Forme compacte (JEP 512) : https://openjdk.org/jeps/512
- Premiers pas officiels : https://dev.java/learn/getting-started/
- httpie : https://httpie.io/docs/cli

## Pour aller plus loin (facultatif)

- Le lancement direct d'un fichier source : JEP 330 (https://openjdk.org/jeps/330) et sa version multi-fichiers, JEP 458 (https://openjdk.org/jeps/458).
- Calendrier de support des versions Java : https://www.oracle.com/java/technologies/java-se-support-roadmap.html
