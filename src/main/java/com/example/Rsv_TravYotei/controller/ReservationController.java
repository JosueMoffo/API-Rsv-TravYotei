package com.example.Rsv_TravYotei.controller;

import com.example.Rsv_TravYotei.model.Reservation;
import com.example.Rsv_TravYotei.service.ReservationService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/rsv-travyotei")
public class ReservationController {

    private final ReservationService service;

    public ReservationController(ReservationService service) {
        this.service = service;
    }

    @GetMapping
    public List<Reservation> getAll() {
        return service.getAll();
    }

    @GetMapping("/{id}")
    public Reservation getOne(@PathVariable Long id) {
        return service.getById(id);
    }

    @PostMapping
    public Reservation create(@RequestBody Reservation r) {
        return service.create(r);
    }

    @PutMapping("/{id}")
    public Reservation update(@PathVariable Long id, @RequestBody Reservation r) {
        return service.update(id, r);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        service.delete(id);
    }
}
