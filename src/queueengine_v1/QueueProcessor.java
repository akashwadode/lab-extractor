package queueengine_v1;

import engine.PDFExtractorEngine;
import engine.MethodValidationEngine;

import java.util.List;
import java.util.Map;

public class QueueProcessor {

    private QueueDatabaseManager db = new QueueDatabaseManager();

    public void processNextReport() {

        QueueReport report = db.fetchNextPendingReport();

        if (report == null) {
            System.out.println("No pending reports found.");
            return;
        }

        System.out.println("=================================");
        System.out.println("Processing: " + report.filename);
        System.out.println("Lab ID    : " + report.labId);
        System.out.println("=================================");

        // STEP 1 - Extract PDF
        PDFExtractorEngine extractor = new PDFExtractorEngine();
        String text = extractor.extractAndSave(report.filepath);

        if (text == null) {
            db.updateStatus(report.reportId, "FAILED");
            System.out.println("PDF extraction failed.");
            return;
        }

        // STEP 2 - Validate Patient Name
        int patientId = 6; // change as needed

        String dbPatientName = db.findPatientNameById(patientId);

        if (dbPatientName == null) {
            db.updateStatus(report.reportId, "FAILED");
            System.out.println("Patient not found in DB.");
            return;
        }

        if (!normalize(text).contains(normalize(dbPatientName))) {

            db.updateStatus(report.reportId, "FAILED");

            System.out.println("\nERROR: Patient name mismatch!");
            System.out.println("Expected: " + dbPatientName);
            return;
        }

        System.out.println("Patient name verified.");

        // STEP 3 - Load Parameters
        Map<String, String> paramMap =
                db.loadParametersWithMethod(report.labId);

        // STEP 4 - Validate Methods
        MethodValidationEngine engine =
                new MethodValidationEngine();

        List<MethodValidationEngine.ValidationResult> results =
                engine.validate(text, paramMap);

        printProblems(results);

        db.updateStatus(report.reportId, "DONE");

        System.out.println("\nValidation Completed.");
    }

    private void printProblems(
            List<MethodValidationEngine.ValidationResult> results) {

        System.out.println("\n===== PROBLEM REPORT =====");

        boolean foundIssue = false;

        for (var r : results) {

            if (!r.status.equals("MATCH") &&
                !r.status.equals("MATCH (NO METHOD)")) {

                foundIssue = true;

                System.out.println("--------------------------------");
                System.out.println("Parameter : " + r.parameter);
                System.out.println("Value     : " + r.value);
                System.out.println("PDF Method: " + r.pdfMethod);
                System.out.println("DB Method : " + r.dbMethod);
                System.out.println("STATUS    : " + r.status);
            }
        }

        if (!foundIssue) {
            System.out.println("No method issues found.");
        }
    }

    private String normalize(String s) {
        if (s == null) return "";
        return s.replaceAll("[^a-zA-Z]", "")
                .toLowerCase();
    }
}