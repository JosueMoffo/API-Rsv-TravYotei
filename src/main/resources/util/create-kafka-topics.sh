#
echo "=== CRÉATION DES TOPICS KAFKA ==="

# Créer les topics nécessaires
docker-compose exec kafka kafka-topics --create \
  --topic reservation-created \
  --partitions 3 \
  --replication-factor 1 \
  --bootstrap-server localhost:9092

docker-compose exec kafka kafka-topics --create \
  --topic reservation-confirmed \
  --partitions 3 \
  --replication-factor 1 \
  --bootstrap-server localhost:9092

docker-compose exec kafka kafka-topics --create \
  --topic reservation-cancelled \
  --partitions 3 \
  --replication-factor 1 \
  --bootstrap-server localhost:9092

docker-compose exec kafka kafka-topics --create \
  --topic payment-confirmed \
  --partitions 3 \
  --replication-factor 1 \
  --bootstrap-server localhost:9092

# Lister les topics
echo "=== TOPICS CRÉÉS ==="
docker-compose exec kafka kafka-topics --list --bootstrap-server localhost:9092

#Donner les droits d'execution a ce fichier, puis l'executé
