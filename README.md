# JDM Patient Management System

A Java CLI application for managing Juvenile Dermatomyositis (JDM) patient data,
built with object-oriented design principles.

---

## Project Structure

```
jdm_app/
├── build_and_run.sh              ← compile + run script
├── data/                         ← CSV data files (your dataset)
│   ├── Patient.csv
│   ├── LabResult.csv
│   ├── LabResultGroup.csv
│   ├── LabResults_EN_.csv
│   ├── Measurement.csv
│   └── 1775593554312_CMAS.csv
└── src/main/java/jdm/
    ├── Main.java                 ← entry point
    ├── model/                    ← domain objects
    │   ├── SystemUser.java       ← interface + Doctor/PatientUser impls
    │   ├── UserFactory.java
    │   ├── Patient.java
    │   ├── CmasEntry.java
    │   ├── LabResult.java
    │   ├── LabResultGroup.java
    │   ├── Measurement.java
    │   └── Appointment.java
    ├── service/                  ← business logic
    │   ├── DataStore.java        ← singleton in-memory repository
    │   ├── DataLoader.java       ← CSV → domain object loading
    │   ├── LabResultService.java
    │   └── AppointmentService.java
    ├── ui/                       ← CLI screens
    │   ├── LoginScreen.java
    │   ├── DoctorDashboard.java
    │   ├── PatientDashboard.java
    │   ├── LabResultsScreen.java
    │   ├── CmasScreen.java
    │   ├── AppointmentsScreen.java
    │   └── InputHelper.java
    └── util/
        ├── CsvParser.java        ← parses all CSV formats
        └── Display.java          ← ANSI colour helpers
```

---

## Requirements

- Java JDK 17 or higher
- Terminal with ANSI colour support (any modern terminal)

---

## Build & Run

```bash
chmod +x build_and_run.sh
./build_and_run.sh
```

Or manually:

```bash
mkdir -p out
find src -name "*.java" > sources.txt
javac --release 17 -d out @sources.txt
java -cp out jdm.Main data
```

---

## Login Credentials

### Doctors
| Username | Password   |
|----------|------------|
| doctor   | doctor123  |
| admin    | admin123   |

### Patients
| Patient ID                                    | Password   |
|-----------------------------------------------|------------|
| 55e2d179-d738-47d1-b88c-606833ce4d31 (Patient X) | patient123 |

*(Patient IDs are shown at application startup when data loads.)*

---

## Features by Role

### Doctor
- View all patients with latest CMAS severity status
- Browse lab results grouped by category (Blood Chemistry, Hematology, etc.)
- View all measurements for any lab result
- **Add** new lab result types to a patient record
- **Edit** measurement values
- **Delete** lab results or individual measurements
- Schedule / cancel / complete appointments for any patient
- View all upcoming appointments across all patients

### Patient
- View own lab results (read-only)
- View own CMAS history with severity labels and ASCII trend chart
- View and schedule own appointments

---

## OOP Design Highlights

| Principle       | Where applied |
|-----------------|---------------|
| Interface       | `SystemUser` — defines role contract; `DoctorUser` / `PatientUser` implement it |
| Factory         | `UserFactory.createDoctor()` / `createPatient()` |
| Singleton       | `DataStore` — single in-memory data repository |
| Encapsulation   | All models expose only necessary getters; lists returned as unmodifiable views |
| Separation of concerns | `model` ↔ `service` ↔ `ui` layers |
| Polymorphism    | UI screens accept `SystemUser`; behaviour differs by role without casting |

---

## CMAS Severity Scale

| Score   | Severity  |
|---------|-----------|
| ≥ 48    | Remission |
| 40–47   | Mild      |
| 28–39   | Moderate  |
| < 28    | Severe    |
