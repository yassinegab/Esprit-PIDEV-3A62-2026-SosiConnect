package org.example.aideEtdon.model;

import java.sql.Timestamp;

public class Don {
    private int id;
    private int demandeId;
    private int donorId;
    private String message;
    private Timestamp date;

    public Don() {}

    public Don(int id, int demandeId, int donorId, String message, Timestamp date) {
        this.id = id;
        this.demandeId = demandeId;
        this.donorId = donorId;
        this.message = message;
        this.date = date;
    }

    public Don(int demandeId, int donorId, String message) {
        this.demandeId = demandeId;
        this.donorId = donorId;
        this.message = message;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public int getDemandeId() { return demandeId; }
    public void setDemandeId(int demandeId) { this.demandeId = demandeId; }
    public int getDonorId() { return donorId; }
    public void setDonorId(int donorId) { this.donorId = donorId; }
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
    public Timestamp getDate() { return date; }
    public void setDate(Timestamp date) { this.date = date; }

    @Override
    public String toString() {
        return "Don{" +
                "id=" + id +
                ", demandeId=" + demandeId +
                ", donorId=" + donorId +
                '}';
    }
}
