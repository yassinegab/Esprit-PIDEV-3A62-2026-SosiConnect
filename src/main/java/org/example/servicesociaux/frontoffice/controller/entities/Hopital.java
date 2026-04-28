package org.example.servicesociaux.frontoffice.controller.entities;

import java.time.LocalDateTime;

public class Hopital {
    private int id;
    private String nom;
    private String adresse;
    private String telephone;
    private boolean serviceUrgenceDispo;
    private double latitude;
    private double longitude;
    private int capacite;
    private String specialites;
    private String ville;
    private String type;

    public Hopital(int id, String nom, String adresse, String telephone,
                   boolean serviceUrgenceDispo, double latitude, double longitude,
                   int capacite, String specialites, String ville, String type) {
        this.id = id;
        this.nom = nom;
        this.adresse = adresse;
        this.telephone = telephone;
        this.serviceUrgenceDispo = serviceUrgenceDispo;
        this.latitude = latitude;
        this.longitude = longitude;
        this.capacite = capacite;
        this.specialites = specialites;
        this.ville = ville;
        this.type = type;
    }
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    // + getters/setters
    // Getters & Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getNom() { return nom; }
    public void setNom(String nom) { this.nom = nom; }
    public String getAdresse() { return adresse; }
    public void setAdresse(String adresse) { this.adresse = adresse; }
    public String getTelephone() { return telephone; }
    public void setTelephone(String telephone) { this.telephone = telephone; }
    public boolean isServiceUrgenceDispo() { return serviceUrgenceDispo; }
    public void setServiceUrgenceDispo(boolean s) { this.serviceUrgenceDispo = s; }
    public double getLatitude() { return latitude; }
    public void setLatitude(double latitude) { this.latitude = latitude; }
    public double getLongitude() { return longitude; }
    public void setLongitude(double longitude) { this.longitude = longitude; }
    public int getCapacite() { return capacite; }
    public void setCapacite(int capacite) { this.capacite = capacite; }
    public String getSpecialites() { return specialites; }
    public void setSpecialites(String specialites) { this.specialites = specialites; }
    public String getVille() { return ville; }
    public void setVille(String ville) { this.ville = ville; }
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}