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
sudo apt install httpie
```

(Ou `pipx install httpie` / `uv tool install httpie` pour une version plus récente.) Il servira à tester l'API à la main, en complément des fichiers `.http` versionnés de VSCode (chapitre 5).

#### 6. HelloWorld, hors du dépôt

Dans `~/tmp/java-hello` :

1. `HelloWorld.java` en forme **classique** (classe publique, `public static void main(String[] args)`), qui affiche aussi le premier argument s'il existe.
2. Exécution directe : `java HelloWorld.java Vincent`.
3. Compilation explicite : `javac HelloWorld.java`, observez le `.class` produit, puis `java HelloWorld Vincent`. Inspectez le bytecode par curiosité : `javap -c HelloWorld`.
4. `Hello.java` en forme **compacte** (JEP 512), exécuté avec `java Hello.java`.
5. Provoquez une erreur de type (affecter une `String` à un `int`) et lisez le message de `javac`.

#### 7. Premiers pas dans jshell

```
jshell
```

À essayer (et à **prédire avant d'exécuter**) :

- `var x = 10; x / 3` puis `x / 3.0` (division entière vs flottante).
- `0.1 + 0.2` (même problème qu'en PHP ; teaser de `BigDecimal` en 1.2).
- `new java.math.BigDecimal("1.0").equals(new java.math.BigDecimal("1.00"))` puis `.compareTo(...)` sur les mêmes valeurs (teaser de l'étape 1.2).
- `String a = "chat"; String b = new String("chat");` puis `a == b` et `a.equals(b)`.
- `List.of(1, 2, 3).add(4)` (l'exception vaut explication ; on y reviendra en 1.3).
- Commandes : `/vars`, `/methods`, `/imports`, `/list`, `/help`, complétion par Tab (y compris sur `String.` pour lister les méthodes), `/exit`.

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
