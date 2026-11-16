# Chapitre 0 - Environnement et dépôt

Objectif du chapitre : une machine prête (JDK 25, outils CLI, VSCode) et un dépôt aux conventions posées (GitHub Flow, `main` protégée) avant d'écrire la moindre ligne de Java "projet".

| Étape | Livrable | Branche / PR |
|---|---|---|
| 0.1 JDK et outils CLI | machine configurée (rien dans le dépôt) | aucune |
| 0.2 VSCode pour Java | `.vscode/`, `.editorconfig` | `ch00/02-vscode` |
| 0.3 Conventions du dépôt | `.gitignore`, `.sdkmanrc`, modèle de PR, protection de `main` | `ch00/03-repo-conventions` |

> L'étape 0.1 ne produit aucun fichier versionné : le `HelloWorld.java` se fait dans un répertoire jetable **hors du dépôt** (ex. `~/tmp/java-hello`). Le `.sdkmanrc` créé en 0.1 sera committé en 0.3, avec le `.gitignore`.

---

## Étape 0.1 - JDK et outils en ligne de commande

### État constaté de la machine (2026-11-16)

| Outil | État | Action |
|---|---|---|
| JDK | OpenJDK **21** (paquet Ubuntu), `JAVA_HOME` et `PATH` codés en dur dans `~/.zshrc` (l. 133-134) | installer SDKMAN + Temurin 25, retirer ces deux lignes |
| SDKMAN | absent | installer |
| `docker` / `docker compose` | 29.9 / v5.7, utilisable sans `sudo` | rien |
| `gh` | 2.104, authentifié | rien |
| `httpie` | absent | installer |

### Concepts

#### JDK, JRE, JVM

- **JVM** (Java Virtual Machine) : exécute du *bytecode* (fichiers `.class`). Elle compile à la volée (JIT) le code "chaud" en code machine. C'est l'équivalent du Zend Engine, mais avec une différence fondamentale : **un processus JVM vit longtemps** (une application Spring tourne des jours), alors qu'en PHP chaque requête repart de zéro. On y reviendra souvent (état partagé, singletons, thread-safety).
- **JRE** (Runtime Environment) : JVM + bibliothèque standard. De quoi *exécuter*.
- **JDK** (Development Kit) : JRE + outils de développement (`javac`, `jshell`, `jar`, `javadoc`, `jlink`, `jcmd`...). De quoi *développer*. C'est ce qu'il vous faut.

Depuis Java 11, Oracle ne distribue plus de JRE séparé ; certaines distributions (dont Temurin) en publient encore, utiles pour des images Docker plus légères (chapitre 10).

#### Distributions : pourquoi Temurin

Java est une spécification ; **OpenJDK** est l'implémentation de référence open source. Plusieurs fournisseurs construisent et maintiennent des binaires à partir du même code : Eclipse **Temurin** (Adoptium), Amazon Corretto, Azul Zulu, Oracle OpenJDK, Microsoft, etc. Fonctionnellement quasi identiques ; on choisit Temurin pour sa neutralité (fondation Eclipse), ses tests de conformité (TCK) et ses mises à jour régulières. `actions/setup-java` en CI (chapitre 2) utilisera aussi Temurin : même JDK en local et en CI.

**Versions LTS** : une LTS tous les deux ans (17, 21, **25**). Les versions intermédiaires (26, 27) ne sont supportées que six mois. Un projet "sérieux" vise une LTS, d'où Java 25.

Numérotation : `25.0.5` = version 25, 5e mise à jour de sécurité/correctifs. Les mises à jour trimestrielles ne cassent rien : on les applique sans hésiter (équivalent des patchs PHP 8.3.x).

#### SDKMAN

Gestionnaire de versions de SDK de l'écosystème JVM (JDK, Maven, Gradle, Spring Boot CLI...). Équivalents : **phpenv / asdf**, ou `symfony local:php:list` + `.php-version`.

- Installe chaque version sous `~/.sdkman/candidates/java/<version>`.
- Une version **par défaut** (lien symbolique `current`), une version **par shell** (`sdk use`), une version **par projet** via un fichier `.sdkmanrc` (équivalent de `.php-version` / `.tool-versions`).
- Positionne `JAVA_HOME` et le `PATH`.

#### `JAVA_HOME`

Variable lue par de nombreux outils pour trouver le JDK : **Maven Wrapper** (chapitre 1), l'extension Java de VSCode (chapitre 0.2), Gradle, certains IDE. Si `JAVA_HOME` et le `java` du `PATH` divergent, on obtient des erreurs déroutantes (compilation en 25, exécution en 21, ou l'inverse). Règle : **un seul responsable** de `JAVA_HOME`, ici SDKMAN.

#### Compiler et exécuter : `javac`, `java`, `jshell`

Java est un langage **compilé** vers du bytecode :

```
HelloWorld.java  --javac-->  HelloWorld.class  --java-->  exécution sur la JVM
```

- `javac HelloWorld.java` produit `HelloWorld.class`.
- `java HelloWorld` exécute la **classe** `HelloWorld` (nom de classe, pas de fichier, sans `.class`), cherchée dans le *classpath* (par défaut le répertoire courant).
- `java HelloWorld.java` (mode "source launcher", JEP 330, étendu au multi-fichiers par JEP 458) compile **en mémoire** puis exécute, sans produire de `.class`. Pratique pour des scripts et l'apprentissage ; un vrai projet passe par Maven (chapitre 1).
- `jshell` : REPL, équivalent de `php -a` ou de PsySH. Idéal pour tester une API de la bibliothèque standard avant de l'utiliser.

Pas d'équivalent direct en PHP pour l'étape `javac` : la plus proche est l'OPcache, mais elle est invisible et ne vérifie pas les types. Ici, **le compilateur refuse le programme** si les types sont incohérents : beaucoup d'erreurs que PHPStan signalerait (au mieux) deviennent bloquantes.

#### Java 25 : fichiers source compacts (JEP 512)

Depuis Java 25, un petit programme n'a plus besoin de la cérémonie `public class ... public static void main(String[] args)` :

```java
void main() {
    IO.println("Bonjour");
}
```

C'est une **classe implicite** avec une méthode `main` d'instance, et `IO` (dans `java.lang`) simplifie les entrées/sorties console. Pratique pour des scripts ; le code du projet utilisera des classes classiques dans des packages. Écrivez les **deux** formes pour voir la différence.

### Étapes

#### 1. Installer SDKMAN

Prérequis (`zip`, `unzip`, `curl`) : déjà présents.

```bash
curl -s "https://get.sdkman.io" | bash
```

L'installeur ajoute en **fin** de `~/.zshrc` un bloc d'initialisation. Ouvrez un nouveau terminal, puis `sdk version`.

#### 2. Nettoyer `~/.zshrc`

Supprimez (ou commentez) les deux lignes qui forcent le JDK 21 :

```bash
export JAVA_HOME=/usr/lib/jvm/java-21-openjdk-amd64
export PATH="$JAVA_HOME/bin:$PATH"
```

Sinon elles entrent en concurrence avec SDKMAN, selon l'ordre des lignes. Vérifiez que le bloc SDKMAN reste **la dernière chose** qui touche au `PATH` dans `~/.zshrc` (c'est ce que demande la doc SDKMAN).

Si ces lignes servaient à l'outillage Android (`ANDROID_HOME` est défini juste au-dessus) : Android Studio embarque son propre JDK ; pour la CLI, installez un Temurin 21 via SDKMAN (`sdk install java 21.x.y-tem`) et utilisez `sdk use java 21...` dans ce contexte. Le paquet `openjdk-21` d'Ubuntu peut rester installé, il ne gênera plus.

#### 3. Installer Temurin 25

```bash
sdk list java            # repérer l'identifiant Temurin 25 le plus récent (colonne Identifier, suffixe -tem)
sdk install java <identifiant-25-tem>
```

Au 2026-11-16, l'API SDKMAN annonce `25.0.5.0+8-tem` comme dernière 25 et comme version par défaut ; **vérifiez** l'identifiant exact dans `sdk list java` plutôt que de le recopier. Acceptez de la définir comme version par défaut.

Commandes utiles : `sdk current java`, `sdk default java <id>`, `sdk use java <id>` (shell courant), `sdk upgrade java`.

#### 4. Version par projet : `.sdkmanrc`

Dans le dépôt :

```bash
cd ~/Sites/spring-boot
sdk env init      # crée .sdkmanrc avec la version courante
```

Puis activez le changement automatique en entrant dans un répertoire : `sdkman_auto_env=true` dans `~/.sdkman/etc/config`. Ne committez pas ce fichier maintenant : il partira dans la PR de l'étape 0.3.

#### 5. Installer httpie

```bash
sudo apt install --no-install-recommends httpie
```

(Ou `pipx install httpie` / `uv tool install httpie` pour une version plus récente.) Il servira à tester l'API à la main, en complément des fichiers `.http` versionnés de VSCode (chapitre 5).

#### 6. HelloWorld, hors du dépôt

**But** : voir les trois façons d'exécuter du Java et ce que fait le compilateur.

**Méthode** : on part d'un **programme d'exemple complet** (`Weather.java`), qu'on lit et qu'on exécute ; puis vous écrivez `HelloWorld.java` en l'**adaptant**. Même démarche pour la forme compacte (`Forecast.java`, puis `Hello.java`).

Syntaxe de référence : [mémo Java pour un développeur PHP](memo-java-pour-php.md).

Tout se passe dans un répertoire jetable, **hors du dépôt** (pas encore de `.gitignore`, et les `.class` générés n'ont rien à y faire) :

```bash
mkdir -p ~/tmp/java-hello && cd ~/tmp/java-hello
```

##### 6.1 Lire et exécuter l'exemple `Weather.java` (forme classique)

Créez `Weather.java` en recopiant ce fichier **tel quel** :

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

| Ligne | Signification | Équivalent PHP |
|---|---|---|
| `public class Weather {` | déclare la classe `Weather`. Une classe `public` doit être dans un fichier **du même nom** (`Weather.java`, même casse) : ce n'est pas une convention comme PSR-4, `javac` refuse de compiler sinon | `class Weather {` |
| `public static void main(String[] args) {` | le point d'entrée du programme. Signature imposée : `public` (la JVM l'appelle de l'extérieur), `static` (sans créer d'objet `Weather`), `void` (ne renvoie rien), `String[] args` (tableau des arguments de la ligne de commande) | le code au niveau racine d'un script |
| `if (args.length > 0) {` | `length` est un **champ** des tableaux (pas de parenthèses) | `if (count($argv) > 1) {` |
| `String city = args[0];` | déclare une variable **typée** : le type avant le nom, pas de `$`. `args[0]` est le **premier argument** | `$city = $argv[1];` |
| `System.out.println(...)` | affiche une ligne (avec retour à la ligne) | `echo ... . "\n";` |
| `"Il fait beau à " + city + " !"` | `+` concatène les chaînes. Les chaînes sont **toujours** entre guillemets doubles (`'x'` est un `char`, un caractère unique) | `"Il fait beau à " . $city . " !"` |
| `;` | obligatoire en fin d'instruction | idem |

**Piège de l'analogie avec `$argv`** : en PHP, `$argv[0]` est le nom du script. En Java, `args[0]` est le **premier vrai argument**. Et si `args` est vide, lire `args[0]` ne donne ni `null` ni un warning : une `ArrayIndexOutOfBoundsException` arrête le programme. D'où le `if (args.length > 0)`.

Exécutez-le, avec et sans argument :

```console
$ java Weather.java Lille
Il fait beau à Lille !
$ java Weather.java
Il fait beau !
```

`java` a compilé le fichier **en mémoire** puis l'a exécuté. Un `ls` le confirme : aucun fichier `.class` n'a été produit.

##### 6.2 À vous : `HelloWorld.java`

En adaptant `Weather.java`, écrivez `HelloWorld.java` qui affiche :

```console
$ java HelloWorld.java Vincent
Hello, Vincent!
$ java HelloWorld.java
Hello, World!
```

Ce qui change par rapport à l'exemple : le nom du fichier **et** de la classe, le nom de la variable, les messages. La structure reste la même.

##### 6.3 Compiler puis exécuter, en deux temps

C'est ce que fera Maven sur le vrai projet. Démonstration avec l'exemple :

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

- `javac` produit `Weather.class` : le **bytecode**, exécutable par la JVM.
- `java Weather` prend un **nom de classe** (pas un nom de fichier), cherché dans le répertoire courant (le *classpath* par défaut). D'où l'échec de `java Weather.class` : Java cherche une classe nommée `Weather.class`.

À vous : même chose avec `HelloWorld` (compilation, `ls`, exécution avec et sans argument).

Par curiosité : `javap -c HelloWorld` désassemble le `.class` (instructions de la JVM). Équivalent de regarder les opcodes PHP avec VLD : à survoler, rien à comprendre en détail.

##### 6.4 Forme compacte (Java 25) : exemple `Forecast.java`, puis `Hello.java`

Depuis Java 25 (JEP 512), un petit programme peut se passer de la déclaration de classe. Exemple complet, `Forecast.java` :

```java
void main(String[] args) {
    if (args.length > 0) {
        IO.println("Demain, du soleil à " + args[0]);
    } else {
        IO.println("Demain, du soleil");
    }
}
```

Différences avec la forme classique :

- pas de `public class ... { }` : le compilateur génère une classe implicite à partir du nom du fichier ;
- `main` sans `public` ni `static` ; le paramètre `String[] args` est même facultatif (`void main()`) si on n'utilise pas les arguments ;
- `IO.println(...)` remplace `System.out.println(...)`.

```console
$ java Forecast.java Lille
Demain, du soleil à Lille
$ java Forecast.java
Demain, du soleil
```

À vous : `Hello.java`, le même comportement que `HelloWorld.java`, en forme compacte. Exécutez-le avec `java Hello.java`, avec et sans argument. Comparez les deux fichiers : la forme compacte sert aux scripts et à l'apprentissage ; le code du projet utilisera des classes classiques, rangées dans des packages (chapitre 1).

##### 6.5 Provoquer une erreur de compilation

Exemple : dans `Weather.java`, ajouter sous `String city = args[0];` une ligne qui affecte une chaîne à une variable `int` :

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

À lire dans le message : le fichier et la ligne (`Weather.java:6`), la nature de l'erreur, et le `^` qui pointe l'expression fautive. Aucun `.class` n'est produit : le programme n'existe pas tant qu'il ne compile pas.

À vous : provoquez la même erreur dans `HelloWorld.java`, lisez le message, puis retirez la ligne.

En PHP, la même erreur n'apparaît qu'**à l'exécution**, et seulement si la ligne est atteinte (`TypeError`), ou au mieux dans PHPStan. En Java, le programme est rejeté avant d'avoir tourné.

##### Résumé

| Commande | Compile ? | Produit un `.class` ? | Argument attendu |
|---|---|---|---|
| `java HelloWorld.java` | oui, en mémoire | non | un fichier source |
| `javac HelloWorld.java` | oui | oui | un fichier source |
| `java HelloWorld` | non (exécute le `.class` existant) | non | un nom de classe |

#### 7. Premiers pas dans jshell

**But** : prendre en main le REPL, puis observer cinq comportements de Java qui surprennent un développeur PHP. Ils seront expliqués en détail au chapitre 1 ; ici, on observe.

##### 7.1 Exemple de session

Lancez `jshell`, puis tapez ces lignes une par une (Entrée après chacune) :

```
var city = "Lille"
city.length()
city.toUpperCase()
int n = "abc"
/vars
/exit
```

Vous devez obtenir :

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

Ce qu'il faut en retenir :

- `nom ==> valeur` : jshell affiche la valeur de chaque expression ou variable ;
- le résultat d'une expression non affectée est rangé dans une variable automatique (`$2`, `$3`...), réutilisable ;
- le `;` final est facultatif dans jshell (obligatoire dans un fichier `.java`) ;
- `var` laisse le compilateur **déduire** le type : `/vars` montre que `city` est une `String`. Ce n'est pas du typage dynamique : `city` restera une `String` ;
- les erreurs de type sont détectées comme par `javac`, avant exécution ;
- `city.length()` : une `String` est un objet, on appelle ses méthodes avec `.` (le `->` de PHP). Tapez `city.` puis **Tab** pour lister toutes ses méthodes ;
- certains packages sont importés d'office (`java.util`, `java.math`...) : `/imports` les liste. Dans un fichier `.java`, il faut les `import` en tête (équivalent des `use` PHP).

Autres commandes : `/list` (historique), `/methods`, `/help`. Flèche haut pour rappeler une ligne.

##### 7.2 Exercices de prédiction

Pour chaque bloc, **notez votre prédiction avant d'appuyer sur Entrée**, puis comparez. On en discute en session.

1. Division entière ou flottante
   ```
   var x = 10
   x / 3
   x / 3.0
   ```
   Observez la valeur **et le type** du résultat (`/vars`) dans les deux cas.

2. Calcul flottant
   ```
   0.1 + 0.2
   ```
   Même comportement qu'en PHP (`var_dump(0.1 + 0.2)`) : pourquoi est-ce un problème pour des quantités de recettes ?

3. Deux façons de comparer des décimaux exacts
   ```
   new BigDecimal("1.0").equals(new BigDecimal("1.00"))
   new BigDecimal("1.0").compareTo(new BigDecimal("1.00"))
   ```
   `new` crée un objet, comme en PHP. `equals` et `compareTo` sont deux méthodes de comparaison : observez qu'elles ne disent pas la même chose. C'est le cœur de l'étape 1.2.

4. Identité et égalité
   ```
   String a = "chat"
   String b = new String("chat")
   a == b
   a.equals(b)
   ```
   En Java, `==` sur des objets ne se comporte ni comme le `==` ni comme le `===` de PHP. Que compare-t-il, d'après le résultat ?

5. Liste immuable
   ```
   List.of(1, 2, 3).add(4)
   ```
   Lisez le nom de l'exception : il dit tout. Suite à l'étape 1.3.

### Critères de validation

- [ ] Dans un **nouveau** terminal : `java -version` et `javac -version` affichent 25 (Temurin) ; `echo $JAVA_HOME` pointe sous `~/.sdkman/candidates/java/`.
- [ ] `which java` pointe sous `~/.sdkman/` et plus sous `/usr/lib/jvm/`.
- [ ] `.sdkmanrc` présent dans le dépôt (non committé), et `cd` dans le dépôt active la bonne version.
- [ ] `http --version` répond ; `docker run --rm hello-world` fonctionne sans `sudo`.
- [ ] `java HelloWorld.java` et `java Hello.java` fonctionnent ; `javac` + `java HelloWorld` aussi.
- [ ] Session `jshell` faite ; vous savez expliquer le résultat de `a == b` et de `BigDecimal.equals`.

### Pièges

- **Deux JDK qui se battent** : lignes `JAVA_HOME` restantes dans `~/.zshrc`, `~/.zprofile`, `~/.profile` ou `/etc/environment`. `which java` et `echo $JAVA_HOME` doivent être cohérents.
- **Terminal non rechargé** : les changements de `~/.zshrc` ne s'appliquent qu'aux nouveaux shells. Pensez aussi à **redémarrer VSCode** (étape 0.2) : il hérite de l'environnement au lancement.
- **`java HelloWorld.class`** : erreur classique. `java` attend un nom de classe (`HelloWorld`) ou un fichier source (`HelloWorld.java`), jamais un `.class`.
- **Nom de fichier et classe publique** : avec `javac`, une classe `public` doit être dans un fichier du même nom (`HelloWorld` dans `HelloWorld.java`). La casse compte.
- **`==` sur des objets** compare les références, pas les valeurs. Ce n'est pas le `==` laxiste de PHP, ni son `===` : c'est l'identité d'objet. Pour les valeurs : `equals`.

### Documentation

- SDKMAN : https://sdkman.io/install et https://sdkman.io/usage (dont `.sdkmanrc` et `sdk env`)
- Adoptium Temurin, versions supportées : https://adoptium.net/support/
- Calendrier et support des versions Java : https://www.oracle.com/java/technologies/java-se-support-roadmap.html
- `java` (dont le mode source) : https://docs.oracle.com/en/java/javase/25/docs/specs/man/java.html
- `javac` : https://docs.oracle.com/en/java/javase/25/docs/specs/man/javac.html
- `jshell` : https://docs.oracle.com/en/java/javase/25/jshell/introduction-jshell.html
- JEP 330 (lancement de fichiers source) : https://openjdk.org/jeps/330 ; JEP 458 (multi-fichiers) : https://openjdk.org/jeps/458
- JEP 512 (fichiers source compacts et `main` d'instance) : https://openjdk.org/jeps/512
- Tutoriels officiels : https://dev.java/learn/getting-started/
- httpie : https://httpie.io/docs/cli

---

## Étape 0.2 - VSCode pour Java

Détail rédigé au démarrage de l'étape. Points d'attention connus :

- L'extension Java (Eclipse JDT LS) a besoin d'un JDK : elle le trouve via `JAVA_HOME` ou le réglage `java.jdt.ls.java.home`. VSCode doit être relancé depuis un shell où SDKMAN est actif.
- Le JDK du projet se déclare dans `java.configuration.runtimes` (`.vscode/settings.json`) ; préférer un chemin via `~/.sdkman/candidates/java/current` à un chemin versionné.

## Étape 0.3 - Conventions du dépôt

Détail rédigé au démarrage de l'étape. Livrable ajusté : `.sdkmanrc` (créé en 0.1) committé avec le `.gitignore` et le modèle de PR.
