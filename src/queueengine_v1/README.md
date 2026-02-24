# DiagnoIQ - Queue Engine v1

## Overview

QueueEngine_v1 is an automated lab report processor.

It fetches a report from the database queue, extracts the PDF, validates patient name and test methods, and updates the report status.

---

## Files and Their Responsibilities

### 1. QueueRunner.java
- Entry point of the module
- Starts the queue processor
- Measures total execution time

---

### 2. QueueProcessor.java
- Main orchestration logic
- Fetches next PENDING report
- Extracts PDF text
- Validates patient name
- Loads parameters from database
- Validates test methods
- Prints mismatches
- Updates report status to DONE or FAILED

---

### 3. QueueDatabaseManager.java
- Handles all database operations
- Fetches next PENDING report from poc.reportqueue
- Loads patient name from poc.patient
- Loads parameters from poc.testgroup → poc.testparameter
- Updates report status

---

### 4. QueueReport.java
- Simple data model (DTO)
- Stores:
  - reportId
  - labId
  - filename
  - filepath

---

## Database Tables Used

- poc.reportqueue
- poc.patient
- poc.testgroup
- poc.testparameter

---

## How It Works

1. Fetch next PENDING report
2. Extract PDF text
3. Validate patient name
4. Load lab parameters
5. Validate methods
6. Print mismatches
7. Update report status

---

## Compile

javac -cp ".;lib/*" -d out src/engine/*.java src/queueengine_v1/*.java

---

## Run

java -cp ".;lib/*;out" queueengine_v1.QueueRunner