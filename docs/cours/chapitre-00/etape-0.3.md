# Étape 0.3 - Conventions du dépôt

[Chapitre 0](README.md) > Étape 0.3

## En bref

- **Objectif** : poser le cadre GitHub Flow avant le premier code Java : fichiers ignorés, version du JDK, modèle de PR, `main` protégée.
- **Apport au projet** : `.gitignore`, `.sdkmanrc`, `.github/pull_request_template.md`, une règle de protection sur `main`, et les deux premières PR mergées (0.3 puis 0.2).
- **Durée indicative** : une demi-journée, en deux cycles. Vous pouvez vous arrêter à chaque "point d'arrêt".
- **Branche** : `ch00/03-conventions`.

Avancement :

- [x] A1. Créer la branche
- [x] A2. Voir ce que votre `.gitignore` global cache
- [x] A3. Écrire `.gitignore` (avec une erreur volontaire)
- [x] A4. Déplacer la règle `sandbox/`
- [x] A5. Committer `.gitignore` et `.sdkmanrc`
- [x] A6. Écrire le modèle de PR
- [x] B1. Régler les méthodes de merge
- [x] B2. Protéger `main`
- [x] B3. Tenter un push direct sur `main`
- [x] B4. Ouvrir et merger la PR de l'étape 0.3
- [x] B5. Ouvrir et merger la PR de l'étape 0.2
- [x] B6. Nettoyer les branches locales
- [x] Validation en session

Astuce : `Ctrl+Shift+O` affiche le plan du document.

---

## Cycle A - Les fichiers du dépôt

### Théorie

Vous connaissez `.gitignore`. Cette partie ne couvre que ce qui change pour un projet Java, et un piège que votre machine rend concret.

#### Trois endroits pour ignorer un fichier

Même logique qu'à l'étape 0.2 pour les réglages VSCode : utilisateur, projet, local.

- **Global** (`core.excludesFile`, chez vous `~/.gitignore`) : vos fichiers de machine et d'outils, pour tous les dépôts.
- **Projet** (`.gitignore`, versionné) : ce que tout contributeur doit ignorer.
- **Local au dépôt** (`.git/info/exclude`, non versionné) : une exception pour vous seul, dans ce dépôt seulement.

Ordre de priorité, du plus fort au plus faible : `.gitignore` du projet, puis `.git/info/exclude`, puis le global.

Conséquence : un `.gitignore` de projet peut **annuler** une règle globale avec une négation (`!`).

Une règle d'ignore ne concerne que les fichiers **non suivis**. Un fichier déjà committé reste suivi, même s'il correspond à une règle.

#### Ce qui change dans un projet Maven

- **Pas de `vendor/`** : Maven télécharge les dépendances dans `~/.m2/repository`, partagé par tous vos projets. Rien à ignorer dans le dépôt.
- **`target/`** : tout ce que Maven produit (classes compilées, jar, rapports de tests). L'équivalent de `var/` : supprimable à tout moment, recréé par le build.
- **Fichiers de l'IDE** : on ignore le dossier `.vscode/`, sauf les deux fichiers partagés de l'étape 0.2.

#### Ré-inclure un fichier : la règle du dossier parent

Documentation de git : "It is not possible to re-include a file if a parent directory of that file is excluded."

Concrètement :

- `/public/uploads/` exclut le **dossier**. Git n'y entre plus : une négation sur un fichier à l'intérieur est sans effet.
- `/public/uploads/*` exclut le **contenu** du dossier. Git y entre, et la négation fonctionne.

#### La commande qui explique tout : `git check-ignore -v`

`git check-ignore -v <chemin>` affiche quelle règle s'applique à un chemin : fichier, numéro de ligne, motif.

Un chemin sans règle n'est pas affiché. Le fichier n'a pas besoin d'exister.

#### Exemple 1 : `.gitignore` d'un projet Symfony

Préparation, dans un dépôt jetable :

```bash
mkdir -p ~/Sites/spring-boot/sandbox/0.3
cd ~/Sites/spring-boot/sandbox/0.3
git init shop
cd shop
mkdir -p vendor/symfony var/cache public/uploads
touch vendor/symfony/x.php var/cache/a .env .env.local public/uploads/.gitkeep public/uploads/photo.jpg
```

Fichier `sandbox/0.3/shop/.gitignore` :

```gitignore
/vendor/
/var/
/.env.local
/public/uploads/*
!/public/uploads/.gitkeep
```

Lecture ligne par ligne :

1. `/vendor/` : le `/` initial ancre le motif à la racine du dépôt ; le `/` final ne vise que les dossiers.
2. `/.env.local` : un seul fichier, à la racine.
3. `/public/uploads/*` : le contenu du dossier, pas le dossier lui-même.
4. `!/public/uploads/.gitkeep` : la négation ré-inclut ce fichier. Elle fonctionne grâce au `*` de la ligne précédente.

Avant d'exécuter, prédisez : quels chemins seront affichés ? `.env` le sera-t-il ?

```bash
git check-ignore -v vendor/symfony/x.php .env.local public/uploads/photo.jpg public/uploads/.gitkeep .env
```

Sortie :

```console
.gitignore:1:/vendor/	vendor/symfony/x.php
.gitignore:3:/.env.local	.env.local
.gitignore:4:/public/uploads/*	public/uploads/photo.jpg
.gitignore:5:!/public/uploads/.gitkeep	public/uploads/.gitkeep
```

Lecture :

- Chaque ligne donne : fichier de règles, numéro de ligne, motif, puis le chemin testé.
- `.gitkeep` est affiché avec un motif `!` : il correspond à une négation, donc il **n'est pas** ignoré.
- `.env` n'est pas affiché : aucune règle ne le concerne.

Puis :

```bash
git status --short -uall
```

Sortie :

```console
?? .env
?? .gitignore
?? public/uploads/.gitkeep
```

`-uall` liste les fichiers un par un au lieu de résumer par dossier.

#### Fixer la version du JDK : `.sdkmanrc`

Créé à l'étape 0.1, il reste à le committer. Équivalent : `.tool-versions` d'asdf, ou `.php-version`.

Avec `sdkman_auto_env=true` (votre cas), un `cd` dans le dépôt bascule sur ce JDK.

Vous ignorez `.php-version` dans votre `.gitignore` global : ici, le choix inverse a été fait (décision du 2026-11-16 dans le plan). La version du JDK fait partie du projet.

#### Le modèle de PR

GitHub lit le fichier `.github/pull_request_template.md` et pré-remplit la description de chaque nouvelle PR.

Il le lit sur la **branche par défaut** (`main`). Tant que le fichier n'y est pas mergé, il ne sert à rien.

#### Exemple 2 : modèle de PR d'un projet Symfony

Fichier `.github/pull_request_template.md` :

```markdown
## Quoi

<!-- Ce que change la PR, en une ou deux phrases. -->

## Pourquoi

<!-- Le besoin. Lien vers l'issue s'il y en a une. -->

## Comment c'est testé

<!-- Tests ajoutés, commandes à lancer, résultat attendu. -->

## Vérifications

- [x] `composer ci` passe en local
- [x] Le titre de la PR suit Conventional Commits
```

Lecture ligne par ligne :

1. `## Quoi`, `## Pourquoi`, `## Comment c'est testé` : des titres que l'auteur garde et remplit.
2. `<!-- ... -->` : un commentaire HTML. Visible pendant la rédaction, invisible une fois la PR affichée. Idéal pour les consignes.
3. `- [ ]` : une case à cocher, cliquable dans la PR affichée.

Un modèle court est rempli ; un modèle long est survolé.

### Pièges

- **Négation sous un dossier exclu** : `.vscode/` puis `!.vscode/settings.json` ne ré-inclut rien. Il faut `.vscode/*`.
- **Fichiers déjà suivis** : ajouter une règle ne retire pas un fichier committé. Il faut `git rm --cached <fichier>`.
- **`.gitignore` global trop large** : il agit sans bruit sur tous vos dépôts. Vous le vérifiez en A2.
- **Changement de branche** : la branche de cette étape part de `origin/main`, qui n'a pas encore `.vscode/`. Le dossier disparaît le temps de l'étape, et réapparaît après le merge de la PR 0.2.

### Vérifiez votre compréhension

1. Une règle pour les fichiers d'un outil que vous seul utilisez : `.gitignore` du projet ou global ? Pourquoi ?
2. Pourquoi n'y a-t-il pas de `vendor/` à ignorer dans un projet Maven ?
3. Que faut-il pour qu'un `.gitignore` de projet annule une règle de votre `.gitignore` global ?

---

### Pratique

#### A1. Créer la branche

Durée : 2 min.

```bash
cd ~/Sites/spring-boot
git fetch
git switch -c ch00/03-conventions origin/main
```

**Résultat attendu** : `git status` affiche la nouvelle branche, avec `.sdkmanrc` non suivi. Les fichiers non suivis suivent le changement de branche.

#### A2. Voir ce que votre `.gitignore` global cache

Durée : 5 min.

Deux fichiers que le projet versionnera plus tard :

- une migration Flyway (chapitre 4) ;
- un fichier `.http` pour httpYac (chapitre 5).

Avant d'exécuter, prédisez : seront-ils ignorés ?

```bash
git check-ignore -v src/main/resources/db/migration/V1__init.sql http/recipes.http
```

**Résultat attendu** :

```console
/home/vincent/.gitignore:26:*.sql	src/main/resources/db/migration/V1__init.sql
/home/vincent/.gitignore:86:*.http	http/recipes.http
```

Les deux sont ignorés par votre `.gitignore` global. Sans rien faire, la première migration n'aurait jamais été committée, sans aucun message.

Deux corrections possibles :

1. retirer ces règles du global (mais elles servent peut-être à d'autres projets) ;
2. les annuler dans le `.gitignore` du projet, par une négation.

La 2 est recommandée : c'est le projet qui sait que ces fichiers sont versionnés, et elle protège aussi un autre contributeur qui aurait les mêmes règles.

#### A3. Écrire `.gitignore`

Durée : 15 min. Modèle : exemple 1. Erreur volontaire.

Les règles à écrire :

- `target/` ;
- le dossier `.vscode/`, sauf `settings.json` et `extensions.json` ;
- la négation des deux règles globales vues en A2, avec une ligne de commentaire (`#`) qui explique pourquoi.

Pour le dossier `.vscode/`, écrivez d'abord la version fausse, volontairement :

```gitignore
.vscode/
!.vscode/settings.json
!.vscode/extensions.json
```

Puis vérifiez :

```bash
git check-ignore -v .vscode/settings.json .vscode/launch.json
```

Observez la règle affichée pour `settings.json`. Corrigez avec le piège "Négation sous un dossier exclu", puis relancez la commande.

**Résultat attendu**, après correction :

- `settings.json` est affiché avec la règle `!.vscode/settings.json` ;
- `launch.json` est affiché avec la règle qui exclut le contenu de `.vscode/` ;
- la commande de A2 affiche vos deux négations, avec `.gitignore` au lieu de `/home/vincent/.gitignore`.

#### A4. Déplacer la règle `sandbox/`

Durée : 5 min.

Aujourd'hui, `sandbox/` est ignoré par `.git/info/exclude`, donc pour vous seul.

1. Ajoutez `sandbox/` au `.gitignore`.
2. Retirez la ligne `sandbox/` de `.git/info/exclude` (gardez les commentaires).
3. Vérifiez :

   ```bash
   git check-ignore -v sandbox/0.1/HelloWorld.java
   git status --short
   ```

**Résultat attendu** : la règle vient de `.gitignore`, et `git status` ne liste que `.gitignore` et `.sdkmanrc`.

#### A5. Committer `.gitignore` et `.sdkmanrc`

Durée : 5 min.

Vérifiez d'abord que `.sdkmanrc` fonctionne :

```bash
cd ..
cd spring-boot
```

**Résultat attendu** : `Using java version 25.0.5.0+8-tem in this shell.`

Puis deux commits, un par sujet :

```bash
git add .gitignore
git commit -m "chore: add .gitignore for Maven and VSCode"
git add .sdkmanrc
git commit -m "build: pin JDK version with SDKMAN"
```

> **Point d'arrêt** : vous pouvez faire une pause ici.

#### A6. Écrire le modèle de PR

Durée : 15 min. Modèle : exemple 2.

Créez `.github/pull_request_template.md`.

Contenu attendu :

- les trois sections "Quoi", "Pourquoi", "Comment c'est testé", avec une consigne en commentaire HTML pour chacune ;
- une section "Vérifications" de deux ou trois cases, vraies dès aujourd'hui. Par exemple : titre Conventional Commits, périmètre de l'étape respecté.

`./mvnw verify` n'existe pas encore : la case viendra au chapitre 1.

```bash
git add .github/pull_request_template.md
git commit -m "docs: add pull request template"
```

**Résultat attendu** : `git log --oneline origin/main..` affiche trois commits.

> **Point d'arrêt** : le cycle A est terminé.

---

## Cycle B - GitHub Flow et protection de `main`

### Théorie

#### Ce que vous savez déjà

GitHub Flow : une branche par changement, une PR, une revue, un merge sur `main`, qui reste toujours livrable. Rien de propre à Java ici.

Ce qui est nouveau dans ce dépôt : la méthode de merge, et une protection que même vous ne pouvez pas contourner.

#### Merger en "squash"

Trois méthodes de merge sur GitHub :

- **merge commit** : garde tous les commits de la branche, plus un commit de merge ;
- **rebase** : rejoue les commits de la branche sur `main` ;
- **squash** : réduit toute la branche en **un seul commit** sur `main`.

Le choix pour ce dépôt : squash seul. Pourquoi :

- vous committez souvent sur la branche, comme points de sauvegarde, sans soigner chaque message ;
- `main` garde un commit par PR, donc un commit par étape : l'historique se lit comme le plan du cours.

Conséquence : le **titre de la PR** devient le message du commit sur `main`. Il doit donc suivre Conventional Commits.

GitHub ajoute le numéro de la PR au titre, par exemple : `build: add shared VSCode configuration and EditorConfig (#3)`. On remonte ainsi du commit à la discussion.

#### Rulesets : protéger `main`

Une **ruleset** est un ensemble de règles appliquées à des branches. Elle remplace l'ancienne "branch protection" : même rôle, plus lisible, et la liste des exceptions est explicite.

Les règles retenues :

- **Restrict deletions** : `main` ne peut pas être supprimée.
- **Block force pushes** : l'historique de `main` ne peut pas être réécrit.
- **Require a pull request before merging** : aucun push direct. Zéro approbation requise : sur GitHub, on ne peut pas approuver sa propre PR.

La **bypass list** reste vide : la règle s'applique aussi aux administrateurs, donc à vous.

Le statut de la CI sera ajouté à cette ruleset à l'étape 2.3.

Conséquence pour Claude : il pousse avec votre compte, il est donc bloqué lui aussi. Les documents du cours passeront par une PR, comme le code.

### Pièges

- **Le modèle n'apparaît pas** dans la PR qui l'ajoute : il n'est pas encore sur `main`. Il servira dès la PR suivante.
- **`git branch -d` refuse** de supprimer une branche mergée en squash : le commit sur `main` est nouveau, git ne reconnaît pas les commits de la branche. Utilisez `git branch -D` une fois la PR mergée.
- **Branche créée depuis `origin/main`** : elle suit `origin/main` comme branche amont. Un simple `git push` est refusé (les noms diffèrent). Le premier push se fait avec `git push -u origin HEAD`.

### Vérifiez votre compréhension

1. Pourquoi le titre de la PR compte-t-il plus que les messages des commits de la branche ?
2. Pourquoi zéro approbation requise, et pas une ?
3. Que se passerait-il si votre compte était dans la bypass list ?

---

### Pratique

#### B1. Régler les méthodes de merge

Durée : 5 min.

```bash
gh repo edit --enable-squash-merge --squash-merge-commit-message pr-title --enable-merge-commit=false --enable-rebase-merge=false --delete-branch-on-merge
```

Lecture :

- `--enable-squash-merge` et les deux `=false` : seul le squash reste autorisé ;
- `--squash-merge-commit-message pr-title` : le commit squashé reprend le titre de la PR, sans corps ;
- `--delete-branch-on-merge` : la branche distante est supprimée après le merge.

**Résultat attendu** :

```bash
gh api repos/vincentchalamon/spring-boot --jq '{squash: .allow_squash_merge, merge: .allow_merge_commit, rebase: .allow_rebase_merge, delete: .delete_branch_on_merge, title: .squash_merge_commit_title}'
```

affiche `squash` et `delete` à `true`, `merge` et `rebase` à `false`, `title` à `PR_TITLE`.

#### B2. Protéger `main`

Durée : 10 min.

Dans le navigateur, sur le dépôt :

1. Settings, puis Rules, puis Rulesets.
2. New ruleset, puis New branch ruleset.
3. Ruleset name : `main`. Enforcement status : Active.
4. Bypass list : ne rien ajouter.
5. Target branches : Add target, puis Include default branch.
6. Rules : cochez Restrict deletions, Block force pushes, Require a pull request before merging.
7. Dans Require a pull request : Required approvals à 0 ; Allowed merge methods : Squash seul.
8. Create.

**Résultat attendu** :

```bash
gh api repos/vincentchalamon/spring-boot/rulesets --jq '.[] | {name, enforcement}'
```

affiche `{"enforcement":"active","name":"main"}`.

#### B3. Tenter un push direct sur `main`

Durée : 5 min. Erreur volontaire.

Depuis la branche `ch00/03-conventions`, poussez vos commits directement sur `main` :

```bash
git push origin HEAD:main
```

`HEAD:main` signifie : "envoie mon commit courant sur la branche `main` distante".

**Résultat attendu** : le push est refusé, avec un message `GH013: Repository rule violations found for refs/heads/main` et la règle violée.

Si le push passe : la ruleset n'est pas active. Dites-le en session avant d'aller plus loin.

#### B4. Ouvrir et merger la PR de l'étape 0.3

Durée : 15 min.

1. Poussez la branche :

   ```bash
   git push -u origin HEAD
   ```

2. Le modèle n'est pas encore sur `main` : remplissez-le à la main, dans un fichier du bac à sable.

   ```bash
   mkdir -p sandbox/0.3
   cp .github/pull_request_template.md sandbox/0.3/pr-0.3.md
   ```

   Remplissez `sandbox/0.3/pr-0.3.md`.

3. Ouvrez la PR :

   ```bash
   gh pr create --title "chore: repository conventions" --body-file sandbox/0.3/pr-0.3.md
   ```

4. Relisez-la sur GitHub, onglet "Files changed" : `gh pr view --web`.
5. Dites-le en session : Claude relit la PR avant le merge.
6. Mergez :

   ```bash
   gh pr merge --squash
   ```

**Résultat attendu** : `git fetch && git log --oneline -1 origin/main` affiche `chore: repository conventions`, suivi du numéro de la PR entre parenthèses.

> **Point d'arrêt** : vous pouvez faire une pause ici.

#### B5. Ouvrir et merger la PR de l'étape 0.2

Durée : 10 min.

1. Revenez sur la branche de l'étape 0.2 et poussez-la :

   ```bash
   git switch ch00/02-vscode
   git push -u origin HEAD
   ```

2. Ouvrez la PR dans le navigateur, pour voir le modèle s'appliquer :

   ```bash
   gh pr create --web
   ```

3. Titre : celui du commit, `build: add shared VSCode configuration and EditorConfig`. Remplissez la description.
4. Claude relit, puis vous mergez : bouton "Squash and merge", ou `gh pr merge --squash`.

**Résultat attendu** : la description est pré-remplie avec votre modèle.

#### B6. Nettoyer les branches locales

Durée : 5 min.

```bash
git switch main
git pull
git fetch --prune
git branch -D ch00/02-vscode ch00/03-conventions
```

Lecture :

- `git fetch --prune` retire les références des branches distantes supprimées après le merge ;
- `-D` et pas `-d` : voir le piège "`git branch -d` refuse".

**Résultat attendu** : `git log --oneline -3` affiche vos deux commits squashés en tête, et `.vscode/` est de retour.

> **Point d'arrêt** : la pratique est terminée. Il reste la validation, en session.

---

## Validation

En session, Claude :

- relit les deux PR ;
- vérifie les points ci-dessous ;
- vous pose les questions "expliquez".

Points vérifiables :

- [x] `git check-ignore -v` sur `sandbox/x`, `target/x` et `.vscode/launch.json` : ignorés par `.gitignore`.
- [x] `git check-ignore -v` sur `.vscode/settings.json`, une migration `.sql` et un fichier `.http` : affichés avec une négation.
- [x] `.git/info/exclude` ne contient plus `sandbox/`.
- [x] Un push direct sur `main` est refusé.
- [x] Deux PR mergées en squash, chacune un seul commit sur `main`, au format Conventional Commits.

Questions "expliquez" :

1. Pourquoi `.vscode/` suivi de `!.vscode/settings.json` ne fonctionne-t-il pas ?
2. Pourquoi la PR de l'étape 0.3 n'avait-elle pas sa description pré-remplie ?
3. Pourquoi `git branch -d` refuse-t-il de supprimer une branche mergée en squash ?

---

## Documentation

- gitignore (motifs, priorités, négation) : https://git-scm.com/docs/gitignore
- `git check-ignore` : https://git-scm.com/docs/git-check-ignore
- Modèles `.gitignore` Java et Maven : https://github.com/github/gitignore
- SDKMAN, `.sdkmanrc` : https://sdkman.io/usage#env-command
- Modèle de PR : https://docs.github.com/en/communities/using-templates-to-encourage-useful-issues-and-pull-requests/creating-a-pull-request-template-for-your-repository
- Méthodes de merge : https://docs.github.com/en/repositories/configuring-branches-and-merges-in-your-repository/configuring-pull-request-merges/about-merge-methods-on-github
- Rulesets : https://docs.github.com/en/repositories/configuring-branches-and-merges-in-your-repository/managing-rulesets/about-rulesets
- GitHub Flow : https://docs.github.com/en/get-started/using-github/github-flow

## Pour aller plus loin (facultatif)

- **Require linear history** : une règle de plus, qui interdit les commits de merge sur `main`. Redondante ici avec "squash seul", utile si vous autorisez un jour le rebase.
- **Mettre la ruleset en code** : `gh api repos/vincentchalamon/spring-boot/rulesets/<id>` renvoie la ruleset en JSON. On peut la sauvegarder et la recréer avec `gh api -X POST ... --input fichier.json`.
- **Auto-merge** : `gh pr merge --auto --squash` merge la PR dès que la CI est verte. À activer d'abord sur le dépôt (`gh repo edit --enable-auto-merge`). Intéressant à partir du chapitre 2.
