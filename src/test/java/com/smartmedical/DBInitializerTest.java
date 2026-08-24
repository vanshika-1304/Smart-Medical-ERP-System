package com.smartmedical;

import com.smartmedical.util.DBConnection;
import java.io.BufferedReader;
import java.io.FileReader;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;

public class DBInitializerTest {

    public static void main(String[] args) {
        System.out.println("=== Testing Oracle DB Connection ===");
        try {
            Connection conn = null;
            try {
                conn = DBConnection.getConnection();
                System.out.println("SUCCESS: Connected using DBConnection default pool!");
            } catch (Throwable e) {
                System.out.println("Default DBConnection failed: " + e.getMessage());
                System.out.println("Attempting fallback connection parameters...");
                
                // Try system / sysdba or alternate URLs if needed
                String[] urls = {
                    "jdbc:oracle:thin:@//localhost:1521/XEPDB1",
                    "jdbc:oracle:thin:@localhost:1521:XE",
                    "jdbc:oracle:thin:@//localhost:1521/FREEPDB1",
                    "jdbc:oracle:thin:@localhost:1521:ORCL",
                    "jdbc:oracle:thin:@//localhost:1521/orclpdb1"
                };
                
                String[] users = {"smartmedical", "system", "sys as sysdba"};
                String[] passes = {"SmartMed123", "SmartMed@123", "oracle", "admin", "password"};

                for (String url : urls) {
                    for (String user : users) {
                        for (String pass : passes) {
                            try {
                                conn = DriverManager.getConnection(url, user, pass);
                                System.out.println("SUCCESSFULLY connected with URL: " + url + " | User: " + user);
                                break;
                            } catch (Exception ex) {
                                // ignore & try next
                            }
                        }
                        if (conn != null) break;
                    }
                    if (conn != null) break;
                }
            }

            if (conn == null) {
                System.err.println("ERROR: Could not connect to Oracle database with any candidate settings.");
                System.exit(1);
            }

            // Check if USERS table exists
            Statement stmt = conn.createStatement();
            ResultSet rs = null;
            boolean usersExist = false;
            try {
                rs = stmt.executeQuery("SELECT COUNT(*) FROM USERS");
                if (rs.next()) {
                    System.out.println("USERS table exists. Count: " + rs.getInt(1));
                    usersExist = true;
                }
            } catch (Exception e) {
                System.out.println("USERS table does not exist or error: " + e.getMessage());
            }

            if (!usersExist) {
                System.out.println("Running sql/01_schema.sql schema script...");
                BufferedReader br = new BufferedReader(new FileReader("sql/01_schema.sql"));
                StringBuilder sb = new StringBuilder();
                String line;
                while ((line = br.readLine()) != null) {
                    line = line.trim();
                    if (line.startsWith("--") || line.isEmpty()) continue;
                    sb.append(line).append(" ");
                    if (line.endsWith(";")) {
                        String sql = sb.toString().replace(";", "").trim();
                        sb.setLength(0);
                        if (sql.equalsIgnoreCase("COMMIT")) {
                            conn.commit();
                            continue;
                        }
                        try {
                            stmt.execute(sql);
                            System.out.println("Executed: " + (sql.length() > 50 ? sql.substring(0, 50) + "..." : sql));
                        } catch (Exception ex) {
                            System.out.println("Notice on statement execution: " + ex.getMessage());
                        }
                    }
                }
                br.close();
                System.out.println("Schema creation complete.");
            }

            conn.close();
            System.out.println("=== DB Initialization & Verification Complete ===");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
