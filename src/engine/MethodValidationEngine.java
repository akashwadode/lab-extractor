package engine;

import org.ahocorasick.trie.Emit;
import org.ahocorasick.trie.Trie;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MethodValidationEngine {

    // =========================================================
    // RESULT MODEL
    // =========================================================
    public static class ValidationResult {
        public String parameter;
        public String value;
        public String pdfMethod;
        public String dbMethod;
        public String status;

        public ValidationResult(String parameter,
                String value,
                String pdfMethod,
                String dbMethod,
                String status) {

            this.parameter = parameter;
            this.value = value;
            this.pdfMethod = pdfMethod;
            this.dbMethod = dbMethod;
            this.status = status;
        }
    }

    // =========================================================
    // READ TEXT FILE
    // =========================================================
    public String readTextFile(String path) {

        try {
            String text = new String(Files.readAllBytes(Paths.get(path)));
            System.out.println("Text loaded successfully.");
            return text;
        } catch (Exception e) {
            System.out.println("Error reading file: " + e.getMessage());
            return null;
        }
    }

    // =========================================================
    // EXTRACT NUMERIC VALUE
    // =========================================================
    private String extractValue(String line) {
        Matcher m = Pattern.compile("([-+]?[0-9]*\\.?[0-9]+)").matcher(line);
        if (m.find())
            return m.group(1);
        return null;
    }

    // =========================================================
    // NORMALIZATION (Safe Comparison)
    // Removes symbols, spaces, case differences
    // =========================================================
    private String normalize(String s) {
        if (s == null)
            return "";
        return s.replaceAll("[^a-zA-Z]", "").toLowerCase();
    }

    // =========================================================
    // EXTRACT METHOD FROM LINE (DB-Driven)
    // =========================================================
    private String extractMethod(String line, Set<String> knownMethods) {

        String lowerLine = line.toLowerCase();

        for (String method : knownMethods) {
            if (method == null || method.trim().isEmpty())
                continue;

            if (lowerLine.contains(method.toLowerCase())) {
                return method;
            }
        }

        return null;
    }

    // =========================================================
    // MAIN VALIDATION LOGIC (UPDATED)
    // =========================================================
    public List<ValidationResult> validate(String text,
            Map<String, String> paramMap) {

        Set<String> knownMethods = new HashSet<>(paramMap.values());

        Trie.TrieBuilder builder = Trie.builder()
                .ignoreCase()
                .onlyWholeWords();

        for (String param : paramMap.keySet()) {
            builder.addKeyword(param);
        }

        Trie trie = builder.build();
        Collection<Emit> emits = trie.parseText(text);

        Set<String> processed = new HashSet<>();
        List<ValidationResult> results = new ArrayList<>();

        for (Emit emit : emits) {

            String paramName = emit.getKeyword();

            if (processed.contains(paramName))
                continue;

            int lineStart = text.lastIndexOf("\n", emit.getStart());
            int lineEnd = text.indexOf("\n", emit.getEnd());

            if (lineStart == -1)
                lineStart = 0;
            if (lineEnd == -1)
                lineEnd = text.length();

            String line = text.substring(lineStart, lineEnd).trim();

            String value = extractValue(line);

            // Skip title/header rows (no numeric value)
            if (value == null)
                continue;

            processed.add(paramName);

            String pdfMethod = extractMethod(line, knownMethods);
            String dbMethod = paramMap.get(paramName);

            boolean dbEmpty = (dbMethod == null || dbMethod.trim().isEmpty());
            boolean pdfEmpty = (pdfMethod == null || pdfMethod.trim().isEmpty());

            String status;

            // =====================================================
            // 🔥 UPDATED DECISION LOGIC
            // =====================================================

            // CASE 1: Both NULL → VALID (No method defined anywhere)
            if (dbEmpty && pdfEmpty) {
                status = "MATCH (NO METHOD)";
            }

            // CASE 2: DB missing but PDF has method
            else if (dbEmpty) {
                status = "DB METHOD MISSING";
            }

            // CASE 3: PDF missing but DB has method
            else if (pdfEmpty) {
                status = "PDF METHOD NOT FOUND";
            }

            // CASE 4: Both exist → Compare
            else if (normalize(pdfMethod).equals(normalize(dbMethod))) {
                status = "MATCH";
            }

            // CASE 5: Both exist but different
            else {
                status = "MISMATCH";
            }

            results.add(new ValidationResult(
                    paramName,
                    value,
                    pdfMethod,
                    dbMethod,
                    status));
        }

        return results;
    }
}
