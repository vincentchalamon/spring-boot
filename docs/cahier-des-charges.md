# Cahier des charges - Obi-Wan Quinoa (API)

Source : [document Google](https://docs.google.com/document/d/1irAff33tif4fMpIenng3vgoz9uUJfL7yQraFD3tQe1E/edit?usp=sharing). Cette page en est le résumé de référence pour le cours, avec le périmètre retenu.

## Vision

Gestionnaire de recettes accessible sur desktop (web) et mobile (PWA), adossé à une API, administré via un back-office sécurisé.

**Périmètre du cours : l'API REST uniquement.** Le front (web, PWA, back-office) est hors périmètre ; les fonctionnalités purement navigateur sont listées ci-dessous pour mémoire.

## Recette

| Champ | Notes |
|---|---|
| Titre | obligatoire |
| Type de repas | dessert, plat, accompagnement, etc. (liste fermée) |
| Régime | vegan, végétarien, etc. (liste fermée) |
| Rendement | nombre de portions |
| Calories par portion | |
| Contenant de cuisson | ex. moule à cake, cocotte |
| Température du four | optionnelle, en °C |
| Ingrédients | liste ordonnée ; nom + précision optionnelle ("bien mûres", "pour le décor") + quantité optionnelle ("sel, poivre"). Quantité = valeur numérique + unité (liste fermée ci-dessous) |
| Étapes | liste ordonnée ; une étape peut avoir une durée (minuteur côté front) |
| Image | une image par recette |

### Unités

- **Mesurables** : gramme, millilitre, cuillère à café (5 ml), cuillère à soupe (15 ml). Les multiples (kg, l, cl) et les unités US (cup, oz, lb) sont des formats d'affichage du front.
- **Non convertibles** : pièce, pincée, gousse, brin, branche, feuille, tranche, botte, sachet, pot.
- **Exclues** (imprécises ou variables) : verre, tasse, poignée, filet, noix, trait, boîte. Utiliser une mesure, ou une quantité absente avec une précision.

Liste définitive fixée à l'étape 1.2 du cours ; elle peut s'enrichir (ajout de valeur non cassant).

## Fonctionnalités

### Liste des recettes - API
- Paginée.
- Filtrable par type, régime, calories.
- Recherche libre : titre, ingrédients, contenant, etc.

### Consultation d'une recette - API
- Toutes les informations de la recette, quantités structurées en unités canoniques.
- L'adaptation au **nombre de portions** et la conversion **métrique / US** (cups, oz, °F pour la température du four) sont faites par le **front**, car elles doivent fonctionner hors-ligne.

### Administration - API
- Réservée aux administrateurs authentifiés.
- Ajout, modification, suppression d'une recette (formulaire unique côté front).
- Autocomplétion de certains champs (type, régime).
- Suppression avec confirmation (côté front).

### Hors périmètre (front)
- Mode cuisine (empêcher la mise en veille de l'écran).
- Minuteur sonore et visuel sur les étapes avec durée (l'API expose la durée).
- Impression / export PDF.
- Hors-ligne : liste non disponible hors-ligne ; une recette enregistrée par l'utilisateur est consultable hors-ligne.

## Idées futures

- **MCP** : un agent IA accède à l'application pour proposer des améliorations de recettes ou en ajouter. Chapitre bonus du cours.
- **"Dans mon frigo"** : filtrer les recettes selon les ingrédients disponibles. Non planifié.
