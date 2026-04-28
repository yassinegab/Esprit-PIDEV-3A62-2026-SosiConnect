package org.example.servicesociaux.backoffice.entities;

import java.util.Date;

public class RendezVous {
    private int id;
    private int patientId;
    private int hopitalId;
    private String hopitalNom; // ✅ pour l'affichage dans le tableau
    private String typeConsultation;
    private String statut;
    private Date dateRendezVous;
    private String notes;

    public RendezVous(int id, int patientId, int hopitalId,
                      String typeConsultation, String statut,
                      Date dateRendezVous, String notes) {
        this.id = id;
        this.patientId = patientId;
        this.hopitalId = hopitalId;
        this.typeConsultation = typeConsultation;
        this.statut = statut;
        this.dateRendezVous = dateRendezVous;
        this.notes = notes;
    }

    public int getId()                      { return id; }
    public int getPatientId()               { return patientId; }
    public int getHopitalId()               { return hopitalId; }
    public String getHopitalNom()           { return hopitalNom; }
    public String getTypeConsultation()     { return typeConsultation; }
    public String getStatut()               { return statut; }
    public Date getDateRendezVous()         { return dateRendezVous; }
    public String getNotes()                { return notes; }

    public void setId(int id)                              { this.id = id; }
    public void setPatientId(int patientId)                { this.patientId = patientId; }
    public void setHopitalId(int hopitalId)                { this.hopitalId = hopitalId; }
    public void setHopitalNom(String hopitalNom)           { this.hopitalNom = hopitalNom; }
    public void setTypeConsultation(String t)              { this.typeConsultation = t; }
    public void setStatut(String statut)                   { this.statut = statut; }
    public void setDateRendezVous(Date dateRendezVous)     { this.dateRendezVous = dateRendezVous; }
    public void setNotes(String notes)                     { this.notes = notes; }
}