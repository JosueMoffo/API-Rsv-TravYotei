#
echo "=== TEST DU FLUX DE PAIEMENT KAFKA ==="
echo ""

# Fonctions d'affichage
GREEN='\033[0;32m'
RED='\033[0;31m'
YELLOW='\033[1;33m'
NC='\033[0m'

echo -e "${YELLOW}1. Création d'une réservation de test...${NC}"
RESERVATION_JSON='{
    "clientId": "test-payment-'$(date +%s)'",
    "transportId": "11111111-1111-1111-1111-111111111111",
    "passengers": [
        {"name": "Test Payment", "seatNumber": 99}
    ]
}'

echo "JSON envoyé: $RESERVATION_JSON"
echo ""

RESPONSE=$(curl -s -X POST "http://localhost:8080/api/reservations" \
  -H "Content-Type: application/json" \
  -d "$RESERVATION_JSON")

if echo "$RESPONSE" | grep -q '"id"'; then
    echo -e "${GREEN}✅ Réservation créée avec succès${NC}"
    RESERVATION_ID=$(echo "$RESPONSE" | grep -o '"id":"[^"]*"' | head -1 | cut -d'"' -f4)
    echo "ID de réservation: $RESERVATION_ID"
    echo "Statut: $(echo "$RESPONSE" | grep -o '"status":"[^"]*"' | head -1 | cut -d'"' -f4)"
else
    echo -e "${RED}❌ Échec création réservation${NC}"
    echo "Réponse: $RESPONSE"
    exit 1
fi

echo ""
echo -e "${YELLOW}2. Flux Kafka attendu:${NC}"
echo "   → T+0s:   'reservation-created' envoyé"
echo "   → T+0s:   PaymentSimulator reçoit le message"
echo "   → T+30s:  PaymentSimulator envoie 'payment-confirmed'"
echo "   → T+30s:  PaymentConfirmationConsumer traite la confirmation"
echo "   → T+30s:  Réservation passe en CONFIRMED"

echo ""
echo -e "${YELLOW}3. Vérification immédiate (T+0s):${NC}"
curl -s "http://localhost:8080/api/reservations/$RESERVATION_ID" | \
  python3 -m json.tool 2>/dev/null || \
  echo "Réservation: $RESPONSE"

echo ""
echo -e "${YELLOW}4. Attente de 35 secondes pour la simulation de paiement...${NC}"
echo "   (Regardez les logs Spring Boot pendant ce temps)"
for i in {1..35}; do
    echo -ne "⏳ Attente: $i/35 secondes\r"
    sleep 1
done
echo ""

echo ""
echo -e "${YELLOW}5. Vérification après paiement simulé (T+35s):${NC}"
curl -s "http://localhost:8080/api/reservations/$RESERVATION_ID" | \
  python3 -m json.tool 2>/dev/null || \
  curl -s "http://localhost:8080/api/reservations/$RESERVATION_ID"

echo ""
echo -e "${YELLOW}6. Vérification du statut final:${NC}"
STATUS=$(curl -s "http://localhost:8080/api/reservations/$RESERVATION_ID" | \
  grep -o '"status":"[^"]*"' | head -1 | cut -d'"' -f4)

if [ "$STATUS" = "CONFIRMED" ]; then
    echo -e "${GREEN}✅ SUCCÈS: Réservation confirmée automatiquement par Kafka${NC}"
elif [ "$STATUS" = "PENDING" ]; then
    echo -e "${RED}❌ ÉCHEC: Réservation toujours PENDING après 35s${NC}"
    echo "   → Le simulateur n'a pas envoyé l'événement"
    echo "   → Vérifiez les logs du PaymentSimulator"
elif [ "$STATUS" = "CANCELLED" ] || [ "$STATUS" = "EXPIRED" ]; then
    echo -e "${YELLOW}⚠️ ATTENTION: Réservation annulée/expirée${NC}"
    echo "   → L'expiration automatique (2 min) s'est déclenchée avant la confirmation"
    echo "   → Testez avec expiration-minutes=5 dans application.properties"
else
    echo -e "${RED}❌ ERREUR: Statut inconnu '$STATUS'${NC}"
fi

echo ""
echo "=========================================="
echo -e "${YELLOW} RÉSUMÉ DU TEST${NC}"
echo "=========================================="
