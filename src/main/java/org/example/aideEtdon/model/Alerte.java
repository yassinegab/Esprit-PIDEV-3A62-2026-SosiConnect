package org.example.aideEtdon.model;

import java.time.LocalDateTime;

public class Alerte {
    private int id;
    private String typeBesoin;
    private double latitude;
    private double longitude;
    private LocalDateTime dateAlerte;
    private String statut; // "En Attente", "Résolue"

    public Alerte() {}

    public Alerte(String typeBesoin, double latitude, double longitude) {
        this.typeBesoin = typeBesoin;
        this.latitude = latitude;
        this.longitude = longitude;
        this.statut = "En Attente";
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getTypeBesoin() { return typeBesoin; }
    public void setTypeBesoin(String typeBesoin) { this.typeBesoin = typeBesoin; }

    public double getLatitude() { return latitude; }
    public void setLatitude(double latitude) { this.latitude = latitude; }

    public double getLongitude() { return longitude; }
    public void setLongitude(double longitude) { this.longitude = longitude; }

    public LocalDateTime getDateAlerte() { return dateAlerte; }
    public void setDateAlerte(LocalDateTime dateAlerte) { this.dateAlerte = dateAlerte; }

    public String getStatut() { return statut; }
    public void setStatut(String statut) { this.statut = statut; }
}
