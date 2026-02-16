# Store Project

Application de gestion de magasin complète avec API REST, gestion d'inventaire, système de commandes et authentification JWT.

![Java](https://img.shields.io/badge/Java-21-orange?logo=openjdk)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-4.0.2-brightgreen?logo=springboot)
![PostgreSQL](https://img.shields.io/badge/PostgreSQL-16-blue?logo=postgresql)
![Docker](https://img.shields.io/badge/Docker-Ready-2496ED?logo=docker)
![Tests](https://img.shields.io/badge/Tests-44%20passing-success)

## Table des matières

- [Fonctionnalités](#fonctionnalités)
- [Technologies](#technologies)
- [Architecture](#architecture)
- [Prérequis](#prérequis)
- [Installation](#installation)
- [Configuration](#configuration)
- [Lancement](#lancement)
- [API Documentation](#api-documentation)
- [Tests](#tests)
- [Déploiement Docker](#déploiement-docker)
- [Structure du projet](#structure-du-projet)
- [Fonctionnalités détaillées](#fonctionnalités-détaillées)

## Fonctionnalités

### Authentification & Autorisation
- Inscription et connexion utilisateurs
- JWT (JSON Web Tokens) pour l'authentification
- Gestion des rôles
- Token blacklist pour la déconnexion sécurisée

### Gestion des produits
- CRUD complet des produits
- Gestion des stocks avec suivi des mouvements
- Support des quantités décimales (BigDecimal)
- Pagination des résultats
- Gestion des unités de mesure (kg, L, pièces, etc.)

### Gestion des commandes
- Création et modification de commandes
- Conversion automatique entre unités de mesure
- Validation de stock en temps réel
- Numérotation unique des commandes
- Historique des modifications de stock
- Calculs précis avec BigDecimal (évite les erreurs d'arrondi)

### Système d'unités
- Catégories d'unités (poids, volume, quantité)
- Conversions automatiques entre unités
- Unités de base par catégorie
- Support des unités personnalisées

## Technologies

### Backend
- **Java 21** (LTS)
- **Spring Boot 4.0.2**
  - Spring Data JPA (Hibernate 7.2.1)
  - Spring Security 7.0.2
  - Spring Web MVC
- **PostgreSQL 16** - Base de données relationnelle
- **JWT (jjwt 0.12.5)** - Authentification stateless

### Documentation
- **SpringDoc OpenAPI 2.7.0** - Documentation API interactive (Swagger)

### Testing
- **JUnit 6.0.2** - Framework de tests
- **Mockito 5.20.0** - Mocking pour tests unitaires
- **44 tests unitaires** avec couverture complète des services

### DevOps
- **Docker & Docker Compose** - Containerisation
- **Maven** - Gestion de dépendances et build

## Architecture

```
┌─────────────────────────────────────────────────┐
│            Couche Présentation (API REST)       │
│  Controllers + DTOs (Request/Response)          │
└─────────────────────┬───────────────────────────┘
                      │
┌─────────────────────▼───────────────────────────┐
│         Couche Service (Logique Métier)         │
│  ProductService, OrderService, AuthService      │
│  UnitConversionService, StockService            │
└─────────────────────┬───────────────────────────┘
                      │
┌─────────────────────▼───────────────────────────┐
│      Couche Persistance (Spring Data JPA)       │
│  Repositories (ProductRepo, OrderRepo, etc.)    │
└─────────────────────┬───────────────────────────┘
                      │
┌─────────────────────▼───────────────────────────┐
│            Base de données PostgreSQL           │
│  Tables: users, products, orders, units, etc.   │
└─────────────────────────────────────────────────┘
```

### Sécurité

```
Request → JWT Filter → Authentication → Authorization → Controller
            ↓
      Token Validation
            ↓
      User Context
```

## Prérequis

- **Java 17** ou supérieur (JDK)
- **Maven 3.8+** (ou utiliser le wrapper Maven inclus `./mvnw`)
- **PostgreSQL 16** (ou Docker pour l'exécuter)
- **Docker & Docker Compose** (optionnel, pour le déploiement containerisé)

## Installation

### 1. Cloner le repository

```bash
git clone https://github.com/marcostart/store-api-spring-boot.git
cd store-api-spring-boot
```

### 2. Créer la base de données PostgreSQL

```bash
# Se connecter à PostgreSQL
psql -U postgres

# Créer la base de données
CREATE DATABASE store_db;

# Créer un utilisateur (optionnel)
CREATE USER store_user WITH PASSWORD 'votre_mot_de_passe';
GRANT ALL PRIVILEGES ON DATABASE store_db TO store_user;
```

### 3. Configurer l'application

Créez un fichier `.env` ou modifiez [application.properties](src/main/resources/application.properties) :

```properties
# Base de données
DB_HOST=localhost
DB_PORT=5432
DB_NAME=store_db
DB_USER=postgres
DB_PASSWORD=votre_mot_de_passe

# JWT
JWT_SECRET=votre_secret_jwt_tres_securise_au_moins_256_bits
JWT_EXPIRATION_MS=86400000

# Serveur
SERVER_PORT=9000
```

## Configuration

### Variables d'environnement

| Variable | Description | Défaut |
|----------|-------------|--------|
| `DB_HOST` | Hôte PostgreSQL | `localhost` |
| `DB_PORT` | Port PostgreSQL | `5432` |
| `DB_NAME` | Nom de la base | `store_db` |
| `DB_USER` | Utilisateur DB | `user` |
| `DB_PASSWORD` | Mot de passe DB | `password` |
| `JWT_SECRET` | Clé secrète JWT | *(voir properties)* |
| `JWT_EXPIRATION_MS` | Durée du token (ms) | `86400000` (24h) |
| `SERVER_PORT` | Port de l'application | `9000` |

## Lancement

### Initialisation automatique des données

Au premier démarrage, l'application exécute automatiquement des **seeds** pour initialiser les données de base :

- **Rôles** : 3 rôles pré-configurés (USER, ADMIN, SUPER_ADMIN)
- **Unités de mesure** : 
  - Poids : gramme (g), kilogramme (kg), milligramme (mg), tonne (t)
  - Volume : millilitre (mL), centilitre (cL), décilitre (dL), litre (L)
- **Conversions d'unités** : Toutes les conversions entre unités de la même catégorie

Ces données sont créées uniquement si elles n'existent pas déjà en base de données.

### Avec Maven (développement)

```bash
# Compiler et lancer
./mvnw spring-boot:run

# Ou compiler puis exécuter
./mvnw clean package
java -jar target/store-project-0.0.1-SNAPSHOT.jar
```

### Avec Docker Compose (production-like)

```bash
# Lancer PostgreSQL + Application
docker compose up -d --build

# Voir les logs
docker compose logs -f app
```

L'application sera accessible sur **http://localhost:9000**

## API Documentation

### Swagger UI (interactif)

Une fois l'application lancée, accédez à :

**http://localhost:9000/swagger-ui.html**

### Endpoints principaux

#### Authentication (`/api/auth`)

| Méthode | Endpoint | Description |
|---------|----------|-------------|
| POST | `/api/auth/register` | Inscription utilisateur |
| POST | `/api/auth/login` | Connexion (renvoie JWT) |
| POST | `/api/auth/logout` | Déconnexion |
| GET | `/api/auth/roles` | Liste des rôles |

#### Products (`/api/products`)

| Méthode | Endpoint | Description | Auth |
|---------|----------|-------------|------|
| GET | `/api/products` | Liste paginée | Oui |
| GET | `/api/products/{id}` | Détails produit | Oui |
| POST | `/api/products` | Créer produit | Oui |
| PUT | `/api/products/{id}` | Modifier produit | Oui |
| DELETE | `/api/products/{id}` | Supprimer produit | Oui |

#### Orders (`/api/orders`)

| Méthode | Endpoint | Description | Auth |
|---------|----------|-------------|------|
| GET | `/api/orders` | Toutes les commandes | Oui Admin |
| GET | `/api/orders/my-orders` | Mes commandes | Oui |
| GET | `/api/orders/{id}` | Détails commande | Oui |
| POST | `/api/orders` | Créer commande | Oui |
| PUT | `/api/orders/{id}` | Modifier commande | Oui |
| PATCH | `/api/orders/{id}/status` | Changer statut | Oui |
| DELETE | `/api/orders/{id}` | Supprimer commande | Oui |

#### Units (`/api/units`)

| Méthode | Endpoint | Description | Auth |
|---------|----------|-------------|------|
| GET | `/api/units` | Liste des unités | Oui |
| POST | `/api/units` | Créer unité | Oui |
| POST | `/api/units/conversions` | Créer conversion | Oui |

### Exemples de requêtes

#### Inscription

```bash
curl -X POST http://localhost:9000/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "username": "john",
    "firstname": "John",
    "lastname": "Doe",
    "email": "john@example.com",
    "password": "securePassword123",
    "roleId": "uuid-du-role"
  }'
```

#### Connexion

```bash
curl -X POST http://localhost:9000/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "username": "john",
    "password": "securePassword123"
  }'
```

Réponse :
```json
{
  "user": { ... },
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "expireAt": "2026-02-17T00:00:00.000+00:00"
}
```

#### Créer une commande (avec JWT)

```bash
curl -X POST http://localhost:9000/api/orders \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer VOTRE_TOKEN_JWT" \
  -d '{
    "items": [
      {
        "productId": "uuid-du-produit",
        "quantity": 5.5,
        "unitId": "uuid-de-unite"
      }
    ]
  }'
```

## Tests

### Exécuter tous les tests

```bash
./mvnw test
```

### Tests par classe

```bash
# ProductService
./mvnw test -Dtest=ProductServiceImplTest

# OrderService
./mvnw test -Dtest=OrderServiceImplTest

# AuthenticationService
./mvnw test -Dtest=AuthenticationServiceImplTest
```

### Couverture des tests

- **44 tests unitaires**
- **ProductServiceImpl** : 14 tests
- **OrderServiceImpl** : 16 tests
- **AuthenticationServiceImpl** : 13 tests
- **StoreProjectApplicationTests** : 1 test d'intégration

#### Tests couverts

- CRUD complet des entités
- Logique métier (conversions, validations)
- Gestion des erreurs et cas limites
- Authentification JWT
- Autorisation et contrôle d'accès
- Calculs avec précision décimale

## Déploiement Docker

### Démarrage rapide

```bash
# Build et démarrage
docker compose up -d --build

# Vérifier les logs
docker compose logs -f

# Arrêter
docker compose down
```

### Configuration

Le fichier [docker-compose.yml](docker-compose.yml) configure :

- **PostgreSQL 16** sur port 5432
- **Application Spring Boot** sur port 9000
- Volume persistant pour les données PostgreSQL
- Health checks automatiques
- Network bridge isolé

### Commandes utiles

```bash
docker compose build app

docker compose restart app

docker compose exec app sh

docker stats

## Structure du projet

```
store-project/
├── src/
│   ├── main/
│   │   ├── java/marcostar/project/store_project/
│   │   │   ├── config/              # Configurations (Security, JWT)
│   │   │   ├── controllers/         # API REST Controllers
│   │   │   ├── dtos/                # Data Transfer Objects
│   │   │   │   ├── auth/           # DTOs d'authentification
│   │   │   │   ├── order/          # DTOs de commandes
│   │   │   │   ├── product/        # DTOs de produits
│   │   │   │   └── unit/           # DTOs d'unités
│   │   │   ├── entities/            # Entités JPA
│   │   │   │   └── enums/          # Énumérations
│   │   │   ├── repositories/        # Spring Data Repositories
│   │   │   ├── services/            # Interfaces de services
│   │   │   │   └── implementations/ # Implémentations
│   │   │   └── StoreProjectApplication.java
│   │   └── resources/
│   │       ├── application.properties
│   │       ├── static/
│   │       └── templates/
│   └── test/
│       └── java/marcostar/project/store_project/
│           ├── services/implementations/
│           │   ├── AuthenticationServiceImplTest.java
│           │   ├── OrderServiceImplTest.java
│           │   └── ProductServiceImplTest.java
│           └── StoreProjectApplicationTests.java
├── .mvn/                   
├── Dockerfile              
├── docker-compose.yml      
├── .dockerignore         
├── pom.xml                
└── README.md              
```

## Fonctionnalités détaillées

### Gestion des stocks

- **Suivi des mouvements** : Chaque entrée/sortie de stock est enregistrée
- **Types de mouvements** : `IN` (entrée), `OUT` (sortie)
- **Raisons obligatoires** : Traçabilité complète
- **Précision décimale** : Utilisation de BigDecimal pour éviter les erreurs d'arrondi

### Conversion d'unités

- **Catégories** : Poids (WEIGHT), Volume (VOLUME), Quantité (QUANTITY)
- **Unité de base** : Une unité de base par catégorie (ex: kg pour poids)
- **Conversions bidirectionnelles** : Facteurs de conversion automatiques
- **Application automatique** : Lors de la création de commandes

### Système de commandes

- **Numérotation unique** : Format `ORD-{timestamp}-{random}`
- **Validation de stock** : Vérification en temps réel
- **Conversion automatique** : Unités converties vers l'unité du produit
- **Réversion de stock** : En cas de modification/suppression
- **Statuts** : PENDING, PROCESSING, SHIPPED, DELIVERED, CANCELLED

### Sécurité

- **JWT Tokens** : Authentification stateless
- **Password Encoding** : BCrypt pour le hachage des mots de passe
- **Token Blacklist** : Invalidation des tokens lors de la déconnexion
- **CORS configuré** : Pour les applications frontend
- **Validation des rôles** : @PreAuthorize sur les endpoints sensibles

## Configuration avancée

### Profils Spring

Créez des fichiers `application-{profile}.properties` :

```properties
# application-prod.properties
spring.jpa.hibernate.ddl-auto=validate
spring.jpa.show-sql=false
debug=false

# application-dev.properties
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true
debug=true
```

Lancer avec profil :
```bash
java -jar app.jar --spring.profiles.active=prod
```

### Base de données de production

Pour production, utilisez des migrations avec Flyway ou Liquibase :

```xml
<!-- pom.xml -->
<dependency>
    <groupId>org.flywaydb</groupId>
    <artifactId>flyway-core</artifactId>
</dependency>
```

## Contribution

Les contributions sont les bienvenues !

1. Forkez le projet
2. Créez une branche feature (`git checkout -b feature/AmazingFeature`)
3. Committez vos changements (`git commit -m 'Add AmazingFeature'`)
4. Pushez vers la branche (`git push origin feature/AmazingFeature`)
5. Ouvrez une Pull Request

### Standards de code

- Suivre les conventions Java standard
- Tests unitaires obligatoires pour les nouvelles fonctionnalités
- Documentation Javadoc pour les méthodes publiques
- Commits avec messages descriptifs

## Changelog

### Version 0.0.1-SNAPSHOT (2026-02-16)

#### Ajouté
- Système d'authentification JWT complet
- Gestion CRUD des produits avec stocks décimaux
- Système de commandes avec conversion d'unités
- Gestion des unités de mesure et conversions
- 44 tests unitaires avec Mockito
- Documentation Swagger/OpenAPI
- Déploiement Docker avec PostgreSQL
- Pagination des résultats
- Suivi des mouvements de stock

#### Fonctionnalités techniques
- Support BigDecimal pour précision décimale
- Token blacklist pour sécurité JWT
- Health checks Docker
- Build multi-stage optimisé


## Auteurs

- **Marcolin** - *Développement complet*

## Support

Pour toute question ou problème :

- Veuillez me contacter
- Email : marcolinmontcho@gmail.com

Made with Java & Spring Boot
