package engine;

import java.util.List;
import java.util.Map;

public class EngineRunner {

    // ======================================================
    // NORMALIZE STRING (Ignore dots, spaces, case)
    // ======================================================
    private static String normalize(String s) {
        if (s == null) return "";
        return s.replaceAll("[^a-zA-Z]", "")
                .toLowerCase();
    }

    // ======================================================
    // MAIN
    // ======================================================
    public static void main(String[] args) {

        System.out.println("=================================");
        System.out.println(" LAB METHOD VALIDATION ENGINE ");
        System.out.println("=================================");

        int labId = 1;
        int patientId = 1;   // YOU PROVIDE THIS
        String pdfPath = "pdf/report.pdf";

        // ======================================================
        // STEP 1 - Extract PDF
        // ======================================================
        PDFExtractorEngine extractor = new PDFExtractorEngine();
        String text = extractor.extractAndSave(pdfPath);

        if (text == null) {
            System.out.println("PDF extraction failed.");
            return;
        }

        // ======================================================
        // STEP 2 - Fetch Patient Name From DB
        // ======================================================
        DatabaseManager db = new DatabaseManager();

        String dbPatientName = db.findPatientNameById(patientId);

        if (dbPatientName == null) {
            System.out.println("Patient ID not found in database.");
            return;
        }

        System.out.println("Patient Name in DB: " + dbPatientName);

        // ======================================================
        // STEP 3 - Check If DB Name Exists In PDF Text
        // ======================================================
        if (!normalize(text).contains(normalize(dbPatientName))) {

            System.out.println("\n❌ ERROR: Patient name not found in PDF!");
            System.out.println("Expected Name: " + dbPatientName);
            System.out.println("Processing stopped.");
            return;
        }

        System.out.println("✅ Patient name found in PDF. Proceeding...\n");

        // ======================================================
        // STEP 4 - Load DB Parameters
        // ======================================================
        Map<String, String> paramMap =
                db.loadParametersWithMethod(labId);

        System.out.println("Loaded parameters from DB: "
                + paramMap.size());

        // ======================================================
        // STEP 5 - Validate Methods
        // ======================================================
        MethodValidationEngine engine =
                new MethodValidationEngine();

        List<MethodValidationEngine.ValidationResult> results =
                engine.validate(text, paramMap);

        // ======================================================
        // STEP 6 - PROCESS + INSERT
        // ======================================================
        System.out.println("\n========= PROCESSING RESULTS =========");

        int insertedCount = 0;
        int skippedCount = 0;

        for (var r : results) {

            System.out.println("-----------------------------------");
            System.out.println("Parameter : " + r.parameter);
            System.out.println("Value     : " + r.value);
            System.out.println("PDF Method: " + r.pdfMethod);
            System.out.println("DB Method : " + r.dbMethod);
            System.out.println("STATUS    : " + r.status);

            if (r.status.equals("MATCH") ||
                r.status.equals("MATCH (NO METHOD)")) {

                if (r.value != null) {

                    try {
                        double numericValue =
                                Double.parseDouble(r.value);

                        db.insertPatientResult(
                                patientId,
                                labId,
                                r.parameter,
                                numericValue
                        );

                        insertedCount++;

                    } catch (NumberFormatException e) {

                        System.out.println(
                                "Invalid numeric value for: "
                                        + r.parameter);

                        skippedCount++;
                    }

                } else {
                    skippedCount++;
                }

            } else {

                System.out.println(
                        "❌ NOT INSERTED due to method issue: "
                                + r.parameter);

                skippedCount++;
            }
        }

        // ======================================================
        // FINAL SUMMARY
        // ======================================================
        System.out.println("\n=================================");
        System.out.println(" PROCESS SUMMARY ");
        System.out.println("=================================");
        System.out.println("Total Parameters Found : " + results.size());
        System.out.println("Inserted               : " + insertedCount);
        System.out.println("Skipped                : " + skippedCount);
        System.out.println("=================================");
    }
}
