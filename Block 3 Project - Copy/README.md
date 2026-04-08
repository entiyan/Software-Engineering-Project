# JDM Patient Management System

A JavaFX desktop application for managing Juvenile Dermatomyositis (JDM) patient data,
built with object-oriented design principles and layered architecture.

---

## Project Structure

```
Block 3 Project - Copy/
├── build.ps1                     ← PowerShell build & run script
├── build_and_run.sh              ← Unix/Linux build & run script
├── README.md                     ← This file
├── data/                         ← CSV data files
│   ├── CMAS.csv
│   ├── LabResult.csv
│   ├── LabResultGroup.csv
│   ├── LabResults(EN).csv
│   ├── Measurement.csv
│   └── Patient.csv
├── javafx-sdk-26/                ← Bundled JavaFX SDK (JDK 17+)
│   ├── bin/
│   ├── legal/
│   └── lib/
└── src/jdm/
    ├── fx/                       ← JavaFX controllers & UI
    │   ├── MainApp.java          ← Application entry point
    │   ├── LoginController.java
    │   ├── DoctorController.java
    │   ├── PatientController.java
    │   └── SceneManager.java      ← Scene navigation management
    ├── model/                    ← Domain objects
    │   ├── SystemUser.java       ← User interface (Doctor/Patient implementations)
    │   ├── DoctorUser.java
    │   ├── PatientUser.java
    │   ├── UserFactory.java
    │   ├── Patient.java
    │   ├── Appointment.java
    │   ├── LabResult.java
    │   ├── LabResultGroup.java
    │   ├── CmasEntry.java
    │   └── Measurement.java
    ├── service/                  ← Business logic & data management
    │   ├── DataStore.java        ← Singleton in-memory repository
    │   ├── DataLoader.java       ← CSV loading & initialization
    │   ├── SessionContext.java   ← Session & user management
    │   ├── UserFactory.java      ← User creation factory
    │   ├── LabResultService.java ← Lab result operations
    │   ├── AppointmentService.java ← Appointment operations
    │   ├── ClinicalAnalyticsService.java ← Clinical analysis
    │   └── ReportingService.java ← Report generation
    ├── util/                     ← Utility classes
    │   ├── CsvParser.java        ← CSV parsing
    │   └── DataPaths.java        ← Data path management
    └── FXML files (in src/)
        ├── login.fxml            ← Login screen UI
        ├── doctor.fxml           ← Doctor dashboard UI
        └── patient.fxml          ← Patient dashboard UI
```

---

## Requirements

- Java JDK 17 or higher
- JavaFX SDK 26 (bundled in project)
- Windows, macOS, or Linux

---

## Build & Run

**Navigate to the project directory and run:**

```powershell
cd "Block 3 Project - Copy"
powershell -ExecutionPolicy Bypass -File .\build.ps1 -run
```

Or manually compile and run:

```powershell
mkdir out
$sources = Get-ChildItem -Path src -Recurse -Filter "*.java" | ForEach-Object { $_.FullName }
javac --release 17 -encoding UTF-8 -d out --module-path "javafx-sdk-26\lib" --add-modules javafx.controls,javafx.fxml @sources
java --module-path "javafx-sdk-26\lib" --add-modules javafx.controls,javafx.fxml -cp out jdm.fx.MainApp data
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

### Doctor Dashboard
- **View Patients**: Browse all patients with latest CMAS severity status
- **Lab Results Management**: 
  - Browse lab results grouped by category (Blood Chemistry, Hematology, etc.)
  - View all measurements for any lab result
  - Add new lab result types to a patient's record
  - Edit and delete measurement values
- **Appointment Management**: 
  - Schedule new appointments for patients
  - View all upcoming appointments across all patients
  - Cancel or complete appointments
- **Clinical Analytics**: Generate clinical reports and analysis

### Patient Dashboard
- **View Lab Results**: Browse own lab results (read-only)
- **CMAS History**: View CMAS trend history with severity labels
- **Appointment Management**: 
  - View and manage own appointments
  - Schedule new appointments
- **Clinical Reports**: Access personal clinical reports

---

## CMAS Severity Scale

| Score   | Severity  |
|---------|-----------|
| ≥ 48    | Remission |
| 40–47   | Mild      |
| 28–39   | Moderate  |
| < 28    | Severe    |

