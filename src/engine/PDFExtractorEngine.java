package engine;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class PDFExtractorEngine {

    public String extractAndSave(String pdfPath) {

        try (PDDocument document = Loader.loadPDF(new File(pdfPath))) {

            PDFTextStripper stripper = new PDFTextStripper();
            stripper.setSortByPosition(true);
            stripper.setLineSeparator(System.lineSeparator());

            String text = stripper.getText(document);

            // Save inside engine folder
            Path outputPath = Paths.get("src", "engine", "extracted.txt");

            Files.write(outputPath, text.getBytes());

            System.out.println("Extracted text saved to: "
                    + outputPath.toAbsolutePath());

            return text;

        } catch (Exception e) {
            System.err.println("PDF extraction error: " + e.getMessage());
            return null;
        }
    }
}
