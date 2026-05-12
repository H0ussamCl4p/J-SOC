# J-SOC — Java SOC Incident Management Platform

> **Plateforme console de gestion d'incidents de sécurité pour équipe SOC**
> Projet POO Java — Application en ligne de commande, sans interface graphique.

---

## 📖 Table des matières

- [Contexte du projet](#-contexte-du-projet)
- [Objectifs pédagogiques](#-objectifs-pédagogiques)
- [Description fonctionnelle](#-description-fonctionnelle)
- [Architecture du projet](#-architecture-du-projet)
- [Stack technique](#-stack-technique)
- [L'équipe et la répartition des tâches](#-léquipe-et-la-répartition-des-tâches)
- [Planning de développement (1 semaine)](#-planning-de-développement-1-semaine)
- [Comment contribuer au projet](#-comment-contribuer-au-projet)
- [Démarrage rapide](#-démarrage-rapide)
- [Livrables attendus](#-livrables-attendus)
- [Avertissement légal](#-avertissement-légal)

---

## 🎯 Contexte du projet

**J-SOC** (Java Security Operations Center) est un projet académique réalisé dans le cadre du module de **Programmation Orientée Objet en Java**.

L'application simule une plateforme légère utilisée par une équipe SOC (Security Operations Center) pour gérer le cycle de vie complet des incidents de sécurité — de leur détection à leur résolution. C'est l'équivalent simplifié, en ligne de commande, d'outils professionnels comme **TheHive**, **Jira Service Management** ou **ServiceNow SecOps**.

### Pourquoi ce sujet ?

- ✅ **Pertinent métier** : reflète une vraie problématique en cybersécurité
- ✅ **Riche en POO** : hiérarchies naturelles (types d'incidents, rôles utilisateurs)
- ✅ **Workflow complexe** : machine à états, validations métier
- ✅ **Manipulation String** : parsing CSV, formatage de rapports, génération d'IDs
- ✅ **Réparti à 4** : modules clairement découpés

---

## 🎓 Objectifs pédagogiques

Ce projet met en œuvre les concepts fondamentaux suivants :

| Concept POO | Comment c'est appliqué dans J-SOC |
|-------------|------------------------------------|
| **Héritage** | `Incident` (abstract) → `PhishingIncident`, `MalwareIncident`, `DDoSIncident` ; `User` (abstract) → `Analyst`, `Manager` |
| **Polymorphisme** | Chaque type d'incident a sa propre `getResponseProcedure()` et son `computeSLA()` |
| **Classes abstraites** | `Incident`, `User`, `AbstractRepository` |
| **Interfaces** | `Assignable`, `Notifiable`, `Repository<T, ID>` |
| **Exceptions personnalisées** | `UnauthorizedActionException`, `InvalidStateTransitionException`, `IncidentNotFoundException`, `AuthenticationFailedException` |
| **Collections** | `Map<String, Incident>` (storage), `List<Comment>` (commentaires), `Set<String>` (IOCs), `Queue<Notification>` |
| **Manipulation String** | Parsing CSV, génération d'IDs (INC-001), formatage de rapports, validation regex |
| **Gestion fichiers** | Persistance CSV avec chargement/sauvegarde automatique |
| **Architecture modulaire** | 5 packages avec responsabilités séparées (model, repository, service, cli, exception) |

---

## ⚙️ Description fonctionnelle

### Acteurs

- **Analyst** : peut créer des incidents, consulter et mettre à jour ceux qui lui sont assignés, ajouter des commentaires
- **Manager** : a tous les droits de l'analyste + assignation des incidents, fermeture, accès aux statistiques

### Cycle de vie d'un incident

```
   ┌──────┐    ┌──────────┐    ┌─────────────┐    ┌──────────┐    ┌────────┐
   │ NEW  │ ──>│ ASSIGNED │ ──>│ IN_PROGRESS │ ──>│ RESOLVED │ ──>│ CLOSED │
   └──────┘    └──────────┘    └─────────────┘    └──────────┘    └────────┘
                                      │
                                      ▼
                                ┌────────────┐
                                │ ESCALATED  │
                                └────────────┘
```

Chaque transition est validée par `IncidentService` — une transition invalide lève une `InvalidStateTransitionException`.

### Types d'incidents supportés (v1.0)

| Type | CWE | SLA par défaut | Procédure spécifique |
|------|-----|----------------|----------------------|
| **Phishing** | CWE-1021 | 4h | Analyse email + URL + sandbox |
| **Malware** | CWE-506 | 1h | Isolation poste + analyse hash |
| **DDoS** | CWE-400 | 2h | Mitigation réseau + analyse trafic |

### Fonctionnalités principales

#### 🔐 Authentification
- Login par username/password
- Session unique par lancement
- Affichage du menu adapté au rôle

#### 📋 CRUD Incidents
- **Create** : créer un incident (type, titre, description, sévérité, IOCs)
- **Read** : lister tous les incidents, filtrer par statut/sévérité/assigné
- **Update** : modifier le statut, ajouter un commentaire, mettre à jour les IOCs
- **Delete** : suppression (réservée au Manager)

#### 👥 Assignation
- Assignation manuelle par le Manager
- Auto-assignation : analyste avec le moins d'incidents en cours
- Réassignation possible

#### ⏱️ Suivi du SLA
- Calcul automatique du temps restant selon la sévérité
- Alerte visuelle pour les SLA en danger (`⚠`) ou dépassés (`⚠⚠`)
- SLA par sévérité :
  - `CRITICAL` : 1h
  - `HIGH` : 4h
  - `MEDIUM` : 24h
  - `LOW` : 72h

#### 📊 Statistiques (vue Manager)
- Nombre d'incidents par sévérité
- Nombre d'incidents par type
- Charge de travail par analyste
- Temps moyen de résolution (MTTR — Mean Time To Resolve)

#### 💾 Persistance
- Sauvegarde automatique après chaque action critique
- Format CSV (lisible et debuggable)
- Fichiers dans `data/` :
  - `users.csv`
  - `incidents.csv`
  - `comments.csv`
  - `history.csv`

#### 📤 Export de rapports
- Rapport TXT : liste lisible des incidents ouverts
- Rapport CSV : export pour Excel
- Rapport Markdown : pour documentation

---

## 🏗️ Architecture du projet

### Structure des packages

```
J-SOC/
├── src/
│   └── main/
│       └── java/
│           └── jsoc/
│               ├── Main.java
│               │
│               ├── model/                    [MEMBRE 1]
│               │   ├── user/
│               │   │   ├── User.java         (abstract)
│               │   │   ├── Analyst.java
│               │   │   └── Manager.java
│               │   ├── incident/
│               │   │   ├── Incident.java     (abstract)
│               │   │   ├── PhishingIncident.java
│               │   │   ├── MalwareIncident.java
│               │   │   └── DDoSIncident.java
│               │   ├── enums/
│               │   │   ├── Severity.java
│               │   │   └── IncidentStatus.java
│               │   ├── Comment.java
│               │   ├── HistoryEntry.java
│               │   └── IOC.java
│               │
│               ├── interfaces/               [MEMBRE 1]
│               │   ├── Assignable.java
│               │   └── Notifiable.java
│               │
│               ├── exception/                [MEMBRE 2]
│               │   ├── JSocException.java   (parent)
│               │   ├── AuthenticationFailedException.java
│               │   ├── IncidentNotFoundException.java
│               │   ├── InvalidStateTransitionException.java
│               │   └── UnauthorizedActionException.java
│               │
│               ├── repository/               [MEMBRE 2]
│               │   ├── Repository.java       (interface)
│               │   ├── AbstractRepository.java
│               │   ├── IncidentRepository.java
│               │   ├── UserRepository.java
│               │   └── csv/
│               │       └── CsvHelper.java
│               │
│               ├── service/                  [MEMBRE 3]
│               │   ├── AuthService.java
│               │   ├── IncidentService.java
│               │   ├── AssignmentService.java
│               │   ├── SLAService.java
│               │   └── StatisticsService.java
│               │
│               └── cli/                      [MEMBRE 4]
│                   ├── menus/
│                   │   ├── LoginMenu.java
│                   │   ├── MainMenu.java
│                   │   ├── IncidentMenu.java
│                   │   ├── ManagerMenu.java
│                   │   └── ReportMenu.java
│                   └── utils/
│                       ├── ConsoleHelper.java
│                       └── TableFormatter.java
│
├── data/                                     (généré au runtime)
│   ├── users.csv
│   ├── incidents.csv
│   ├── comments.csv
│   └── history.csv
│
├── docs/
│   ├── UML.png
│   ├── UML.puml                              (source PlantUML)
│   └── rapport.pdf
│
├── .gitignore
├── README.md
├── CONTRIBUTING.md
└── LICENSE
```

---

## 🛠️ Stack technique

| Élément | Choix |
|---------|-------|
| **Langage** | Java 17+ |
| **Build** | Compilation javac (ou Maven/Gradle si l'équipe préfère) |
| **Persistance** | Fichiers CSV (sans BDD) |
| **UML** | PlantUML ou draw.io |
| **IDE recommandé** | IntelliJ IDEA Community ou VS Code + extension Java |
| **Versioning** | Git + GitHub |

> **Note** : aucune dépendance externe n'est requise. Tout est fait avec la bibliothèque standard Java.

---

## 👥 L'équipe et la répartition des tâches

Le projet est découpé en **4 modules indépendants** pour permettre un travail en parallèle sans blocage.

### 🧱 Membre 1 — L'Architecte (Modèle + UML)

**Responsabilités** :
- Conception et codage de **toutes les classes du modèle** (`User`, `Incident`, sous-classes, enums, classes auxiliaires)
- Création des **interfaces** (`Assignable`, `Notifiable`)
- Création de **toutes les exceptions personnalisées**
- Réalisation du **diagramme UML complet** (livrable obligatoire)
- Documentation Javadoc des classes principales

**Packages sous sa responsabilité** :
- `jsoc.model.*`
- `jsoc.interfaces.*`
- `jsoc.exception.*`

**Livrables** :
- Code du modèle compilable dès la fin du jour 2
- `docs/UML.puml` + `docs/UML.png`

---

### 💾 Membre 2 — Le Persisteur (Repository + Fichiers)

**Responsabilités** :
- Implémentation du pattern **Repository générique**
- Sérialisation / désérialisation **CSV**
- Chargement automatique au démarrage, sauvegarde après chaque action
- Gestion des IDs uniques (génération `INC-001`, `INC-002`...)
- Méthodes de recherche bas niveau (par statut, sévérité, date...)

**Packages sous sa responsabilité** :
- `jsoc.repository.*`

**Livrables** :
- Repository fonctionnel avec persistance CSV
- Chargement de données de test au démarrage si fichier vide

---

### ⚙️ Membre 3 — Le Cerveau métier (Services)

**Responsabilités** :
- **Authentification** (`AuthService`)
- **Logique de workflow** : validation des transitions d'état
- **Système d'assignation** (manuelle + auto-assignation au moins chargé)
- **Calcul du SLA** selon sévérité et date de création
- **Statistiques** (MTTR, distribution par sévérité/type/analyste)
- **Validations métier** (qui a le droit de faire quoi)

**Packages sous sa responsabilité** :
- `jsoc.service.*`

**Livrables** :
- Tous les services fonctionnels
- Documentation des règles métier dans le rapport

---

### 🖥️ Membre 4 — L'Interface (CLI + Rapports + Doc)

**Responsabilités** :
- Tous les **menus interactifs**
- **Validation des entrées utilisateur**
- **Affichage formaté** (tableaux ASCII)
- **Génération de rapports exportables** (TXT, CSV, Markdown)
- **README.md final**, **CONTRIBUTING.md**, scénario de démo

**Packages sous sa responsabilité** :
- `jsoc.cli.*`
- `jsoc.Main`

**Livrables** :
- Interface complète et utilisable
- Documentation utilisateur
- Scénario de démo de soutenance

---

## 📅 Planning de développement (1 semaine)

| Jour | M1 — Architecte | M2 — Persistance | M3 — Services | M4 — CLI |
|------|------------------|-------------------|----------------|-----------|
| **J1 (Dim)** | Réunion équipe + UML brouillon | Setup repo + .gitignore | Brainstorm workflow | Maquettes menus |
| **J2 (Lun)** | Modèle complet + interfaces + exceptions | Interface `Repository<T,ID>` | Lecture du modèle | `Main.java` + login menu vide |
| **J3 (Mar)** | Finalisation UML + Javadoc | `IncidentRepository` + `UserRepository` | `AuthService` + `IncidentService` (CRUD) | `LoginMenu` + `MainMenu` |
| **J4 (Mer)** | Aide intégration + début rapport | Persistance CSV + sauvegarde auto | `AssignmentService` + `SLAService` | `IncidentMenu` (CRUD) |
| **J5 (Jeu)** | Finition rapport + tests croisés | Format CSV propre + backup | `StatisticsService` + commentaires | Menu Manager + rapports export |
| **J6 (Ven)** | **TOUS ENSEMBLE** : debug, tests d'intégration, préparation démo |
| **J7 (Sam)** | Polissage final, relecture, soutenance |

> **🔔 Sync quotidien** : appel rapide de 20-30 min chaque soir à 21h pour faire le point.

---

## 🤝 Comment contribuer au projet

> Cette section est destinée aux **3 autres membres de l'équipe** qui vont collaborer sur ce dépôt.

### Étape 1 — Configuration initiale (à faire UNE SEULE FOIS)

#### 1.1 Installer les outils

- [Git](https://git-scm.com/downloads)
- [Java JDK 17+](https://adoptium.net/)
- IDE : [IntelliJ IDEA Community](https://www.jetbrains.com/idea/download/) (recommandé) ou [VS Code](https://code.visualstudio.com/) avec l'extension "Extension Pack for Java"

#### 1.2 Configurer Git (si pas déjà fait)

```bash
git config --global user.name "Ton Nom"
git config --global user.email "ton.email@example.com"
```

#### 1.3 Cloner le dépôt

```bash
git clone https://github.com/H0ussamCl4p/J-SOC.git
cd J-SOC
```

#### 1.4 Vérifier que tout fonctionne

```bash
git status
java -version    # doit afficher 17 ou plus
```

---

### Étape 2 — Workflow de travail au quotidien

Nous utilisons un workflow **branche par fonctionnalité** pour éviter les conflits.

#### 2.1 Toujours commencer la journée par un pull

```bash
git checkout main
git pull origin main
```

#### 2.2 Créer ta branche de travail

Le nommage des branches suit cette convention :

```
feature/<initiale-membre>-<description-courte>
```

**Exemples** :
- `feature/m1-incident-model`
- `feature/m2-csv-persistence`
- `feature/m3-auth-service`
- `feature/m4-login-menu`

```bash
git checkout -b feature/m1-incident-model
```

#### 2.3 Coder, tester, commiter

```bash
# Vérifier ce qui a changé
git status

# Ajouter les fichiers modifiés
git add src/main/java/jsoc/model/Incident.java

# Commiter avec un message clair
git commit -m "feat(model): add abstract Incident class with state transitions"
```

##### Convention de messages de commit

Utilise ce format pour des commits propres :

| Préfixe | Quand l'utiliser | Exemple |
|---------|------------------|---------|
| `feat:` | Nouvelle fonctionnalité | `feat(service): add SLA calculation` |
| `fix:` | Correction de bug | `fix(repo): handle empty CSV file on startup` |
| `docs:` | Documentation | `docs: update README with setup instructions` |
| `refactor:` | Refactoring sans changement de comportement | `refactor(cli): extract menu helpers` |
| `test:` | Ajout de tests | `test(service): add tests for IncidentService` |
| `chore:` | Tâches diverses (build, gitignore...) | `chore: add .gitignore for IntelliJ` |

#### 2.4 Pousser la branche sur GitHub

```bash
git push origin feature/m1-incident-model
```

#### 2.5 Ouvrir une Pull Request

1. Va sur [https://github.com/H0ussamCl4p/J-SOC](https://github.com/H0ussamCl4p/J-SOC)
2. GitHub va te proposer un bandeau jaune **"Compare & pull request"** → clique dessus
3. Remplis la description :
   - **Titre** : court et clair (`Add Incident model with abstract base class`)
   - **Description** : ce que ça fait, quels fichiers sont touchés, quels tests ont été faits
4. Clique sur **"Create pull request"**
5. Préviens dans le groupe WhatsApp/Discord pour qu'un autre membre review

#### 2.6 Attendre la review et merger

- **Au moins 1 autre membre** doit relire ton code avant le merge
- Une fois approuvé, **toi-même** tu cliques sur **"Merge pull request"**
- Ensuite, supprime la branche distante (bouton "Delete branch" sur GitHub)

#### 2.7 Nettoyer en local

```bash
git checkout main
git pull origin main
git branch -d feature/m1-incident-model    # supprime la branche locale
```

---

### Étape 3 — En cas de conflit

Si lors d'un `git pull` ou d'un merge tu as un conflit :

```bash
# 1. Mettre à jour main
git checkout main
git pull origin main

# 2. Revenir sur ta branche
git checkout feature/m1-incident-model

# 3. Rebaser sur main (réapplique tes commits par-dessus)
git rebase main

# 4. Si conflit : ouvrir les fichiers concernés, chercher les "<<<<<<<", choisir quel code garder
# Puis :
git add <fichier-résolu>
git rebase --continue

# 5. Pousser (force nécessaire après un rebase)
git push origin feature/m1-incident-model --force-with-lease
```

> **⚠️ JAMAIS** de `git push --force` sur la branche `main`. Le `--force-with-lease` n'est OK que sur ta propre branche.

---

### Étape 4 — Règles d'or de l'équipe

#### ✅ À FAIRE

- 🟢 **Pull avant chaque session** de travail
- 🟢 **Petits commits réguliers** plutôt qu'un énorme commit final
- 🟢 **Messages de commit clairs** en anglais (suit la convention)
- 🟢 **Une PR par fonctionnalité**, pas une PR avec 15 changements mélangés
- 🟢 **Prévenir l'équipe** quand tu touches à un fichier partagé
- 🟢 **Tester ton code** avant de pousser
- 🟢 **Demander de l'aide** au bout de 30 min de blocage

#### ❌ À NE PAS FAIRE

- 🔴 **Jamais commit sur `main` directement** — toujours via PR
- 🔴 **Jamais de `git push --force`** sur `main`
- 🔴 **Pas de fichiers `.class`, `.idea/`, `target/`, `out/`** dans le repo (le `.gitignore` doit s'en charger)
- 🔴 **Pas de mots de passe ou secrets** dans le code (même pour les tests)
- 🔴 **Pas de "merge commit"** sans review
- 🔴 **Pas de modification du code d'un autre membre** sans le prévenir

---

### Étape 5 — Communication

Outils utilisés par l'équipe :

| Outil | Usage |
|-------|-------|
| **GitHub Issues** | Tracker les tâches et les bugs |
| **WhatsApp/Discord** | Coordination rapide, sync quotidien |
| **Google Meet** | Réunion de fin de journée (20-30 min) |
| **Google Docs** | Rédaction collaborative du rapport |

#### Comment créer une issue

1. Onglet **Issues** sur le repo
2. Bouton **New issue**
3. Titre clair : `[M1] Add HistoryEntry class for action tracking`
4. Description : ce qui doit être fait, critères d'acceptation
5. Assigner à un membre + ajouter un label (`enhancement`, `bug`, `documentation`)

---

## 🚀 Démarrage rapide

> Cette section concerne **l'utilisation de l'application** une fois que le développement est terminé.

### Compilation

```bash
# Depuis la racine du projet
javac -d out src/main/java/jsoc/**/*.java
```

### Exécution

```bash
java -cp out jsoc.Main
```

### Comptes par défaut (pour les tests)

| Username | Password | Rôle |
|----------|----------|------|
| `alice` | `password` | Analyst |
| `bob` | `password` | Analyst |
| `carol` | `password` | Manager |

> ⚠️ Ces comptes sont uniquement pour le développement et la démo. Dans une vraie application, les mots de passe seraient hashés (PBKDF2 ou bcrypt).

### Premier lancement

Au premier lancement, le dossier `data/` est créé automatiquement avec des données d'exemple (3 incidents de test). Tu peux les modifier ou les supprimer.

---

## 📦 Livrables attendus

À la fin du projet, le dépôt doit contenir :

- ✅ **Code source Java** complet, compilable et fonctionnel
- ✅ **README.md** (ce fichier)
- ✅ **CONTRIBUTING.md** (guide de contribution)
- ✅ **docs/UML.png** + **docs/UML.puml** (diagramme de classes)
- ✅ **docs/rapport.pdf** (rapport technique du projet)
- ✅ **.gitignore** propre (pas de fichiers compilés versionnés)
- ✅ Données d'exemple dans `data/`
- ✅ Tag Git `v1.0` sur le commit final

---

## ⚖️ Avertissement légal

Ce projet est un **exercice pédagogique** réalisé dans un cadre académique. Il simule des concepts métiers de la cybersécurité mais n'est **pas destiné à un usage en production**.

Aucune donnée réelle ne doit être stockée dans le système. Les comptes utilisateurs et les incidents pré-chargés sont fictifs.

---

## 👤 Contributeurs

| Nom | Rôle | GitHub |
|-----|------|--------|
| Houssam | Architecte / Lead | [@H0ussamCl4p](https://github.com/H0ussamCl4p) |
| _À compléter_ | Persistance | _@username_ |
| Fouad_Naatani | Services | [@fouad-naatani](https://github.com/fouad-naatani)|
| _À compléter_ | CLI / Doc | _@username_ |

---

## 📜 Licence

Projet académique — usage pédagogique uniquement.

---

**Réalisé dans le cadre du module POO Java — 2026**
