# API-Rsv-TravYotei : Microservice de Gestion de Réservations

Microservice autonome de gestion des réservations pour une agence de voyage, implémentant une architecture événementielle avec Kafka.

## Fonctionnalités
- **Gestion des réservations** : Création, confirmation, annulation, modification
- **Gestion d'inventaire** : Vérification et blocage de places en temps réel
- **Architecture événementielle** : Communication asynchrone via Kafka
- **Expiration automatique** : Nettoyage des réservations PENDING expirées
- **Cohérence des données** : Verrouillage optimiste pour gestion de la concurrence
- **API REST complète** : Documentation OpenAPI/Swagger intégrée
- **[POUR TEST]** **Simulateur de paiement** : Simulation du flux de paiement avec délai configurable

## Technologies
- **Java 21** - Langage de programmation
- **Spring Boot 4.0.0** - Framework d'application
- **Spring Data JPA** - ORM et persistance
- **Spring Kafka** - Intégration de messagerie
- **Spring Validation** - Validation des données
- **Lombok** - Réduction du code boilerplate

- **MySQL 8.0** - Base de données relationnelle
- **Hibernate** - ORM JPA

- **Apache Kafka 3.5** - Bus d'événements

- **Docker & Docker Compose** - Conteneurisation
- **Maven** - Gestion des dépendances

## nstallation et Démarrage
### Prérequis
- Java 21+
- Maven 3.8+
- Docker & Docker Compose
- MySQL 8.0 (optionnel, inclus dans Docker)

### 1. Cloner le projet
```bash
git clone https://github.com/JosueMoffo/API-Rsv-TravYotei.git
cd API-Rsv-TravYotei
```
### 2. Démarrer l'infrastructure avec Docker
- # Démarrer Kafka et MySQL
docker-compose up -d
- # Vérifier que les services sont en cours
docker-compose ps
- # Créer les topics Kafka
./src/main/resources/util/create-kafka-topics.sh
- Configurer la base de données

### 3.Configurer l'application : application.properties

### 4.Construire et lancer l'application
- # Compilation
mvn clean compile
- # Lancement en mode développement
mvn spring-boot:run  #ou autre methode de lancement

## API Endpoints
Réservations

    POST /api/reservations - Créer une nouvelle réservation

    GET /api/reservations/{id} - Récupérer une réservation par ID

    POST /api/reservations/{id}/confirm - Confirmer une réservation

    POST /api/reservations/{id}/cancel - Annuler une réservation

    GET /api/reservations - Lister toutes les réservations (avec filtres)

Inventaire

    GET /api/reservations/availability/{trajetId}?seats=2 - Vérifier la disponibilité

Monitoring

    GET /api/reservations/health - Vérifier l'état du service

    POST /api/reservations/expire-pending - Déclencher manuellement l'expiration

Documentation

    GET /swagger-ui.html - Interface Swagger UI

    GET /api-docs - Documentation OpenAPI JSON

## Tests d'intégration
# Tester le flux complet de reservaion
./src/main/resources/util/test-payment-flow.sh

## Structure de la base de données

    trajet - Informations sur les trajets

    reservations - En-têtes des réservations

    reservation_items - Détails par passager

    inventaire - État des places disponibles

    payments - Historique des paiements

    UserAgency - Agences partenaires
