package org.example.cycle.model;

import java.sql.Date;

public class Symptome {

    private int idSymptome;
    private int cycleId; // foreign key
    private TypeSymptome type;
    private IntensiteSymptome intensite;
    private Date dateObservation;

    // Empty constructor
    public Symptome() {
    }

    // Constructor without ID (for INSERT)
    public Symptome(int cycleId, TypeSymptome type, IntensiteSymptome intensite, Date dateObservation) {
        this.cycleId = cycleId;
        this.type = type;
        this.intensite = intensite;
        this.dateObservation = dateObservation;
    }

    // Constructor with ID (for SELECT)
    public Symptome(int idSymptome, int cycleId, TypeSymptome type, IntensiteSymptome intensite, Date dateObservation) {
        this.idSymptome = idSymptome;
        this.cycleId = cycleId;
        this.type = type;
        this.intensite = intensite;
        this.dateObservation = dateObservation;
    }

    // Getters and Setters
    public int getIdSymptome() {
        return idSymptome;
    }

    public void setIdSymptome(int idSymptome) {
        this.idSymptome = idSymptome;
    }

    public int getCycleId() {
        return cycleId;
    }

    public void setCycleId(int cycleId) {
        this.cycleId = cycleId;
    }

    public TypeSymptome getType() {
        return type;
    }

    public void setType(TypeSymptome type) {
        this.type = type;
    }

    public IntensiteSymptome getIntensite() {
        return intensite;
    }

    public void setIntensite(IntensiteSymptome intensite) {
        this.intensite = intensite;
    }

    public Date getDateObservation() {
        return dateObservation;
    }

    public void setDateObservation(Date dateObservation) {
        this.dateObservation = dateObservation;
    }

    @Override
    public String toString() {
        return "Symptome{" +
                "idSymptome=" + idSymptome +
                ", cycleId=" + cycleId +
                ", type='" + type + '\'' +
                ", intensite='" + intensite + '\'' +
                ", dateObservation=" + dateObservation +
                '}';
    }
}