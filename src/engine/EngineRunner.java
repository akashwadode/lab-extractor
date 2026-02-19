// javac -encoding UTF-8 -cp "lib/*" -d bin src\engine\*.java
// java -cp "bin;lib/*" engine.EngineRunner

package engine;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class EngineRunner {

    public static void main(String[] args) {

        System.out.println("=================================");
        System.out.println(" LAB METHOD VALIDATION ENGINE ");
        System.out.println("=================================");

        int labId = 1;
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
        // STEP 2 - Load DB Parameters
        // ======================================================
        DatabaseManager db = new DatabaseManager();
        Map<String, String> paramMap =
                db.loadParametersWithMethod(labId);

        System.out.println("Loaded parameters from DB: "
                + paramMap.size());

        // ======================================================
        // STEP 3 - Validate
        // ======================================================
        MethodValidationEngine engine =
                new MethodValidationEngine();

        List<MethodValidationEngine.ValidationResult> results =
                engine.validate(text, paramMap);

        // ======================================================
        // STEP 4 - Full Output
        // ======================================================
        System.out.println("\n========= FULL VALIDATION OUTPUT =========");

        int matchCount = 0;
        int mismatchCount = 0;
        int dbMissingCount = 0;
        int pdfMissingCount = 0;

        List<MethodValidationEngine.ValidationResult> mismatches = new ArrayList<>();
        List<MethodValidationEngine.ValidationResult> dbMissing = new ArrayList<>();
        List<MethodValidationEngine.ValidationResult> pdfMissing = new ArrayList<>();

        for (var r : results) {

            System.out.println("-----------------------------------");
            System.out.println("Parameter : " + r.parameter);
            System.out.println("Value     : " + r.value);
            System.out.println("PDF Method: " + r.pdfMethod);
            System.out.println("DB Method : " + r.dbMethod);
            System.out.println("STATUS    : " + r.status);

            switch (r.status) {

                case "MATCH":
                case "MATCH (NO METHOD)":
                    matchCount++;
                    break;

                case "MISMATCH":
                    mismatchCount++;
                    mismatches.add(r);
                    break;

                case "DB METHOD MISSING":
                    dbMissingCount++;
                    dbMissing.add(r);
                    break;

                case "PDF METHOD NOT FOUND":
                    pdfMissingCount++;
                    pdfMissing.add(r);
                    break;
            }
        }

        // ======================================================
        // FILTERED ERROR SECTION
        // ======================================================
        System.out.println("\n=================================");
        System.out.println(" FILTERED ERROR REPORT ");
        System.out.println("=================================");

        if (!mismatches.isEmpty()) {
            System.out.println("\n--- MISMATCHED METHODS ---");
            for (var r : mismatches) {
                System.out.println(r.parameter +
                        " | PDF: " + r.pdfMethod +
                        " | DB: " + r.dbMethod);
            }
        }

        if (!dbMissing.isEmpty()) {
            System.out.println("\n--- DB METHOD MISSING ---");
            for (var r : dbMissing) {
                System.out.println(r.parameter +
                        " | PDF: " + r.pdfMethod);
            }
        }

        if (!pdfMissing.isEmpty()) {
            System.out.println("\n--- PDF METHOD NOT FOUND ---");
            for (var r : pdfMissing) {
                System.out.println(r.parameter +
                        " | DB: " + r.dbMethod);
            }
        }

        if (mismatches.isEmpty() && dbMissing.isEmpty() && pdfMissing.isEmpty()) {
            System.out.println("No validation issues found.");
        }

        // ======================================================
        // FINAL SUMMARY
        // ======================================================
        System.out.println("\n=================================");
        System.out.println(" VALIDATION SUMMARY ");
        System.out.println("=================================");
        System.out.println("Total Parameters Checked : " + results.size());
        System.out.println("MATCH                    : " + matchCount);
        System.out.println("MISMATCH                 : " + mismatchCount);
        System.out.println("DB METHOD MISSING        : " + dbMissingCount);
        System.out.println("PDF METHOD NOT FOUND     : " + pdfMissingCount);
        System.out.println("=================================");
    }
}
