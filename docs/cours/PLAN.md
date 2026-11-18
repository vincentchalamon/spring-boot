# Plan du cours Java / Spring Boot

Construire, étape par étape, l'API REST du gestionnaire de recettes décrit dans le [cahier des charges](../cahier-des-charges.md), pour apprendre **Java moderne** puis **Spring Boot**, avec tests, qualité et CI dès le départ.

- **Public** : développeur Symfony / API Platform expert, débutant en Java.
- **Rythme** : temps plein, avec des interruptions. Les durées sont des estimations d'effort, pas un calendrier : une étape peut s'étaler sur plusieurs sessions et plusieurs jours.
- **Durée estimée** : environ 22,5 jours d'effort, plus le bonus.
- **Où reprendre** : le [journal](JOURNAL.md), entrée la plus récente en haut.
- **Détail** : un dossier par chapitre (`chapitre-XX/README.md`) et un fichier par étape (`etape-X.Y.md`), rédigés par Claude au démarrage.

## Comment travailler

1. Ouvrir une session Claude dans le dépôt : `cd ~/Sites/spring-boot && claude`.
2. Demander "On reprend" ou "On attaque l'étape X.Y". Claude fait le point de reprise (journal), pose 2 ou 3 questions de rappel, puis fixe l'objectif de la session.
3. Lire la théorie de l'étape en exécutant les exemples, puis faire la pratique.
4. Exercices temporaires dans `sandbox/` : jamais committés, vérifiés par Claude en local.
5. Code du projet sur la branche `chXX/YY-slug`, créée depuis `origin/main`. Committer souvent (Conventional Commits).
6. Ouvrir la PR : la CI et la revue Claude (à partir du chapitre 2) tournent. Corriger, faire valider, merger.
7. Fin de session : Claude pose les questions "expliquez", puis note dans le journal ce qui est fait, appris, flou, et la prochaine action.

Modèle d'une étape :

- **En bref** : objectif, apport au projet, durée, avancement ;
- **Théorie** : ce qui change par rapport à PHP / Symfony, concepts, exemples complets exécutés, pièges, questions ;
- **Pratique** : sous-étapes courtes sur le code du projet, avec modèle, résultat attendu et points d'arrêt ;
- **Validation** : critères vérifiables et questions "expliquez".

Une étape chargée enchaîne deux cycles théorie puis pratique.

Accompagnement dégressif (détail dans `CLAUDE.md`) :

- **Guidé** (chapitres 0 à 2) : un exemple complet à adapter pour chaque exercice ;
- **Semi-guidé** (chapitres 3 à 6) : exemples pour la syntaxe et les API nouvelles seulement ;
- **Autonome** (chapitres 7 à 10) : objectifs, notions, pièges, documentation.

Chaque phase se termine par un **bilan** : une vraie fonctionnalité du projet, réalisée sans exemple, qui fixe le niveau d'accompagnement de la suite.

Syntaxe de référence : [mémo Java pour un développeur PHP](memo-java-pour-php.md), complété au fil des chapitres.

## Avancement

| Chapitre | Phase | Jours | Statut |
|---|---|---|---|
| 0. Environnement et dépôt | guidée | 1 | terminé |
| 1. Java moderne, sans framework | guidée | 2 | en cours |
| 2. Qualité et CI | guidée | 1,5 | à faire |
| Bilan 1. Étapes de recette | bilan | 0,5 | à faire |
| 3. Fondamentaux Spring Boot | semi-guidée | 2 | à faire |
| 4. Persistance | semi-guidée | 2,5 | à faire |
| 5. API REST : lecture | semi-guidée | 2 | à faire |
| 6. Contrat OpenAPI | semi-guidée | 1 | à faire |
| Bilan 2. Autocomplétion | bilan | 1 | à faire |
| 7. API REST : écriture | autonome | 2 | à faire |
| 8. Sécurité | autonome | 2 | à faire |
| 9. Images | autonome | 1,5 | à faire |
| 10. Observabilité et packaging | autonome | 2 | à faire |
| Bilan 3. "Dans mon frigo" | bilan | 1,5 | à faire |
| Bonus. Serveur MCP | autonome | 1,5 | optionnel |

---

## Chapitre 0 - Environnement et dépôt

Apport au projet : configuration VSCode partagée, `.gitignore`, modèle de PR, protection de `main`. Détail : [chapitre-00](chapitre-00/README.md).

- [x] **0.1 JDK et outils en ligne de commande**
  - Objectif : disposer d'un JDK et des CLI du projet.
  - Notions : JDK vs JRE, distributions (Temurin), SDKMAN (équivalent de phpenv / asdf), `JAVA_HOME`, `java`, `javac`, `jshell` (REPL, équivalent `php -a`).
  - Livrable : JDK 25 Temurin installé via SDKMAN ; `docker`, `gh`, `httpie` disponibles.
  - Validation : `java -version` et `javac -version` affichent 25 ; un premier `HelloWorld.java` exécuté avec `java HelloWorld.java` (lancement direct d'un fichier source) ; un essai dans `jshell`.

- [x] **0.2 VSCode pour Java**
  - Objectif : un IDE productif et une configuration partagée via le dépôt.
  - Notions : Language Server Java (Eclipse JDT LS), extensions, réglages de workspace.
  - Extensions : Extension Pack for Java (Red Hat, Microsoft), Spring Boot Extension Pack, Container Tools, httpYac (fichiers `.http` versionnés), SonarQube for IDE, EditorConfig, GitLens, XML (Red Hat, pour `pom.xml`), YAML.
  - Livrable : `.vscode/extensions.json` (recommandations), `.vscode/settings.json` (formatage et organisation des imports à l'enregistrement ; le JDK reste dans les réglages utilisateur), `.editorconfig`.
  - Validation : VSCode propose les extensions à l'ouverture ; le `HelloWorld` se lance et se débogue depuis l'IDE (point d'arrêt atteint).

- [x] **0.3 Conventions du dépôt**
  - Objectif : poser le cadre GitHub Flow avant d'écrire du code.
  - Notions : protection de branche, modèle de PR, `.gitignore` Java/Maven/VSCode.
  - Livrable : `.gitignore` (dont `sandbox/`), `.sdkmanrc` (créé en 0.1), `.github/pull_request_template.md`, protection de `main` (PR obligatoire ; le statut CI requis sera ajouté au chapitre 2).
  - Validation : un push direct sur `main` est refusé ; première PR mergée.

## Chapitre 1 - Java moderne, sans framework

Apport au projet : le projet Maven et les types de valeur de la recette (type, régime, unité, quantité, ingrédient), en Java pur et testés, réutilisés tels quels au chapitre 4 (`@Embeddable`, enums JPA). La mise à l'échelle des portions et la conversion métrique/US sont à la charge du front (hors-ligne) : l'API stocke et expose des quantités structurées en unités canoniques. Détail : [chapitre-01](chapitre-01/README.md).

Fil rouge `Quantity` : la même valeur est réécrite à chaque étape avec un meilleur outil (classe classique en 1.0, projet Maven et tests en 1.1, record avec `BigDecimal` et enum `Unit` en 1.2). Elle devient ensuite un composant de `Ingredient` (1.3), un `@Embeddable` (4.2), un champ des DTO JSON (5.1, 7.1) et un schéma du contrat OpenAPI (6.1).

- [x] **1.0 Syntaxe de base**
  - Objectif : écrire de petits programmes Java avant d'aborder Maven.
  - Notions : types primitifs et objets, `String`, opérateurs, conditions, boucles, tableaux, méthodes, classes (champs, constructeur, `this`, visibilité), `equals` / `hashCode` / `toString` écrits à la main, exceptions (`throw`, `try` / `catch`), conversion texte vers nombre ; lire une Javadoc.
  - Livrable : `Quantity` en classe classique (valeur `double`, unité `String`, validation dans le constructeur) et un `main` de démonstration qui lit `250 g` en argument ; dans `sandbox/1.0/`, vérifié en session.
  - Validation : programme fonctionnel, y compris sur une entrée invalide ; Vincent retrouve seul dans la Javadoc la méthode adaptée à un besoin donné.

- [x] **1.1 Projet Maven à la main**
  - Objectif : comprendre Maven avant que Spring ne le cache.
  - Notions : `pom.xml` (équivalent de `composer.json`), coordonnées `groupId:artifactId:version`, cycle de vie (`compile`, `test`, `package`, `verify`), arborescence `src/main/java` / `src/test/java`, packages (équivalent des namespaces PSR-4), Maven Wrapper (`mvnw`, équivalent d'un binaire Composer versionné), `maven-compiler-plugin` et `release`.
  - Notions complémentaires : packaging `jar` (`./mvnw package`).
  - Livrable : projet Maven avec Wrapper, JUnit 6 et AssertJ ; la classe `Quantity` de l'étape 1.0 déplacée dans son package, avec ses premiers tests (dont un qui révèle le problème des `double`).
  - Validation : `./mvnw verify` passe ; les tests sont exécutés depuis VSCode (Test Runner).

- [x] **1.2 Enums et records : type, régime, unité, quantité**
  - Objectif : modéliser des valeurs immuables et sûres.
  - Notions : `enum` avec attributs et méthodes ; `record` et constructeur compact (invariants) ; `equals` / `hashCode` / `toString` ; `==` vs `equals` ; `BigDecimal` vs `double` (précision, échelle, `compareTo` vs `equals`) ; exceptions non vérifiées (`IllegalArgumentException`) ; nullité (JSpecify `@NullMarked`, `@Nullable`, utilisés par Spring 7) ; tests paramétrés `@ParameterizedTest` / `@CsvSource` (équivalent des `dataProvider` PHPUnit), `assertThatThrownBy`.
  - Livrable : enums `RecipeType` et `Diet` ; enum `Unit` (`GRAM`, `MILLILITER`, `TEASPOON`, `TABLESPOON` et unités non convertibles : pièce, pincée, etc.) ; `Quantity` réécrite en record (`BigDecimal` + `Unit`, invariants validés), en comparant avec la classe de l'étape 1.0.
  - Validation : tests unitaires des invariants (valeur nulle ou négative refusée, égalité de `1.0` et `1.00`).

- [ ] **1.3 Ingrédients, collections et streams**
  - Objectif : manipuler des listes de valeurs à la manière idiomatique de Java.
  - Notions : `List`, `Map`, `Set`, `List.of` / `List.copyOf` (immutabilité, copie défensive), génériques, Streams API (`map`, `filter`, `toList`, `Collectors.groupingBy`), `Optional` (quand l'utiliser, quand l'éviter), `var`, text blocks ; quantité optionnelle ("sel, poivre").
  - Livrable : record `Ingredient` (nom + précision optionnelle + quantité optionnelle) ; une règle de validation écrite avec les streams (ingrédient en double dans une liste).
  - Validation : tests ; Vincent explique en revue la différence entre `List.of`, `Arrays.asList` et `new ArrayList<>()`.

## Chapitre 2 - Qualité et CI

Apport au projet : formatage, analyse statique, couverture, tests de mutation, CI et revue automatique sur chaque PR.

- [ ] **2.1 Formatage et analyse statique**
  - Objectif : faire porter le style et les erreurs courantes par l'outillage.
  - Notions : Spotless + google-java-format ou palantir-java-format (équivalent de PHP-CS-Fixer) ; Error Prone, avec NullAway en option (équivalent de PHPStan) ; Maven Enforcer (version de Java et de Maven) ; plugins Maven et phases.
  - Livrable : configuration dans le `pom.xml` ; `./mvnw verify` échoue sur un code mal formaté ou une erreur détectée.
  - Validation : démonstration d'un échec puis d'un succès.

- [ ] **2.2 Couverture et tests de mutation**
  - Objectif : mesurer la qualité des tests, pas seulement leur présence.
  - Notions : JaCoCo (rapport, seuil minimal) ; PIT (équivalent d'Infection) : mutants tués ou survivants, score de mutation ; Surefire vs Failsafe (tests unitaires vs tests d'intégration).
  - Livrable : rapport JaCoCo avec seuil ; PIT exécutable (`./mvnw ... pitest:mutationCoverage`) sur la bibliothèque de quantités.
  - Validation : score de mutation analysé ; au moins un test ajouté pour tuer un mutant survivant.

- [ ] **2.3 CI GitHub Actions et Dependabot**
  - Objectif : chaque PR est vérifiée automatiquement.
  - Notions : workflow, `actions/setup-java` (Temurin, cache Maven), `./mvnw verify`, statut requis dans la protection de `main`, rapports en artefacts ; Dependabot (`maven` + `github-actions`).
  - Livrable : `.github/workflows/ci.yml`, `.github/dependabot.yml`, statut CI requis sur `main`.
  - Validation : une PR rouge est bloquée, une PR verte est mergeable.

- [ ] **2.4 Revue de code par Claude**
  - Objectif : une revue pédagogique automatique sur chaque PR.
  - Notions : `anthropics/claude-code-action` (installé via `/install-github-app`), secrets GitHub (clé API ou jeton `claude setup-token`), déclencheurs (`pull_request` : `opened`, `ready_for_review` ; mention `@claude` dans un commentaire), permissions minimales (lecture du contenu, écriture des commentaires de PR uniquement), prompt qui impose les règles de revue de `CLAUDE.md` (expliquer sans corriger).
  - Livrable : `.github/workflows/claude-review.yml`.
  - Validation : une PR de test reçoit une revue conforme (pas de code corrigé) ; une mention `@claude` déclenche une nouvelle revue.

## Bilan 1 - Étapes de recette (fin de la phase guidée)

- [ ] **Bilan 1 Record `Step`**
  - Objectif : vérifier, sans exemple fourni, les acquis des chapitres 1 et 2 ; fixer le niveau d'accompagnement du chapitre 3.
  - Notions mobilisées : record et invariants, `Optional`, collections, streams, tests paramétrés, chaîne qualité complète. Petite nouveauté à découvrir seul dans la Javadoc : `java.time.Duration`.
  - Livrable : record `Step` (texte + durée optionnelle) et ses règles de validation, avec tests.
  - Validation : CI verte (formatage, Error Prone, seuil de couverture, score de mutation) ; revue au format PR ; grille de compétences (acquis, à consolider, non acquis) notée dans le journal.

## Chapitre 3 - Fondamentaux Spring Boot

Apport au projet : l'application Spring Boot, sa configuration typée, PostgreSQL en local via Compose.

- [ ] **3.1 Initialiser l'application**
  - Objectif : passer du projet Maven pur à une application Spring Boot.
  - Notions : Spring Initializr (start.spring.io ou l'extension VSCode) ; `spring-boot-starter-parent` et BOM ; starters (équivalent des recipes Flex) ; `@SpringBootApplication` ; auto-configuration (équivalent de la configuration par défaut des bundles) ; `./mvnw spring-boot:run`, Spring Boot Dashboard, DevTools.
  - Livrable : application Spring Boot 4.2 qui démarre et intègre la bibliothèque du chapitre 1.
  - Validation : l'application démarre ; `GET /actuator/health` renvoie `UP`.

- [ ] **3.2 Conteneur IoC et injection de dépendances**
  - Objectif : comprendre ce que Spring fait de vos classes.
  - Notions : interfaces (et pourquoi ne pas en créer pour une seule implémentation), beans, `@Component` / `@Service` / `@Repository`, injection par constructeur (pourquoi pas par champ), `@Configuration` / `@Bean`, scopes (singleton par défaut : conséquences sur l'état), cycle de vie, profils ; parallèle avec l'autowiring de `services.yaml`.
  - Livrable : un service applicatif injecté (ex. adaptation d'une liste d'ingrédients) et un test unitaire sans Spring.
  - Validation : Vincent explique la différence entre un test unitaire pur, `@SpringBootTest` et un test de slice.

- [ ] **3.3 Configuration**
  - Objectif : une configuration typée, validée et surchargeable.
  - Notions : `application.yaml`, profils (`dev`, `test`), `@ConfigurationProperties` avec records et validation, variables d'environnement, ordre de priorité des sources (équivalent de `%env()%` et des `.env`).
  - Livrable : une configuration applicative typée (ex. taille de page maximale) utilisée par un bean.
  - Validation : test qui vérifie le chargement et le rejet d'une valeur invalide.

- [ ] **3.4 Docker Compose et Actuator**
  - Objectif : environnement local reproductible en une commande.
  - Notions : `compose.yaml` (PostgreSQL), `spring-boot-docker-compose` (démarrage automatique en dev), Actuator (`health`, `info`, exposition des endpoints), premier test `@SpringBootTest`.
  - Livrable : `./mvnw spring-boot:run` démarre PostgreSQL et l'API.
  - Validation : `health` inclut l'état de la base de données.

## Chapitre 4 - Persistance

Apport au projet : le schéma versionné, l'entité `Recipe`, le repository et le service de lecture, testés contre un vrai PostgreSQL.

- [ ] **4.1 Migrations Flyway**
  - Objectif : schéma versionné, jamais généré par Hibernate.
  - Notions : Flyway (équivalent de Doctrine Migrations), nommage `V1__...sql`, `spring.jpa.hibernate.ddl-auto=validate`.
  - Livrable : migration du schéma des recettes (recette, ingrédients ordonnés, étapes ordonnées).
  - Validation : base recréée de zéro par Flyway au démarrage ; Hibernate valide le mapping.

- [ ] **4.2 Entité Recipe**
  - Objectif : mapper le modèle avec JPA.
  - Notions : `@Entity`, identifiant (UUID vs séquence), `@Enumerated(STRING)` (type, régime), `@Embeddable` (réutiliser `Quantity` / `Ingredient`), `@ElementCollection` + `@OrderColumn` pour les listes ordonnées, durée optionnelle d'une étape (`Duration`), température du four optionnelle (°C) ; contraintes JPA (constructeur sans argument, mutabilité) et pourquoi un record ne peut pas être une entité ; `equals` / `hashCode` d'une entité.
  - Livrable : entité `Recipe` complète (sauf image).
  - Validation : test de persistance aller-retour.

- [ ] **4.3 Repositories et transactions**
  - Objectif : accéder aux données sans piège de performance.
  - Notions : `JpaRepository`, requêtes dérivées, `@Query` JPQL, projections ; problème N+1, `@EntityGraph`, `spring.jpa.open-in-view=false` ; `@Transactional` (service, `readOnly`, propagation de base, proxy et appel interne) ; parallèle avec l'EntityManager et l'UnitOfWork de Doctrine.
  - Livrable : `RecipeRepository` et `RecipeService` (lecture par id, liste).
  - Validation : test qui compte les requêtes SQL et prouve l'absence de N+1.

- [ ] **4.4 Tests de persistance**
  - Objectif : tester contre un vrai PostgreSQL.
  - Notions : `@DataJpaTest`, Testcontainers 2 avec `@ServiceConnection`, réutilisation de conteneur, données de test (builder de test, équivalent de Foundry), isolation transactionnelle.
  - Livrable : tests d'intégration du repository.
  - Validation : tests verts en local et en CI (Docker disponible sur les runners).

## Chapitre 5 - API REST : lecture

Apport au projet : `GET /recipes/{id}` et `GET /recipes` (pagination, filtres, recherche).

- [ ] **5.1 Premier endpoint**
  - Objectif : `GET /recipes/{id}`.
  - Notions : `@RestController`, `@GetMapping`, `@PathVariable`, DTO de réponse (record) et mapping manuel entité vers DTO (pourquoi ne pas exposer l'entité), Jackson 3, erreurs RFC 9457 `ProblemDetail` (`spring.mvc.problemdetails`, `@RestControllerAdvice`), 404.
  - Livrable : endpoint et gestion d'erreur.
  - Validation : test `@WebMvcTest` avec `MockMvcTester` ; fichier `.http` exécutable depuis VSCode.

- [ ] **5.2 Liste paginée et tri**
  - Objectif : `GET /recipes?page=&size=&sort=`.
  - Notions : `Pageable`, `Page` vs `Slice`, `PagedModel` (sérialisation stable via `@EnableSpringDataWebSupport(pageSerializationMode = VIA_DTO)`), taille maximale, tri autorisé (liste blanche) ; comparaison avec la pagination d'API Platform.
  - Livrable : endpoint paginé.
  - Validation : tests des bornes (taille max, tri inconnu refusé).

- [ ] **5.3 Filtres**
  - Objectif : filtrer par type, régime, calories min / max.
  - Notions : binding des paramètres dans un record de critères, validation, `Specification` JPA (`JpaSpecificationExecutor`) composables ; comparaison avec les filtres d'API Platform.
  - Livrable : filtres combinables.
  - Validation : tests d'intégration sur un jeu de données.

- [ ] **5.4 Recherche libre**
  - Objectif : rechercher dans le titre, les ingrédients et le contenant.
  - Notions : recherche plein texte PostgreSQL (`tsvector`, configuration `french`, `unaccent`, index GIN, colonne générée par migration) vs `ILIKE` ; intégration à la `Specification` ; pertinence et tri.
  - Livrable : paramètre `q` combinable avec les filtres.
  - Validation : tests avec accents, pluriels et plusieurs mots ; premier test d'intégration complet (`@SpringBootTest` + `RestTestClient`).

## Chapitre 6 - Contrat OpenAPI

Apport au projet : la documentation de l'API et son contrat versionné, contrôlé en CI.

- [ ] **6.1 Documentation springdoc**
  - Objectif : une spécification OpenAPI fidèle et utile.
  - Notions : springdoc-openapi 3 (`/v3/api-docs`, Swagger UI), ce qui est inféré automatiquement et ce qu'il faut annoter (`@Operation`, `@Schema`, `@Tag`, `@ApiResponse`), documenter les `ProblemDetail` et la pagination ; comparaison avec la génération par API Platform.
  - Livrable : spécification complète pour les endpoints de lecture.
  - Validation : Swagger UI utilisable ; la spécification passe un linter (Spectral ou Redocly).

- [ ] **6.2 Contrat versionné**
  - Objectif : aucune modification du contrat sans le vouloir.
  - Notions : export de la spécification dans le dépôt (`openapi.yaml`), test ou étape de CI qui échoue si la spécification générée diffère de la version committée, détection des changements cassants (`oasdiff`).
  - Livrable : `openapi.yaml` versionné + contrôle en CI.
  - Validation : une modification d'un DTO sans mise à jour du contrat fait échouer la CI.

## Bilan 2 - Autocomplétion (fin de la phase semi-guidée)

- [ ] **Bilan 2 Endpoints d'autocomplétion**
  - Objectif : vérifier, sans exemple fourni, les acquis des chapitres 3 à 6 ; fixer le niveau d'accompagnement de la phase autonome.
  - Notions mobilisées : requête de repository et projection, service, contrôleur, DTO, `ProblemDetail`, tests de slice et d'intégration, contrat OpenAPI. Petite nouveauté à découvrir seul dans la documentation : l'en-tête `Cache-Control`.
  - Livrable : endpoints de référentiel pour le formulaire (types, régimes, contenants déjà utilisés).
  - Validation : CI verte (dont le contrôle du contrat) ; revue au format PR ; grille de compétences notée dans le journal.

## Chapitre 7 - API REST : écriture

Apport au projet : création, modification et suppression des recettes, avec validation.

- [ ] **7.1 Création**
  - Objectif : `POST /recipes`.
  - Notions : DTO de requête (record) distinct du DTO de réponse, Bean Validation (`@NotBlank`, `@Positive`, `@Size`, `@Valid` imbriqué), réponse `201 Created` + `Location`, erreurs 400 détaillées par champ en `ProblemDetail` (équivalent des violations du Validator Symfony).
  - Livrable : création d'une recette complète.
  - Validation : tests des cas valides et invalides.

- [ ] **7.2 Modification et suppression**
  - Objectif : `PUT /recipes/{id}` et `DELETE /recipes/{id}`.
  - Notions : sémantique PUT (remplacement complet, adapté au formulaire unique) ; pourquoi pas de PATCH ici ; `204 No Content` ; verrouillage optimiste (`@Version`) exposé via `ETag` / `If-Match` (`412 Precondition Failed`).
  - Livrable : modification et suppression.
  - Validation : test de modification concurrente.

- [ ] **7.3 Validation personnalisée**
  - Objectif : exprimer une règle métier comme contrainte de validation réutilisable.
  - Notions : contrainte personnalisée (annotation + `ConstraintValidator`, équivalent d'une Constraint Symfony), réutilisation de la règle "ingrédient en double" de l'étape 1.3.
  - Livrable : contrainte appliquée au DTO de création et de modification.
  - Validation : tests ; contrat OpenAPI mis à jour.

- [ ] **7.4 Tests bout en bout du cycle de vie**
  - Objectif : sécuriser l'ensemble du CRUD.
  - Notions : `@SpringBootTest(webEnvironment = RANDOM_PORT)`, `RestTestClient`, isolation des données entre tests, lisibilité des tests (given / when / then).
  - Livrable : scénarios création, consultation, modification, suppression.
  - Validation : CI verte ; score de mutation maintenu.

## Chapitre 8 - Sécurité

Apport au projet : lecture publique, écriture réservée aux administrateurs authentifiés par JWT.

- [ ] **8.1 Spring Security : les bases**
  - Objectif : lecture publique, écriture protégée.
  - Notions : chaîne de filtres (`SecurityFilterChain`), règles d'autorisation par méthode HTTP et chemin, API stateless (pas de session, CSRF désactivé et pourquoi), `401` vs `403` en `ProblemDetail` ; parallèle avec le firewall et l'`access_control` Symfony.
  - Livrable : configuration de sécurité.
  - Validation : tests avec `@WithMockUser`.

- [ ] **8.2 Administrateurs**
  - Objectif : stocker et vérifier les identifiants admin.
  - Notions : `UserDetailsService`, `PasswordEncoder` (`DelegatingPasswordEncoder`, BCrypt / Argon2), création du premier admin (commande au démarrage ou script ; choix discuté), aucun secret dans le dépôt.
  - Livrable : table des admins + création de l'admin initial.
  - Validation : tests du chargement et de la vérification du mot de passe.

- [ ] **8.3 Authentification JWT**
  - Objectif : `POST /auth/token` renvoie un JWT ; les endpoints d'écriture l'exigent.
  - Notions : `spring-boot-starter-oauth2-resource-server`, `JwtEncoder` / `JwtDecoder` (Nimbus), clé RSA fournie par configuration, claims (sujet, rôles, expiration), conversion des claims en autorités ; pourquoi pas de refresh token pour l'instant ; parallèle avec LexikJWTAuthenticationBundle.
  - Livrable : émission et vérification du JWT.
  - Validation : tests (jeton valide, expiré, signature invalide, rôle insuffisant).

- [ ] **8.4 CORS et documentation de la sécurité**
  - Objectif : API prête à être consommée par un front.
  - Notions : CORS (origines configurées par profil), schéma `bearer` dans OpenAPI, `.http` authentifié.
  - Livrable : configuration CORS + contrat à jour.
  - Validation : test de requête préliminaire (preflight) ; Swagger UI avec "Authorize".

## Chapitre 9 - Images

Apport au projet : une image par recette, envoyée, servie et supprimée avec la recette.

- [ ] **9.1 Upload**
  - Objectif : `PUT /recipes/{id}/image`.
  - Notions : `MultipartFile`, limites `spring.servlet.multipart.*`, validation du type réel (pas seulement l'extension) et de la taille, nom de fichier généré (traversée de répertoire), stockage sur le système de fichiers local dans un répertoire configuré (service simple, sans abstraction) ; parallèle avec VichUploader.
  - Livrable : upload sécurisé (admin).
  - Validation : tests avec `@TempDir` (fichier valide, trop gros, faux type).

- [ ] **9.2 Diffusion et cycle de vie**
  - Objectif : servir l'image et la supprimer avec la recette.
  - Notions : `ResponseEntity<Resource>`, `Content-Type`, cache HTTP (`ETag`, `Cache-Control`), suppression transactionnelle et cohérence fichier / base (ce qui peut échouer et comment l'accepter simplement).
  - Livrable : `GET /recipes/{id}/image`, suppression.
  - Validation : tests ; contrat OpenAPI (multipart et binaire) à jour.

## Chapitre 10 - Observabilité et packaging

Apport au projet : logs structurés, métriques, image OCI et CI complète.

- [ ] **10.1 Logs et métriques**
  - Objectif : une API diagnostiquable.
  - Notions : SLF4J / Logback, logs structurés natifs de Spring Boot (JSON, ECS / logfmt), niveaux par package, Actuator (`metrics`, groupes de health, exposition par profil), Micrometer.
  - Livrable : configuration de logs et d'Actuator par profil.
  - Validation : logs JSON en profil `prod` ; métriques HTTP consultables.

- [ ] **10.2 Image OCI**
  - Objectif : une image exécutable, sans déploiement.
  - Notions : `./mvnw spring-boot:build-image` (Buildpacks) vs Dockerfile multi-étapes (jar en couches) : comparaison et choix ; utilisateur non root ; `compose.yaml` complet (API + base de données) ; healthcheck.
  - Livrable : image construite et démarrée via Compose.
  - Validation : l'API répond dans le conteneur.

- [ ] **10.3 CI finale**
  - Objectif : une CI complète avant le bilan final.
  - Notions : construction de l'image en CI (sans push), scan de vulnérabilités (Trivy), pyramide de tests, revue globale du code.
  - Livrable : CI complète ; liste des améliorations possibles.
  - Validation : CI verte de bout en bout.

## Bilan 3 - "Dans mon frigo" (bilan final)

- [ ] **Bilan 3 Filtre par ingrédients disponibles**
  - Objectif : vérifier ce qui est resté de tout le cours, sur une fonctionnalité complète, sans accompagnement.
  - Notions mobilisées : migration et index, `Specification`, paramètres de requête et validation, performance (N+1), contrat OpenAPI, tests à tous les niveaux, observabilité. Petite nouveauté choisie selon ce qui sera alors flou.
  - Livrable : filtre des recettes réalisables avec une liste d'ingrédients disponibles ; règles précises (correspondance des noms, ingrédients sans quantité comme "sel, poivre") définies au démarrage du bilan.
  - Validation : CI verte ; revue globale du code ; liste des améliorations possibles ; grille de compétences finale.

## Bonus - Serveur MCP (optionnel)

- [ ] **Bonus 1 Exposer l'API comme serveur MCP**
  - Objectif : permettre à un agent IA de consulter et proposer des recettes.
  - Notions : Spring AI (starter MCP server), outils (`@Tool` / `@McpTool`) qui s'appuient sur les services existants, transport HTTP.
  - Livrable : outils de recherche et de consultation.
  - Validation : Claude Code connecté au serveur MCP local répond à "trouve-moi un dessert vegan".

- [ ] **Bonus 2 Écriture et sécurité**
  - Objectif : ajouter une recette via l'agent, de manière sécurisée.
  - Notions : outil d'écriture, réutilisation de la validation, authentification du client MCP (JWT existant ; évolution possible vers OAuth, prévue par la spécification MCP).
  - Livrable : outil d'ajout de recette protégé.
  - Validation : ajout réussi avec jeton, refusé sans jeton.

---

## Décisions

| Date | Décision |
|---|---|
| 2026-11-16 | API REST seule ; front hors périmètre ; pas de reprise du projet `obi-wan-quinoa` ni de ses recettes. |
| 2026-11-16 | Java seul d'abord (chapitre 1), puis Spring Boot. |
| 2026-11-16 | Architecture en couches simple, organisée par fonctionnalité ; pas de DDD ni d'hexagonal. |
| 2026-11-16 | Java 25 LTS ; Spring Boot 4.2.x (GA prévue le 2026-11-19, sinon 4.1.x puis montée de version) ; Maven 3.9 via Wrapper. |
| 2026-11-16 | Authentification JWT signée par l'API, sans serveur OAuth/OIDC (évolution possible au bonus MCP). |
| 2026-11-16 | GitHub Flow : une PR par étape, revue par une GitHub Action Claude qui explique sans corriger. |
| 2026-11-16 | IDE : VSCode. |
| 2026-11-16 | Mise à l'échelle des portions et conversion métrique/US (quantités et température du four en °C/°F) à la charge du front (exigence hors-ligne). Ingrédient : nom + précision optionnelle + quantité optionnelle. L'API stocke les quantités en g, ml, c. à café, c. à soupe ou en unités non convertibles (pièce, pincée...), liste fermée fixée à l'étape 1.2 ; pas d'endpoint de référentiel des unités, la liste des codes est documentée par le contrat OpenAPI. |
| 2026-11-16 | Version du JDK fixée par projet via `.sdkmanrc` (créé en 0.1, committé en 0.3). |
| 2026-11-16 | Accompagnement dégressif et mémos de syntaxe `memo-*.md`. |
| 2026-11-16 | Étape modèle : théorie (exemples complets exécutés, sur un autre sujet) puis pratique (code du projet). Phases : guidée ch. 0-2, semi-guidée ch. 3-6, autonome ch. 7-10. |
| 2026-11-16 | Ajout de l'étape 1.0 (syntaxe de base) ; fil rouge `Quantity` réécrite de 1.0 à 1.2 ; suppression de l'étape 1.4, remplacée par le bilan 1. |
| 2026-11-16 | Trois bilans en fin de phase, chacun une vraie fonctionnalité : bilan 1 (record `Step`), bilan 2 (autocomplétion, retirée de l'étape 7.3), bilan 3 ("Dans mon frigo", jusque-là idée future du cahier des charges). |
| 2026-11-16 | Exercices temporaires dans `sandbox/`, jamais committés, vérifiés en local ; le résultat de chaque chapitre sert le projet. |
| 2026-11-16 | Durées indicatives (effort, pas calendrier) ; reprise via le journal (`JOURNAL.md`), rituels de début et de fin de session. |
| 2026-11-16 | VSCode : httpYac plutôt que REST Client (non maintenu depuis 2022). Aucun chemin de machine dans `.vscode/settings.json` : le JDK du projet est détecté par l'extension Java (SDKMAN) ou fixé dans les réglages utilisateur. |
| 2026-11-16 | Documents du cours : un dossier par chapitre, un fichier par étape, mise en forme aérée et régulière (règles dans `CLAUDE.md`). |
| 2026-11-17 | `main` protégée par une ruleset sans exception (bypass list vide) ; merge en squash seul, titre de PR = commit sur `main`. Les documents du cours passent aussi par une PR `docs/...`, mergée par Claude après validation. |
| 2026-11-17 | Étape 1.2 : `Unit` porte un symbole utilisé par `Quantity.parse` et `toString` ; noms des constantes en anglais = futurs codes de l'API ; `Quantity.value` normalisée par `stripTrailingZeros` ; `null` refusé par `NullPointerException` (`requireNonNull`), valeur invalide par `IllegalArgumentException` ; packages `@NullMarked` (JSpecify). |
| 2026-11-18 | Listes fermées fixées par Vincent : `RecipeType` (`STARTER`, `MAIN_COURSE`, `SIDE_DISH`, `DESSERT`, `BREAKFAST`, `SNACK`, `DRINK`, `SAUCE`) ; `Diet` (`VEGETARIAN`, `VEGAN`, `GLUTEN_FREE`, `LOW_FAT`, `LACTOSE_FREE`) ; symboles de `Unit` en anglais, sensibles à la casse (`g`, `ml`, `tsp`, `tbsp`, `piece`, `pinch`, `clove`, `sprig`, `branch`, `leaf`, `slice`, `bunch`, `sachet`, `jar`). `Quantity.parse` laisse passer la `NumberFormatException` de `BigDecimal` (sous-classe d'`IllegalArgumentException`) ; les messages destinés au client seront traités au chapitre 7. |
| 2026-11-17 | Notions de l'étape 1.2 recentrées : classes, visibilité, `final` et `static` déjà vus en 1.0 ; les interfaces passent à l'étape 3.2 (injection de dépendances). |
