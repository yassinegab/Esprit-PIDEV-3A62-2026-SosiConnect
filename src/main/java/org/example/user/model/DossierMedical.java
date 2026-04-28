package org.example.user.model;

import java.sql.Timestamp;
import java.time.format.DateTimeFormatter;

public class DossierMedical {

    private int       id;
    private int       userId;
    private String    antecedentsMedicaux;
    private String    maladiesChroniques;
    private String    allergies;
    private String    traitementsEnCours;
    private String    diagnostics;
    private String    notesMedecin;
    private String    objectifSante;
    private String    niveauActivite;
    private Timestamp dateCreation;
    private Timestamp derniereMiseAJour;

    private static final DateTimeFormatter FMT =
            DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    // ── Constructeur vide ──
    public DossierMedical() {}

    // ── Constructeur complet ──
    public DossierMedical(int id, int userId,
                          String antecedentsMedicaux, String maladiesChroniques,
                          String allergies, String traitementsEnCours,
                          String diagnostics, String notesMedecin,
                          String objectifSante, String niveauActivite,
                          Timestamp dateCreation, Timestamp derniereMiseAJour) {
        this.id                  = id;
        this.userId              = userId;
        this.antecedentsMedicaux = antecedentsMedicaux;
        this.maladiesChroniques  = maladiesChroniques;
        this.allergies           = allergies;
        this.traitementsEnCours  = traitementsEnCours;
        this.diagnostics         = diagnostics;
        this.notesMedecin        = notesMedecin;
        this.objectifSante       = objectifSante;
        this.niveauActivite      = niveauActivite;
        this.dateCreation        = dateCreation;
        this.derniereMiseAJour   = derniereMiseAJour;
    }

    // ── Utilitaire : date formatée pour l'affichage ──
    public String getDateCreationFormatee() {
        if (dateCreation == null) return "—";
        return dateCreation.toLocalDateTime().format(FMT);
    }

    public String getDerniereMiseAJourFormatee() {
        if (derniereMiseAJour == null) return "—";
        return derniereMiseAJour.toLocalDateTime().format(FMT);
    }

    // ── Getters / Setters ──
    public int       getId()                             { return id; }
    public void      setId(int id)                       { this.id = id; }
    public int       getUserId()                         { return userId; }
    public void      setUserId(int userId)               { this.userId = userId; }
    public String    getAntecedentsMedicaux()            { return antecedentsMedicaux; }
    public void      setAntecedentsMedicaux(String s)    { this.antecedentsMedicaux = s; }
    public String    getMaladiesChroniques()             { return maladiesChroniques; }
    public void      setMaladiesChroniques(String s)     { this.maladiesChroniques = s; }
    public String    getAllergies()                      { return allergies; }
    public void      setAllergies(String s)              { this.allergies = s; }
    public String    getTraitementsEnCours()             { return traitementsEnCours; }
    public void      setTraitementsEnCours(String s)     { this.traitementsEnCours = s; }
    public String    getDiagnostics()                    { return diagnostics; }
    public void      setDiagnostics(String s)            { this.diagnostics = s; }
    public String    getNotesMedecin()                   { return notesMedecin; }
    public void      setNotesMedecin(String s)           { this.notesMedecin = s; }
    public String    getObjectifSante()                  { return objectifSante; }
    public void      setObjectifSante(String s)          { this.objectifSante = s; }
    public String    getNiveauActivite()                 { return niveauActivite; }
    public void      setNiveauActivite(String s)         { this.niveauActivite = s; }
    public Timestamp getDateCreation()                   { return dateCreation; }
    public void      setDateCreation(Timestamp t)        { this.dateCreation = t; }
    public Timestamp getDerniereMiseAJour()              { return derniereMiseAJour; }
    public void      setDerniereMiseAJour(Timestamp t)   { this.derniereMiseAJour = t; }
}