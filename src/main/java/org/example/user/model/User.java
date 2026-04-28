package org.example.user.model;

import java.sql.Timestamp;

import java.sql.Timestamp;

public class User {

    private int id;
    private String nom;
    private String prenom;
    private String email;
    private String password;
    private String telephone;
    private UserRole role;
    private int age;
    private String sexe;
    private double poids;
    private double taille;
    private String handicap;
    private Timestamp dateCreation;
    private Timestamp derniereMiseAJour;

    public User() {
    }

    public User(int id, String nom, String prenom, String email, String password,
                String telephone, UserRole role, int age, String sexe,
                double poids, double taille, String handicap,
                Timestamp dateCreation, Timestamp derniereMiseAJour) {
        this.id = id;
        this.nom = nom;
        this.prenom = prenom;
        this.email = email;
        this.password = password;
        this.telephone = telephone;
        this.role = role;
        this.age = age;
        this.sexe = sexe;
        this.poids = poids;
        this.taille = taille;
        this.handicap = handicap;
        this.dateCreation = dateCreation;
        this.derniereMiseAJour = derniereMiseAJour;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }


    public String getNom() {
        return nom;
    }

    public void setNom(String nom) {
        this.nom = nom;
    }


    public String getPrenom() {
        return prenom;
    }

    public void setPrenom(String prenom) {
        this.prenom = prenom;
    }


    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }


    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }


    public String getTelephone() {
        return telephone;
    }

    public void setTelephone(String telephone) {
        this.telephone = telephone;
    }


    public UserRole getRole() {
        return role;
    }

    public void setRole(UserRole role) {
        this.role = role;
    }


    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
    }


    public String getSexe() {
        return sexe;
    }

    public void setSexe(String sexe) {
        this.sexe = sexe;
    }


    public double getPoids() {
        return poids;
    }

    public void setPoids(double poids) {
        this.poids = poids;
    }


    public double getTaille() {
        return taille;
    }

    public void setTaille(double taille) {
        this.taille = taille;
    }


    public String getHandicap() {
        return handicap;
    }

    public void setHandicap(String handicap) {
        this.handicap = handicap;
    }


    public Timestamp getDateCreation() {
        return dateCreation;
    }

    public void setDateCreation(Timestamp dateCreation) {
        this.dateCreation = dateCreation;
    }


    public Timestamp getDerniereMiseAJour() {
        return derniereMiseAJour;
    }

    public void setDerniereMiseAJour(Timestamp derniereMiseAJour) {
        this.derniereMiseAJour = derniereMiseAJour;
    }

    public String getNomComplet() {
        return nom + " " + prenom;
    }

  
}