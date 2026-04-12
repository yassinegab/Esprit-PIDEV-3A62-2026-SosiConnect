package org.example;

import org.example.utils.MyConnection;
import java.sql.Connection;

public class TestConnection {
    public static void main(String[] args) {
        try {
            Connection conn = MyConnection.getConnection();

            if (conn != null) {
                System.out.println("✅ Connected successfully!");
            } else {
                System.out.println("❌ Connection is NULL!");
            }

        } catch (Exception e) {
            System.out.println("❌ Connection failed!");
            e.printStackTrace();
        }
    }
}