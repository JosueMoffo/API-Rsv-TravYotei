package com.example.Rsv_TravYotei.model;

public class Reservation {
    private Long id;
    private String clientNom;
    private String destination;
    private String dateDepart;
    private String dateRetour;
    private int nombrePersonnes;

    public Reservation() {}

    public Reservation(Long id, String clientNom, String destination, String dateDepart, String dateRetour, int nombrePersonnes) {
        this.id = id;
        this.clientNom = clientNom;
        this.destination = destination;
        this.dateDepart = dateDepart;
        this.dateRetour = dateRetour;
        this.nombrePersonnes = nombrePersonnes;
    }

    // Getters et Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getClientNom() { return clientNom; }
    public void setClientNom(String clientNom) { this.clientNom = clientNom; }

    public String getDestination() { return destination; }
    public void setDestination(String destination) { this.destination = destination; }

    public String getDateDepart() { return dateDepart; }
    public void setDateDepart(String dateDepart) { this.dateDepart = dateDepart; }

    public String getDateRetour() { return dateRetour; }
    public void setDateRetour(String dateRetour) { this.dateRetour = dateRetour; }

    public int getNombrePersonnes() { return nombrePersonnes; }
    public void setNombrePersonnes(int nombrePersonnes) { this.nombrePersonnes = nombrePersonnes; }
}
