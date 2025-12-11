USE RsvTravYoteiDB;

-- Insérer 2 trajets avec des IDs fixes pour faciliter les tests
INSERT INTO trajet (id, depart_city, arrival_city, depart_time, arrival_time, total_seats, price_per_seat, is_active, created_at) 
VALUES 
(
    '11111111-1111-1111-1111-111111111111',  -- ID fixe facile à mémoriser
    'Yaounde',
    'Douala',
    NOW() + INTERVAL 2 DAY,          -- Départ dans 2 jours
    NOW() + INTERVAL 2 DAY + INTERVAL 4 HOUR,    -- Durée: 4 heures
    70,                                       
    4500,                                    
    TRUE,                                     -- Trajet actif
    NOW()                                     -- Date de création
),
-- Trajet 2: Yaounde → Ebolowa
(
    '22222222-2222-2222-2222-222222222222', 
    'Yaounde',
    'Ebolowa',
    NOW() + INTERVAL 3 DAY,          -- Départ dans 3 jours
    NOW() + INTERVAL 3 DAY + INTERVAL 3 HOUR, -- Durée: 3 heures
    70,                                       
    4000,                                    
    TRUE,                                     -- Trajet actif
    NOW()                                     -- Date de création
);

-- Insérer les inventaires pour les 2 trajets
INSERT INTO inventaire (trajet_id, seats_available, seats_locked, version, updated_at)
VALUES
-- Inventaire pour premier trajet
(
    '11111111-1111-1111-1111-111111111111',
    70,  -- Toutes les places disponibles initialement
    0,   -- Aucune place bloquée
    0,   -- Version initiale
    NOW()
),
-- Inventaire pour 2
(
    '22222222-2222-2222-2222-222222222222',
    70, 
    0,   
    0,   
    NOW()
);


-- Créer l'inventaire pour ce trajet
INSERT INTO inventaire (
    trajet_id,
    seats_available,
    seats_locked,
    version,
    updated_at
) VALUES (
    @trajet_id,
    50,  -- Toutes les places disponibles initialement
    0,   -- Aucune place bloquée
    0,   -- Version initiale
    NOW()
);


SET SQL_SAFE_UPDATES = 1; -- changer le status update {de 1 à 0} avant  de supprimer toutes
						  -- les lignes des tables ci-dessous
delete from trajet;
delete from reservation_items;
delete from reservations;



--  Vérifier les données insérées
SELECT * FROM trajet;
SELECT * FROM inventaire;
SELECT * FROM reservations;
SELECT * FROM reservation_items;
