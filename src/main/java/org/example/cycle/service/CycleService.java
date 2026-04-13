package org.example.cycle.service;
import org.example.cycle.model.Cycle;
import org.example.utils.MyConnection;

import java.sql.*;
        import java.util.*;

public class CycleService {

    Connection conn;

    public CycleService() {
        try {
            conn = MyConnection.getConnection();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }



    // CREATE
    public void addCycle(Cycle c) {
        try {
            String sql = "INSERT INTO cycle (date_debut_m, date_fin_m, user_id) VALUES (?, ?, ?)";
            PreparedStatement ps = conn.prepareStatement(sql);

            ps.setDate(1, c.getDate_debut_m());
            ps.setDate(2, c.getDate_fin_m());
            ps.setInt(3, c.getUser_id());


            int rows = ps.executeUpdate();

            System.out.println("ROWS INSERTED = " + rows);

        } catch (Exception e) {
            System.out.println("INSERT FAILED ❌");
            e.printStackTrace();
        }
    }

    // READ
    public List<Cycle> getAllCycles() {

        List<Cycle> list = new ArrayList<>();

        try {
            String sql = "SELECT * FROM cycle";
            Statement st = conn.createStatement();
            ResultSet rs = st.executeQuery(sql);

            while (rs.next()) {

                Cycle c = new Cycle();

                c.setCycle_id(rs.getInt("id_cycle"));
                c.setDate_debut_m(rs.getDate("date_debut_m"));
                c.setDate_fin_m(rs.getDate("date_fin_m"));
                c.setUser_id(rs.getInt("user_id"));

                list.add(c);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return list;
    }

    // DELETE
    public void deleteCycle(int id) {

        try {
            String sql = "DELETE FROM cycle WHERE id_cycle=?";
            PreparedStatement ps = conn.prepareStatement(sql);

            ps.setInt(1, id);

            ps.executeUpdate();

            System.out.println("Cycle deleted ^_^");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

//update
public void updateCycle(Cycle c) {

    try {
        String sql = "UPDATE cycle SET date_debut_m=?, date_fin_m=? WHERE id_cycle=?";

        PreparedStatement ps = conn.prepareStatement(sql);

        ps.setDate(1, c.getDate_debut_m());
        ps.setDate(2, c.getDate_fin_m());
        ps.setInt(3, c.getCycle_id()); // ✅ IMPORTANT

        ps.executeUpdate();

        System.out.println("Cycle updated ✔");

    } catch (Exception e) {
        e.printStackTrace();
    }
}
}