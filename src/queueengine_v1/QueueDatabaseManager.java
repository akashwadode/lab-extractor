package queueengine_v1;

import engine.DatabaseManager;

import java.sql.*;
import java.util.HashMap;
import java.util.Map;

public class QueueDatabaseManager extends DatabaseManager {

    // =========================================
    // FETCH NEXT PENDING REPORT
    // =========================================
    public QueueReport fetchNextPendingReport() {

        String query = "SELECT reportid, labid, filename " +
                "FROM poc.reportqueue " +
                "WHERE status = 'PENDING' " +
                "ORDER BY uploadtime ASC LIMIT 1";

        try (Connection conn = getConnection();
                PreparedStatement ps = conn.prepareStatement(query);
                ResultSet rs = ps.executeQuery()) {

            if (rs.next()) {

                int id = rs.getInt("reportid");
                int labId = rs.getInt("labid");
                String filename = rs.getString("filename");

                String filepath = "pdf/" + filename;

                return new QueueReport(id, labId, filename, filepath);
            }

        } catch (Exception e) {
            System.out.println("Queue fetch error: " + e.getMessage());
        }

        return null;
    }

    // =========================================
    // UPDATE STATUS
    // =========================================
    public void updateStatus(int reportId, String status) {

        String query = "UPDATE poc.reportqueue SET status = ?, " +
                "processedtime = CURRENT_TIMESTAMP " +
                "WHERE reportid = ?";

        try (Connection conn = getConnection();
                PreparedStatement ps = conn.prepareStatement(query)) {

            ps.setString(1, status);
            ps.setInt(2, reportId);
            ps.executeUpdate();

        } catch (Exception e) {
            System.out.println("Status update error: " + e.getMessage());
        }
    }

    // =========================================
    // FIND PATIENT NAME
    // =========================================
    public String findPatientNameById(int patientId) {

        String query = "SELECT name FROM poc.patient WHERE patientid = ?";

        try (Connection conn = getConnection();
                PreparedStatement ps = conn.prepareStatement(query)) {

            ps.setInt(1, patientId);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                return rs.getString("name");
            }

        } catch (Exception e) {
            System.out.println("Patient fetch error: " + e.getMessage());
        }

        return null;
    }

    // =========================================
    // LOAD PARAMETERS WITH METHOD
    // =========================================
    public Map<String, String> loadParametersWithMethod(int labId) {

        Map<String, String> paramMap = new HashMap<>();

        String query = "SELECT tp.parametername, tp.method " +
                "FROM poc.testparameter tp " +
                "JOIN poc.testgroup tg ON tp.testgroupid = tg.testgroupid " +
                "WHERE tg.labid = ?";

        try (Connection conn = getConnection();
                PreparedStatement ps = conn.prepareStatement(query)) {

            ps.setInt(1, labId);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                paramMap.put(
                        rs.getString("parametername"),
                        rs.getString("method"));
            }

        } catch (Exception e) {
            System.out.println("Parameter load error: " + e.getMessage());
        }

        return paramMap;
    }

    // =========================================
    // DATABASE CONNECTION
    // =========================================
    public Connection getConnection() throws SQLException {
        return DriverManager.getConnection(
                "jdbc:postgresql://localhost:5432/patient_data",
                "postgres",
                "ajw12345");
    }
}