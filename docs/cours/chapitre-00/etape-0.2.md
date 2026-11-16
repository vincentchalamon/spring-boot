# Étape 0.2 - VSCode pour Java

[Chapitre 0](README.md) > Étape 0.2

## En bref

- **Objectif** : un VSCode qui comprend Java, une configuration partagée par le dépôt, et un premier débogage.
- **Apport au projet** : `.vscode/extensions.json`, `.vscode/settings.json` et `.editorconfig`, versionnés.
- **Durée indicative** : une demi-journée, en deux cycles. Vous pouvez vous arrêter à chaque "point d'arrêt".
- **Branche** : `ch00/02-vscode`. La PR sera ouverte à l'étape 0.3, une fois le modèle de PR en place.

Avancement :

- [ ] A1. Créer la branche
- [ ] A2. Installer les extensions manquantes
- [ ] A3. Écrire `.vscode/extensions.json`
- [ ] A4. Écrire `.vscode/settings.json`
- [ ] A5. Écrire `.editorconfig`
- [ ] A6. Committer
- [ ] B1. Régler le confort de lecture (réglages personnels)
- [ ] B2. Voir une erreur sans compiler
- [ ] B3. Déboguer `HelloWorld.java`
- [ ] B4. Évaluer une expression pendant une pause
- [ ] Validation en session

Astuce : `Ctrl+Shift+O` affiche le plan du document.

---

## Cycle A - Extensions et réglages partagés

### Théorie

#### Le serveur de langage Java

VSCode ne connaît pas Java. Une extension lance un **serveur de langage** : un processus à part qui analyse le code et répond à l'éditeur (complétion, erreurs, renommage...).

Pour PHP, c'est le rôle d'Intelephense. Pour Java, c'est **Eclipse JDT Language Server**, fourni par l'extension "Language Support for Java" de Red Hat.

Ce qui change par rapport à Intelephense :

- JDT LS **compile** le code en continu. Une erreur de type apparaît soulignée en rouge pendant la frappe, sans lancer `javac`.
- Il comprend la structure du projet : avec Maven (chapitre 1), il lit le `pom.xml` pour connaître les dépendances et la version de Java.
- Sans Maven, il traite les fichiers `.java` isolés comme un projet "invisible". C'est le cas de `sandbox/` pour l'instant.

#### Qui fournit le JDK à VSCode ?

Deux besoins distincts :

1. **Faire tourner le serveur de langage** : l'extension Red Hat embarque son propre JRE. Rien à configurer.
2. **Compiler et exécuter votre code** : l'extension détecte les JDK installés, dont ceux de SDKMAN, et choisit celui qui correspond à la version visée par le projet.

Conséquence : `JAVA_HOME` n'est plus indispensable pour VSCode. On peut tout de même fixer le JDK par défaut, avec le réglage `java.configuration.runtimes`.

Ce réglage contient un **chemin de votre machine**. Il va donc dans vos réglages utilisateur, jamais dans le dépôt.

#### Réglages utilisateur, réglages de workspace, `.editorconfig`

Trois endroits, trois usages :

- **Réglages utilisateur** (`~/.config/Code/User/settings.json`) : vos préférences, pour tous les projets. Police, couleurs, chemins de votre machine.
- **Réglages de workspace** (`.vscode/settings.json`, versionné) : ce que tout contributeur du projet doit avoir. Formatage à l'enregistrement, par exemple.
- **`.editorconfig`** (versionné) : indentation, fin de ligne, encodage. Lu par presque tous les éditeurs, pas seulement VSCode.

En cas de conflit, le workspace l'emporte sur l'utilisateur.

Équivalent Symfony : vous connaissez déjà `.editorconfig`. Le dossier `.vscode/` joue le rôle d'un dossier `.idea/` partiellement versionné.

#### Recommander des extensions

`.vscode/extensions.json` liste les extensions **recommandées** pour le projet. À l'ouverture du dossier, VSCode propose d'installer celles qui manquent.

Il ne les installe pas de force : c'est une suggestion, comme la section `suggest` d'un `composer.json`.

Une extension se désigne par son identifiant `éditeur.nom`, visible dans sa page de la marketplace.

#### Les extensions du projet

Java et Spring :

- `vscjava.vscode-java-pack` : Extension Pack for Java. Serveur de langage, débogueur, tests, Maven.
- `vmware.vscode-boot-dev-pack` : Spring Boot Extension Pack. Complétion de `application.yaml`, tableau de bord des applications.

Fichiers du projet :

- `redhat.vscode-xml` : XML, pour le `pom.xml`.
- `redhat.vscode-yaml` : YAML, pour la configuration Spring et la CI.
- `anweber.vscode-httpyac` : httpYac. Exécute les fichiers `.http` versionnés (appels à l'API). Même syntaxe que le client HTTP de PhpStorm.
- `ms-azuretools.vscode-containers` : Container Tools. Docker et Docker Compose.

Qualité et outils :

- `sonarsource.sonarlint-vscode` : SonarQube for IDE. Signale les mauvaises pratiques pendant la frappe.
- `editorconfig.editorconfig` : applique le `.editorconfig`.
- `eamodio.gitlens` : historique git dans l'éditeur.

Pourquoi httpYac et pas REST Client ? REST Client n'est plus maintenu depuis 2022. httpYac lit les mêmes fichiers `.http`.

#### Exemple 1 : `extensions.json` d'un projet Symfony

Fichier `.vscode/extensions.json` :

```json
{
    "recommendations": [
        "bmewburn.vscode-intelephense-client",
        "xdebug.php-debug",
        "editorconfig.editorconfig"
    ]
}
```

Lecture ligne par ligne :

1. `"recommendations": [`

   La seule clé utile ici : un tableau d'identifiants.

2. `"bmewburn.vscode-intelephense-client",`

   Un identifiant `éditeur.nom`. Pas de numéro de version : VSCode installe la dernière.

3. `"editorconfig.editorconfig"`

   Pas de virgule après le dernier élément : ce fichier est du JSON strict sur ce point. VSCode le signale en jaune sinon.

#### Exemple 2 : `settings.json` d'un projet Symfony

Fichier `.vscode/settings.json` :

```json
{
    "editor.formatOnSave": true,
    "[php]": {
        "editor.defaultFormatter": "bmewburn.vscode-intelephense-client"
    },
    "intelephense.environment.phpVersion": "8.3.0"
}
```

Lecture ligne par ligne :

1. `"editor.formatOnSave": true,`

   Reformate le fichier à chaque enregistrement, pour tous les langages.

2. `"[php]": { ... }`

   Un bloc **par langage** : les réglages à l'intérieur ne s'appliquent qu'aux fichiers PHP.

3. `"intelephense.environment.phpVersion": "8.3.0"`

   Un réglage propre à une extension : le préfixe est le nom de l'extension.

Comment trouver le nom d'un réglage : `Ctrl+,`, tapez quelques mots, puis l'icône engrenage d'un réglage, "Copy Setting ID".

#### Exemple 3 : `.editorconfig` d'un projet Symfony

```ini
root = true

[*]
charset = utf-8
end_of_line = lf
indent_style = space
indent_size = 4
insert_final_newline = true
trim_trailing_whitespace = true

[*.{yaml,yml,json}]
indent_size = 2

[*.md]
trim_trailing_whitespace = false
```

Lecture ligne par ligne :

1. `root = true` : ne cherche pas d'autre `.editorconfig` dans les dossiers parents.
2. `[*]` : les règles qui suivent s'appliquent à tous les fichiers.
3. `indent_style = space` et `indent_size = 4` : quatre espaces, pas de tabulation.
4. `[*.{yaml,yml,json}]` : une exception pour ces extensions, deux espaces.
5. `[*.md]` : en Markdown, deux espaces en fin de ligne forcent un retour à la ligne. On ne les supprime pas.

Les sections du bas l'emportent sur celles du haut.

### Pièges

- **Chemin de machine dans le dépôt** : un `/home/vincent/...` dans `.vscode/settings.json` casse la configuration des autres contributeurs et de la CI. Il va dans les réglages utilisateur.
- **Formatage Java provisoire** : `editor.formatOnSave` utilise pour l'instant le formateur d'Eclipse. À l'étape 2.1, Spotless devient la référence ; on alignera alors VSCode dessus.
- **Extension Docker en double** : `ms-azuretools.vscode-docker` est l'ancienne extension, remplacée par Container Tools. Elle peut être désinstallée.
- **Recommandation n'est pas installation** : un collègue peut refuser. Ce qui doit être garanti pour tous (formatage, analyse) sera vérifié par Maven et la CI au chapitre 2.

### Vérifiez votre compréhension

1. Pourquoi le chemin du JDK ne va-t-il pas dans `.vscode/settings.json` ?
2. Quelle différence entre `.editorconfig` et `.vscode/settings.json` ?
3. Pourquoi JDT LS peut-il souligner une erreur de type avant toute compilation par `javac` ?

---

### Pratique

#### A1. Créer la branche

Durée : 2 min.

```bash
cd ~/Sites/spring-boot
git fetch
git switch -c ch00/02-vscode origin/main
```

**Résultat attendu** : `git status` affiche `On branch ch00/02-vscode`, avec `.sdkmanrc` non suivi (il partira en 0.3).

#### A2. Installer les extensions manquantes

Durée : 5 min.

Vous avez déjà les packs Java et Spring Boot. Il manque six extensions :

```bash
code --install-extension anweber.vscode-httpyac
code --install-extension sonarsource.sonarlint-vscode
code --install-extension editorconfig.editorconfig
code --install-extension eamodio.gitlens
code --install-extension redhat.vscode-xml
code --install-extension redhat.vscode-yaml
```

Facultatif, l'ancienne extension Docker :

```bash
code --uninstall-extension ms-azuretools.vscode-docker
```

**Résultat attendu** : `code --list-extensions` affiche les six identifiants.

#### A3. Écrire `.vscode/extensions.json`

Durée : 10 min. Modèle : exemple 1.

Créez `.vscode/extensions.json` à la racine du dépôt. Listez les dix extensions du projet (section "Les extensions du projet" plus haut).

**Résultat attendu** :

1. Fermez VSCode, puis relancez-le avec `code .` depuis la racine du dépôt.
2. Ouvrez la vue Extensions (`Ctrl+Shift+X`) et tapez `@recommended`.
3. La section "Workspace Recommendations" liste les dix extensions, toutes installées.

#### A4. Écrire `.vscode/settings.json`

Durée : 10 min. Modèle : exemple 2.

Trois réglages à mettre :

- `editor.formatOnSave` à `true` ;
- dans un bloc `[java]`, `editor.codeActionsOnSave` avec `"source.organizeImports": "explicit"` : trie les `import` et retire les inutilisés à l'enregistrement ;
- `java.configuration.updateBuildConfiguration` à `"automatic"` : recharge le projet quand le `pom.xml` change (chapitre 1), sans vous poser la question à chaque fois.

Pas de chemin de JDK, pas d'exclusion de fichiers : la recherche de VSCode ignore déjà ce qui est dans le `.gitignore`.

**Résultat attendu** : aucun soulignement dans le fichier. Un réglage inconnu serait affiché en grisé.

#### A5. Écrire `.editorconfig`

Durée : 10 min. Modèle : exemple 3.

Mêmes règles que l'exemple. L'indentation Java reste à quatre espaces ; elle sera confirmée au choix du formateur, à l'étape 2.1.

**Résultat attendu** :

1. Ouvrez `sandbox/0.1/HelloWorld.java` : il est indenté avec des tabulations.
2. Dans la barre d'état en bas, la mention "Spaces: 4" indique le réglage appliqué aux nouvelles lignes.
3. `Ctrl+Shift+P` puis "Format Document" : observez ce que deviennent les tabulations. On en parle en session.

#### A6. Committer

Durée : 2 min.

```bash
git add .vscode/extensions.json .vscode/settings.json .editorconfig
git commit -m "build: add shared VSCode configuration and EditorConfig"
```

Pas de `git add .` : `.sdkmanrc` attend l'étape 0.3.

> **Point d'arrêt** : le cycle A est terminé. Vous pouvez faire une pause ici.

---

## Cycle B - Confort et débogage

### Théorie

#### Réglages de confort

Ce sont des réglages personnels : ils vont dans les réglages utilisateur. `Ctrl+Shift+P` puis "Preferences: Open User Settings (JSON)".

Les leviers les plus utiles pour lire du code longtemps :

- une **police** où `l`, `1`, `I`, `0` et `O` sont bien distincts ;
- l'**espacement** entre les lettres et entre les lignes ;
- **moins d'éléments** qui bougent ou s'affichent en marge.

Exemple 4, à essayer et ajuster :

```json
{
    "editor.fontFamily": "'Atkinson Hyperlegible Mono', 'Fira Mono', monospace",
    "editor.fontSize": 16,
    "editor.lineHeight": 1.8,
    "editor.letterSpacing": 0.5,
    "editor.minimap.enabled": false,
    "editor.cursorBlinking": "solid",
    "gitlens.currentLine.enabled": false,
    "gitlens.codeLens.enabled": false
}
```

Lecture ligne par ligne :

1. `editor.fontFamily` : une liste de polices, la première disponible gagne. Atkinson Hyperlegible Mono a été dessinée pour distinguer les caractères proches. Fira Mono, déjà installée, sert de repli.
2. `editor.lineHeight` : une valeur inférieure à 8 est un multiplicateur de la taille de police.
3. `editor.letterSpacing` : en pixels.
4. `editor.minimap.enabled` : retire l'aperçu miniature à droite.
5. `editor.cursorBlinking` : un curseur fixe attire moins l'œil.
6. Les deux lignes `gitlens` : retirent l'auteur affiché en bout de ligne et les mentions au-dessus des méthodes. L'historique reste accessible par la barre latérale.

Le correcteur orthographique Code Spell Checker (`streetsidesoftware.code-spell-checker`) vérifie aussi les noms de variables et de méthodes : `recipeQuantiy` est signalé. Ajoutez `streetsidesoftware.code-spell-checker-french` pour les commentaires en français.

#### Déboguer en Java

Équivalent PHP : Xdebug. Le principe est le même : points d'arrêt, pas à pas, inspection des variables.

Ce qui change :

- **Rien à installer côté serveur** : le débogage est intégré à la JVM, via le protocole JDWP. Pas de `php.ini`, pas de port à ouvrir.
- **Pas de configuration pour un `main`** : au-dessus de chaque méthode `main`, VSCode affiche deux liens, "Run" et "Debug".
- **Le processus est en pause, pas terminé** : avec Spring (chapitre 3), une pause sur un point d'arrêt bloque une requête pendant que l'application continue de tourner.

Raccourcis :

- `F9` : poser ou retirer un point d'arrêt sur la ligne courante (ou clic dans la marge, à gauche du numéro de ligne).
- `F5` : continuer jusqu'au point d'arrêt suivant.
- `F10` : exécuter la ligne, sans entrer dans les méthodes appelées.
- `F11` : entrer dans la méthode appelée.
- `Shift+F5` : arrêter.

Pendant une pause, la **Debug Console** (`Ctrl+Shift+Y`) évalue des expressions Java dans le contexte courant, comme jshell.

La sortie du programme (`System.out`, `System.err`) s'affiche, elle, dans le **terminal intégré**.

#### Exemple 5 : déboguer `Weather.java`

Préparation :

```bash
mkdir -p ~/Sites/spring-boot/sandbox/0.2
```

Copiez l'exemple 1 de l'étape 0.1 (la version classique, avec `public class Weather`) dans `sandbox/0.2/Weather.java`.

Déroulé :

1. Ouvrez `sandbox/0.2/Weather.java`.
2. Posez un point d'arrêt sur la ligne `if (args.length > 0) {` (`F9`).
3. Cliquez sur "Debug" au-dessus de `main`.
4. L'exécution s'arrête : la ligne est surlignée en jaune.
5. Dans le panneau "Variables", à gauche : `args` vaut `String[0]@...`, un tableau vide. Le nombre après `@` identifie l'objet.
6. `F10` : la ligne surlignée passe dans le `else`, car le tableau est vide.
7. `F5` : le programme se termine.

Sortie, dans le terminal intégré qui s'ouvre en bas :

```console
Il fait beau !
```

Le lien "Debug" ne passe aucun argument : c'est pour cela qu'on passe dans le `else`.

### Pièges

- **Point d'arrêt sur une ligne vide ou une accolade** : il ne sera jamais atteint. Posez-le sur une instruction.
- **Fichier modifié pendant la pause** : le code exécuté reste l'ancien. Arrêtez (`Shift+F5`) et relancez.
- **"Run" ne s'arrête pas** : seul "Debug" respecte les points d'arrêt.

### Vérifiez votre compréhension

1. Pourquoi n'y a-t-il rien à configurer côté JVM pour déboguer, contrairement à Xdebug ?
2. Que vaut `args` quand on lance "Debug" depuis VSCode ?
3. Où vont les réglages de police, et pourquoi ?

---

### Pratique

#### B1. Régler le confort de lecture

Durée : 15 min. Modèle : exemple 4.

1. Installez la police Atkinson Hyperlegible Mono : téléchargez-la depuis https://fonts.google.com/specimen/Atkinson+Hyperlegible+Mono puis :

   ```bash
   unzip ~/Téléchargements/Atkinson_Hyperlegible_Mono.zip -d ~/.local/share/fonts/atkinson-mono
   fc-cache -f
   fc-list | grep -i "hyperlegible mono"
   ```

2. Ouvrez vos réglages utilisateur en JSON et ajoutez les réglages de l'exemple 4, à l'intérieur des accolades existantes.
3. Ajustez les valeurs à votre confort. Vous pourrez les changer à tout moment.

Ce fichier n'est pas dans le dépôt : rien à committer.

**Résultat attendu** : dans un fichier `.java`, `l1I0O` s'affiche avec cinq formes bien distinctes.

#### B2. Voir une erreur sans compiler

Durée : 5 min. Erreur volontaire.

1. Ouvrez `sandbox/0.1/HelloWorld.java`.
2. Ajoutez une ligne `int age = "quarante";` dans le `main`. N'enregistrez pas.
3. Observez le soulignement rouge, puis survolez-le.
4. `Ctrl+Shift+M` ouvre le panneau "Problems".
5. Retirez la ligne.

**Résultat attendu** : `Type mismatch: cannot convert from String to int`, affiché pendant la frappe.

Le message diffère de celui de `javac` (`incompatible types`) : JDT LS utilise son propre compilateur, celui d'Eclipse. Maven, lui, utilisera `javac`.

#### B3. Déboguer `HelloWorld.java`

Durée : 15 min. Modèle : exemple 5.

1. Posez un point d'arrêt sur la ligne du `if`.
2. Lancez "Debug".
3. Notez la valeur de `args` dans "Variables".
4. Avancez ligne par ligne avec `F10` jusqu'à la fin.

**Résultat attendu** : le point d'arrêt est atteint, la ligne passe dans le `else`, et le terminal intégré affiche votre message d'erreur.

#### B4. Évaluer une expression pendant une pause

Durée : 10 min.

1. Relancez "Debug" sur `HelloWorld.java`, avec le même point d'arrêt.
2. Pendant la pause, ouvrez la Debug Console (`Ctrl+Shift+Y`).
3. Avant chaque ligne, **prédisez** le résultat, puis tapez-la :

   ```text
   args.length
   args.length > 0
   "Vincent".toUpperCase()
   ```

4. `F5` pour terminer.

**Résultat attendu** : les valeurs s'affichent sous chaque ligne, comme dans jshell.

> **Point d'arrêt** : la pratique est terminée. Il reste la validation, en session.

---

## Validation

En session, Claude :

- relit les trois fichiers sur la branche `ch00/02-vscode` ;
- vérifie les points ci-dessous ;
- vous pose les questions "expliquez".

Points vérifiables :

- [ ] Après `code .`, le filtre `@recommended` liste les dix extensions du projet.
- [ ] `.vscode/settings.json` ne contient aucun chemin de la machine.
- [ ] Un fichier enregistré se termine par une ligne vide et n'a pas d'espace en fin de ligne.
- [ ] Un point d'arrêt est atteint dans `HelloWorld.java`.
- [ ] Le commit est sur la branche `ch00/02-vscode`, avec un message Conventional Commits.

Questions "expliquez" :

1. Pourquoi les recommandations d'extensions ne suffisent-elles pas à garantir le formatage du code ?
2. Qu'est-il arrivé aux tabulations de `HelloWorld.java` au formatage, et pourquoi ?
3. Pourquoi une pause sur un point d'arrêt sera-t-elle différente dans une application Spring ?

---

## Documentation

- Java dans VSCode : https://code.visualstudio.com/docs/java/java-tutorial
- Choix du JDK par l'extension : https://github.com/redhat-developer/vscode-java#setting-the-jdk
- Déboguer du Java : https://code.visualstudio.com/docs/java/java-debugging
- Réglages utilisateur et workspace : https://code.visualstudio.com/docs/configure/settings
- Recommandations d'extensions : https://code.visualstudio.com/docs/configure/extensions/extension-marketplace#_workspace-recommended-extensions
- EditorConfig : https://editorconfig.org/
- httpYac : https://httpyac.github.io/

## Pour aller plus loin (facultatif)

- Passer des arguments au débogage : "Run and Debug" (`Ctrl+Shift+D`), "create a launch.json file", puis ajouter `"args": "${command:SpecifyProgramArgs}"` dans la configuration. VSCode demande les arguments à chaque lancement. Ce `launch.json` reste personnel tant qu'on n'en a pas besoin à plusieurs.
- Ce que fait le lien "Debug" sous le capot : `java -agentlib:jdwp=...` lance la JVM avec l'agent de débogage. Voir https://docs.oracle.com/en/java/javase/25/docs/specs/jpda/conninv.html
