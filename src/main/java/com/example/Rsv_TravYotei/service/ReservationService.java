package com.example.Rsv_TravYotei.service;

import com.example.Rsv_TravYotei.model.Reservation;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.atomic.AtomicLong;

@Service
public class ReservationService {

    private final Map<Long, Reservation> reservations = new HashMap<>();
    private final AtomicLong idCounter = new AtomicLong();

    public List<Reservation> getAll() {
        return new ArrayList<>(reservations.values());
    }

    public Reservation getById(Long id) {
        return reservations.get(id);
    }

    public Reservation create(Reservation r) {
        long id = idCounter.incrementAndGet();
        r.setId(id);
        reservations.put(id, r);
        return r;
    }

    public Reservation update(Long id, Reservation r) {
        if (!reservations.containsKey(id)) return null;
        r.setId(id);
        reservations.put(id, r);
        return r;
    }

    public boolean delete(Long id) {
        return reservations.remove(id) != null;
    }
}
