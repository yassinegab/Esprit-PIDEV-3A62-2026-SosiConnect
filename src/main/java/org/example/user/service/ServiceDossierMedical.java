package org.example.user.service;

import org.example.user.model.DossierMedical;
import org.example.utils.MyConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ServiceDossierMedical {

    // ── Trouver le dernier dossier d'un utilisateur ──
    public DossierMedical findByUserId(int userId) throws SQLException {
        String sql = "SELECT * FROM dossier_medical WHERE user_id = ? " +
                "ORDER BY id DESC LIMIT 1";
        try (Connection cnx = MyConnection.getConnection();
             PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return mapRow(rs);
        }
        return null;
    }

    // ── Tous les dossiers d'un utilisateur ──
    public List<DossierMedical> findAllByUserId(int userId) throws SQLException {
        List<DossierMedical> list = new ArrayList<>();
        String sql = "SELECT * FROM dossier_medical WHERE user_id = ? ORDER BY id DESC";
        try (Connection cnx = MyConnection.getConnection();
             PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) list.add(mapRow(rs));
        }
        return list;
    }

    // ✅ NOUVEAU — Tous les dossiers (utilisé quand aucun utilisateur n'est connecté) ──
    public List<DossierMedical> findAll() throws SQLException {
        List<DossierMedical> list = new ArrayList<>();
        String sql = "SELECT * FROM dossier_medical ORDER BY id DESC";
        try (Connection cnx = MyConnection.getConnection();
             PreparedStatement ps = cnx.prepareStatement(sql)) {
            ResultSet rs = ps.executeQuery();
            while (rs.next()) list.add(mapRow(rs));
        }
        return list;
    }

    // ── Ajouter ──
    public void add(DossierMedical d) throws SQLException {
        String sql = "INSERT INTO dossier_medical " +
                "(user_id, antecedents_medicaux, maladies_chroniques, allergies, " +
                "traitements_en_cours, diagnostics, notes_medecin, " +
                "objectif_sante, niveau_activite, date_creation, derniere_mise_ajour) " +
                "VALUES (?,?,?,?,?,?,?,?,?,NOW(),NOW())";
        try (Connection cnx = MyConnection.getConnection();
             PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setInt   (1, d.getUserId());
            ps.setString(2, d.getAntecedentsMedicaux());
            ps.setString(3, d.getMaladiesChroniques());
            ps.setString(4, d.getAllergies());
            ps.setString(5, d.getTraitementsEnCours());
            ps.setString(6, d.getDiagnostics());
            ps.setString(7, d.getNotesMedecin());
            ps.setString(8, d.getObjectifSante());
            ps.setString(9, d.getNiveauActivite());
            ps.executeUpdate();
        }
    }

    // ── Modifier ──
    public void update(DossierMedical d) throws SQLException {
        String sql = "UPDATE dossier_medical SET " +
                "antecedents_medicaux=?, maladies_chroniques=?, allergies=?, " +
                "traitements_en_cours=?, diagnostics=?, notes_medecin=?, " +
                "objectif_sante=?, niveau_activite=?, derniere_mise_ajour=NOW() " +
                "WHERE id=?";
        try (Connection cnx = MyConnection.getConnection();
             PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setString(1, d.getAntecedentsMedicaux());
            ps.setString(2, d.getMaladiesChroniques());
            ps.setString(3, d.getAllergies());
            ps.setString(4, d.getTraitementsEnCours());
            ps.setString(5, d.getDiagnostics());
            ps.setString(6, d.getNotesMedecin());
            ps.setString(7, d.getObjectifSante());
            ps.setString(8, d.getNiveauActivite());
            ps.setInt   (9, d.getId());
            ps.executeUpdate();
        }
    }

    // ── Supprimer ──
    public void delete(int id) throws SQLException {
        String sql = "DELETE FROM dossier_medical WHERE id=?";
        try (Connection cnx = MyConnection.getConnection();
             PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        }
    }

    // ── Existe pour user ──
    public boolean existsForUser(int userId) throws SQLException {
        String sql = "SELECT COUNT(*) FROM dossier_medical WHERE user_id=?";
        try (Connection cnx = MyConnection.getConnection();
             PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getInt(1) > 0;
        }
        return false;
    }

    // ── Count total ──
    public int countDossiers() throws SQLException {
        String sql = "SELECT COUNT(*) FROM dossier_medical";
        try (Connection cnx = MyConnection.getConnection();
             PreparedStatement ps = cnx.prepareStatement(sql)) {
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getInt(1);
        }
        return 0;
    }

    // ── Count patients avec maladies chroniques ──
    public int countPatientsWithChronicDiseases() throws SQLException {
        String sql = "SELECT COUNT(*) FROM dossier_medical " +
                "WHERE maladies_chroniques IS NOT NULL " +
                "AND TRIM(maladies_chroniques) <> '' " +
                "AND LOWER(TRIM(maladies_chroniques)) <> 'aucune'";
        try (Connection cnx = MyConnection.getConnection();
             PreparedStatement ps = cnx.prepareStatement(sql)) {
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getInt(1);
        }
        return 0;
    }

    // ── Count patients avec allergies ──
    public int countPatientsWithAllergies() throws SQLException {
        String sql = "SELECT COUNT(*) FROM dossier_medical " +
                "WHERE allergies IS NOT NULL " +
                "AND TRIM(allergies) <> '' " +
                "AND LOWER(TRIM(allergies)) <> 'aucune'";
        try (Connection cnx = MyConnection.getConnection();
             PreparedStatement ps = cnx.prepareStatement(sql)) {
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getInt(1);
        }
        return 0;
    }

    // ── Stats par niveau d'activité ──
    public Map<String, Integer> getActiviteStats() throws SQLException {
        Map<String, Integer> stats = new HashMap<>();
        stats.put("Sédentaire", 0);
        stats.put("Léger",      0);
        stats.put("Modéré",     0);
        stats.put("Actif",      0);
        stats.put("Très actif", 0);

        String sql = "SELECT niveau_activite, COUNT(*) AS total " +
                "FROM dossier_medical GROUP BY niveau_activite";
        try (Connection cnx = MyConnection.getConnection();
             PreparedStatement ps = cnx.prepareStatement(sql)) {
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                String niveau = rs.getString("niveau_activite");
                int    total  = rs.getInt("total");
                if (niveau == null || niveau.isBlank()) {
                    stats.merge("Sédentaire", total, Integer::sum);
                } else {
                    String n = niveau.trim().toLowerCase();
                    if      (n.contains("sédentaire") || n.contains("sedentaire"))
                        stats.merge("Sédentaire", total, Integer::sum);
                    else if (n.contains("léger") || n.contains("leger"))
                        stats.merge("Léger",      total, Integer::sum);
                    else if (n.contains("modéré") || n.contains("modere"))
                        stats.merge("Modéré",     total, Integer::sum);
                    else if (n.contains("actif") && !n.contains("très") && !n.contains("tres"))
                        stats.merge("Actif",      total, Integer::sum);
                    else
                        stats.merge("Très actif", total, Integer::sum);
                }
            }
        }
        return stats;
    }

    // ── Mapper ResultSet → DossierMedical ──
    private DossierMedical mapRow(ResultSet rs) throws SQLException {
        DossierMedical d = new DossierMedical();
        d.setId                 (rs.getInt      ("id"));
        d.setUserId             (rs.getInt      ("user_id"));
        d.setAntecedentsMedicaux(rs.getString   ("antecedents_medicaux"));
        d.setMaladiesChroniques (rs.getString   ("maladies_chroniques"));
        d.setAllergies          (rs.getString   ("allergies"));
        d.setTraitementsEnCours (rs.getString   ("traitements_en_cours"));
        d.setDiagnostics        (rs.getString   ("diagnostics"));
        d.setNotesMedecin       (rs.getString   ("notes_medecin"));
        d.setObjectifSante      (rs.getString   ("objectif_sante"));
        d.setNiveauActivite     (rs.getString   ("niveau_activite"));
        d.setDateCreation       (rs.getTimestamp("date_creation"));
        d.setDerniereMiseAJour  (rs.getTimestamp("derniere_mise_ajour"));
        return d;
    }
}