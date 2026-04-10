package org.example.main;

import org.example.utils.MyConnection;
import java.sql.Connection;

public class Main {
    public static void main(String[] args) {

        try {

            Connection conn = MyConnection.getConnection();


            if (conn != null) {
                System.out.println("Connexion réussie !");
            }


            conn.close();

        } catch (Exception e) {
            System.out.println("Erreur : " + e.getMessage());
        }

    }
}