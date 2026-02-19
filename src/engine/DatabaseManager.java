package engine;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import java.sql.*;
import java.util.HashMap;
import java.util.Map;

public class DatabaseManager {

    private static final String DB_URL = "jdbc:postgresql://localhost:5432/patient_data";
    private static final String DB_USER = "postgres";
    private static final String DB_PASSWORD = "ajw12345";

    private static HikariDataSource dataSource;

    static {
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(DB_URL);
        config.setUsername(DB_USER);
        config.setPassword(DB_PASSWORD);
        config.setMaximumPoolSize(5);
        dataSource = new HikariDataSource(config);
    }

    public Map<String, String> loadParametersWithMethod(int labId) {

        Map<String, String> map = new HashMap<>();

        String query =
                "SELECT tp.ParameterName, tp.Method " +
                "FROM poc.TestGroup tg " +
                "JOIN poc.TestParameter tp ON tg.TestGroupId = tp.TestGroupId " +
                "WHERE tg.LabID = ?";

        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {

            ps.setInt(1, labId);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                map.put(
                        rs.getString("ParameterName"),
                        rs.getString("Method")
                );
            }

            System.out.println("Loaded parameters from DB: " + map.size());

        } catch (SQLException e) {
            System.err.println("DB Error: " + e.getMessage());
        }

        return map;
    }
}
