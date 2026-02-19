# Lab Report Extraction Engine

Java-based PDF lab report extraction and validation engine.

## Features
- PDF text extraction (Apache PDFBox)
- Parameter detection using Aho-Corasick
- Method validation against DB
- Mismatch reporting
- Designed for multi-lab support (DiagnoIQ)

## Tech Stack
- Java
- PDFBox
- PostgreSQL
- HikariCP

## Run
```bash
javac -encoding UTF-8 -cp "lib/*" -d bin src/engine/*.java
java -cp "bin;lib/*" engine.EngineRunner
