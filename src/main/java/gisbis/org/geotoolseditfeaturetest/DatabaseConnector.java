package gisbis.org.geotoolseditfeaturetest;

import java.sql.*;
import java.util.UUID;

public class DatabaseConnector {

    private static final String DB_URL = "jdbc:postgresql://db:5432/postgres";
    private static final String USER = "postgres";
    private static final String PASS = "postgres";

    public String getRowById(String id) throws SQLException {
        String query = "SELECT ST_AsText(ST_GeomFromEWKB(geometry)) FROM bis.test WHERE id = ?";

        try (Connection conn = DriverManager.getConnection(DB_URL, USER, PASS);
             PreparedStatement pstmt = conn.prepareStatement(query)) {

            pstmt.setObject(1, UUID.fromString(id));

            try (ResultSet rs = pstmt.executeQuery()) {
                if (!rs.next())
                    return null;

                return rs.getObject(1, String.class);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            throw e;
        }
    }

    public Connection getConnection() throws SQLException {
        return DriverManager.getConnection(DB_URL, USER, PASS);
    }
}
