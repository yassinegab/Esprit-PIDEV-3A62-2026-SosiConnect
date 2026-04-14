package org.example.aideEtdon.model;

import java.sql.Timestamp;

public class Demande {
    private int id;
    private String titre;
    private String description;
    private String type;
    private String groupeSanguin;
    private String organe;
    private String urgence;
    private Timestamp dateCreation;
    private int userId;

    public Demande() {}

    public Demande(int id, String titre, String description, String type, String groupeSanguin, String organe, String urgence, Timestamp dateCreation, int userId) {
        this.id = id;
        this.titre = titre;
        this.description = description;
        this.type = type;
        this.groupeSanguin = groupeSanguin;
        this.organe = organe;
        this.urgence = urgence;
        this.dateCreation = dateCreation;
        this.userId = userId;
    }

    public Demande(String titre, String description, String type, String groupeSanguin, String organe, String urgence, int userId) {
        this.titre = titre;
        this.description = description;
        this.type = type;
        this.groupeSanguin = groupeSanguin;
        this.organe = organe;
        this.urgence = urgence;
        this.userId = userId;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getTitre() { return titre; }
    public void setTitre(String titre) { this.titre = titre; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    public String getGroupeSanguin() { return groupeSanguin; }
    public void setGroupeSanguin(String groupeSanguin) { this.groupeSanguin = groupeSanguin; }
    public String getOrgane() { return organe; }
    public void setOrgane(String organe) { this.organe = organe; }
    public String getUrgence() { return urgence; }
    public void setUrgence(String urgence) { this.urgence = urgence; }
    public Timestamp getDateCreation() { return dateCreation; }
    public void setDateCreation(Timestamp dateCreation) { this.dateCreation = dateCreation; }
    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }

    @Override
    public String toString() {
        return "Demande{" +
                "id=" + id +
                ", titre='" + titre + '\'' +
                ", type='" + type + '\'' +
                ", urgence='" + urgence + '\'' +
                '}';
    }
}
