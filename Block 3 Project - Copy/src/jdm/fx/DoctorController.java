package jdm.fx;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;
import javafx.stage.FileChooser;
import jdm.model.*;
import jdm.service.*;

import java.io.File;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.*;
import java.util.stream.Collectors;

public class DoctorController {

    private static final DateTimeFormatter TIME_PARSE = DateTimeFormatter.ofPattern("H:mm");

    private final DataStore store = DataStore.getInstance();
    private final LabResultService labService = new LabResultService();
    private final AppointmentService appointmentService = new AppointmentService();
    private final ReportingService reporting = new ReportingService();
    private final ClinicalAnalyticsService analytics = new ClinicalAnalyticsService();

    @FXML private ComboBox<Patient> patientCombo;
    @FXML private TextArea summaryArea;
    @FXML private TableView<LabResult> labTable;
    @FXML private TableView<Measurement> measurementTable;
    @FXML private javafx.scene.layout.VBox cmasChartParent;
    @FXML private TableView<CmasEntry> cmasTable;
    @FXML private TableView<Appointment> appointmentTable;
    @FXML private DatePicker apDate;
    @FXML private TextField apTime;
    @FXML private TextField apReason;
    @FXML private TextArea analyticsArea;
    @FXML private ComboBox<String> biomarkerCombo;
    @FXML private DatePicker measDate;
    @FXML private TextField measValue;
    @FXML private TextArea measurementHint;

    private LineChart<String, Number> cmasLineChart;

    @FXML
    public void initialize() {
        initLabColumns();
        initMeasurementColumns();
        initCmasColumns();
        initAppointmentColumns();

        NumberAxis yAxis = new NumberAxis(0, 52, 5);
        yAxis.setLabel("CMAS score");
        CategoryAxis xAxis = new CategoryAxis();
        xAxis.setLabel("Visit date");
        cmasLineChart = new LineChart<>(xAxis, yAxis);
        cmasLineChart.setTitle("CMAS over time (primary row)");
        cmasLineChart.setLegendVisible(false);
        cmasLineChart.setPrefHeight(320);
        cmasLineChart.setMinHeight(280);
        cmasChartParent.getChildren().add(0, cmasLineChart);

        biomarkerCombo.setItems(FXCollections.observableArrayList(ClinicalAnalyticsService.HIGHLIGHT_BIOMARKERS));
        biomarkerCombo.getSelectionModel().selectFirst();
        measDate.setValue(LocalDate.now());

        patientCombo.setCellFactory(lv -> new ListCell<>() {
            @Override protected void updateItem(Patient p, boolean empty) {
                super.updateItem(p, empty);
                setText(empty || p == null ? null : p.toString());
            }
        });
        patientCombo.setButtonCell(new ListCell<>() {
            @Override protected void updateItem(Patient p, boolean empty) {
                super.updateItem(p, empty);
                setText(empty || p == null ? null : p.toString());
            }
        });

        refreshPatientList();
        patientCombo.getSelectionModel().selectedItemProperty().addListener((o, a, p) -> refreshPatientContext());

        apDate.setValue(LocalDate.now());
        apTime.setPromptText("14:30");
    }

    private void initLabColumns() {
        TableColumn<LabResult, String> name = new TableColumn<>("Analyte");
        name.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(
                c.getValue().getResultNameEnglish()));
        name.setPrefWidth(220);
        TableColumn<LabResult, String> grp = new TableColumn<>("Group");
        grp.setCellValueFactory(c -> {
            LabResultGroup g = store.getLabResultGroup(c.getValue().getGroupId());
            return new javafx.beans.property.SimpleStringProperty(g != null ? g.getGroupName() : "");
        });
        grp.setPrefWidth(160);
        TableColumn<LabResult, String> unit = new TableColumn<>("Unit");
        unit.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(c.getValue().getUnit()));
        TableColumn<LabResult, Number> n = new TableColumn<>("N");
        n.setCellValueFactory(c -> new javafx.beans.property.ReadOnlyObjectWrapper<>(
                c.getValue().getMeasurements().size()));
        TableColumn<LabResult, String> latest = new TableColumn<>("Latest");
        latest.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(c.getValue().getLatestValueString()));
        labTable.getColumns().setAll(name, grp, unit, n, latest);
        labTable.getSelectionModel().selectedItemProperty().addListener((o, a, row) -> showMeasurements(row));
    }

    private void initMeasurementColumns() {
        TableColumn<Measurement, String> when = new TableColumn<>("Date/time");
        when.setCellValueFactory(c -> {
            LocalDateTime dt = c.getValue().getDateTime();
            return new javafx.beans.property.SimpleStringProperty(dt == null ? "" : dt.toString().replace('T', ' '));
        });
        when.setPrefWidth(160);
        TableColumn<Measurement, String> val = new TableColumn<>("Value");
        val.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(c.getValue().getValue()));
        val.setPrefWidth(200);
        TableColumn<Measurement, String> id = new TableColumn<>("Measurement ID");
        id.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(c.getValue().getMeasurementId()));
        id.setPrefWidth(260);
        measurementTable.getColumns().setAll(when, val, id);
    }

    private void initCmasColumns() {
        TableColumn<CmasEntry, String> d = new TableColumn<>("Date");
        d.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(c.getValue().getDate().toString()));
        TableColumn<CmasEntry, String> g = new TableColumn<>("Row label");
        g.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(c.getValue().getScoreGroup()));
        g.setPrefWidth(140);
        TableColumn<CmasEntry, Number> sc = new TableColumn<>("Score");
        sc.setCellValueFactory(c -> new javafx.beans.property.ReadOnlyObjectWrapper<>(c.getValue().getValue()));
        TableColumn<CmasEntry, String> sev = new TableColumn<>("Severity");
        sev.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(c.getValue().getSeverityLabel()));
        cmasTable.getColumns().setAll(d, g, sc, sev);
    }

    private void initAppointmentColumns() {
        TableColumn<Appointment, String> dt = new TableColumn<>("When");
        dt.setCellValueFactory(c -> {
            LocalDateTime t = c.getValue().getDateTime();
            return new javafx.beans.property.SimpleStringProperty(t == null ? "" : t.toString().replace('T', ' '));
        });
        dt.setPrefWidth(150);
        TableColumn<Appointment, String> rs = new TableColumn<>("Reason");
        rs.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(c.getValue().getReason()));
        TableColumn<Appointment, String> doc = new TableColumn<>("Clinician");
        doc.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(c.getValue().getDoctorName()));
        TableColumn<Appointment, String> st = new TableColumn<>("Status");
        st.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(c.getValue().getStatus().name()));
        appointmentTable.getColumns().setAll(dt, rs, doc, st);
    }

    private void showMeasurements(LabResult row) {
        if (row == null) {
            measurementTable.setItems(FXCollections.observableArrayList());
            measurementHint.clear();
            return;
        }
        List<Measurement> sorted = row.getMeasurements().stream()
                .sorted(Comparator.comparing(Measurement::getDateTime,
                        Comparator.nullsLast(Comparator.reverseOrder())))
                .collect(Collectors.toList());
        measurementTable.setItems(FXCollections.observableArrayList(sorted));
        measurementHint.setText(reporting.measurementDetail(row));
    }

    @FXML
    private void refreshPatientList() {
        List<Patient> list = new ArrayList<>(store.getAllPatients());
        list.sort(Comparator.comparing(Patient::getName, String.CASE_INSENSITIVE_ORDER));
        patientCombo.setItems(FXCollections.observableArrayList(list));
        if (!list.isEmpty()) patientCombo.getSelectionModel().selectFirst();
    }

    private void refreshPatientContext() {
        Patient p = patientCombo.getSelectionModel().getSelectedItem();
        if (p == null) return;
        summaryArea.setText(reporting.buildPatientDossier(p));
        labTable.setItems(FXCollections.observableArrayList(labService.getForPatient(p.getPatientId())));
        showMeasurements(null);

        List<CmasEntry> cmas = p.getCmasEntriesSorted();
        cmasTable.setItems(FXCollections.observableArrayList(cmas));
        updateCmasChart(p);

        List<Appointment> ap = appointmentService.getForPatient(p.getPatientId());
        appointmentTable.setItems(FXCollections.observableArrayList(ap));

        analyticsArea.setText(analytics.cohortSummary() + "\n── Selected patient ──\n"
                + runCorrelationFor(p));
    }

    private void updateCmasChart(Patient p) {
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("CMAS");
        for (CmasEntry e : analytics.primaryCmasSeries(p)) {
            series.getData().add(new XYChart.Data<>(e.getDate().toString(), e.getValue()));
        }
        cmasLineChart.getData().clear();
        if (!series.getData().isEmpty()) cmasLineChart.getData().add(series);
    }

    private String runCorrelationFor(Patient p) {
        String bio = biomarkerCombo.getSelectionModel().getSelectedItem();
        if (bio == null) return "";
        return analytics.cmasBiomarkerSameDay(p, bio).toString();
    }

    @FXML
    private void runCorrelation() {
        Patient p = patientCombo.getSelectionModel().getSelectedItem();
        if (p == null) return;
        analyticsArea.setText(analytics.cohortSummary() + "\n── Selected patient ──\n" + runCorrelationFor(p));
    }

    @FXML
    private void exportDossier() {
        Patient p = patientCombo.getSelectionModel().getSelectedItem();
        if (p == null) return;
        FileChooser ch = new FileChooser();
        ch.setTitle("Export dossier");
        ch.setInitialFileName("jdm-dossier-" + p.getPatientId().substring(0, 8) + ".txt");
        File f = ch.showSaveDialog(patientCombo.getScene().getWindow());
        if (f == null) return;
        try {
            reporting.exportText(f.toPath(), reporting.buildPatientDossier(p));
            new Alert(Alert.AlertType.INFORMATION, "Saved to " + f.getAbsolutePath()).showAndWait();
        } catch (Exception e) {
            new Alert(Alert.AlertType.ERROR, e.getMessage()).showAndWait();
        }
    }

    @FXML
    private void scheduleAppointment() {
        Patient p = patientCombo.getSelectionModel().getSelectedItem();
        if (p == null) return;
        LocalDate d = apDate.getValue();
        if (d == null) {
            new Alert(Alert.AlertType.WARNING, "Pick a date.").showAndWait();
            return;
        }
        LocalTime time;
        try {
            time = LocalTime.parse(apTime.getText().trim(), TIME_PARSE);
        } catch (DateTimeParseException ex) {
            new Alert(Alert.AlertType.WARNING, "Time must look like 9:30 or 14:15 (H:mm).").showAndWait();
            return;
        }
        LocalDateTime dt = LocalDateTime.of(d, time);
        String docName = SessionContext.getCurrent() != null
                ? SessionContext.getCurrent().getDisplayName() : "Clinician";
        String reason = apReason.getText() == null ? "" : apReason.getText().trim();
        appointmentService.schedule(p.getPatientId(), dt, reason, docName);
        appointmentTable.setItems(FXCollections.observableArrayList(
                appointmentService.getForPatient(p.getPatientId())));
        new Alert(Alert.AlertType.INFORMATION, "Appointment scheduled.").showAndWait();
    }

    @FXML
    private void editSelectedMeasurement() {
        LabResult lr = labTable.getSelectionModel().getSelectedItem();
        Measurement m = measurementTable.getSelectionModel().getSelectedItem();
        if (lr == null || m == null) return;
        TextInputDialog dlg = new TextInputDialog(m.getValue());
        dlg.setTitle("Edit measurement");
        dlg.setHeaderText("New value");
        dlg.showAndWait().ifPresent(v -> {
            if (v != null && labService.editMeasurementValue(lr.getLabResultId(), m.getMeasurementId(), v.trim())) {
                labTable.refresh();
                showMeasurements(labService.getById(lr.getLabResultId()));
                Patient p = patientCombo.getSelectionModel().getSelectedItem();
                if (p != null) summaryArea.setText(reporting.buildPatientDossier(p));
            }
        });
    }

    @FXML
    private void deleteSelectedMeasurement() {
        LabResult lr = labTable.getSelectionModel().getSelectedItem();
        Measurement m = measurementTable.getSelectionModel().getSelectedItem();
        if (lr == null || m == null) return;
        if (labService.deleteMeasurement(lr.getLabResultId(), m.getMeasurementId())) {
            labTable.refresh();
            showMeasurements(labService.getById(lr.getLabResultId()));
            refreshPatientContext();
        }
    }

    @FXML
    private void addMeasurement() {
        LabResult lr = labTable.getSelectionModel().getSelectedItem();
        if (lr == null) {
            new Alert(Alert.AlertType.WARNING, "Select a lab row first.").showAndWait();
            return;
        }
        LocalDate d = measDate.getValue();
        if (d == null || measValue.getText() == null || measValue.getText().isBlank()) {
            new Alert(Alert.AlertType.WARNING, "Enter date and value.").showAndWait();
            return;
        }
        LocalDateTime dt = d.atTime(9, 0);
        labService.addMeasurement(lr.getLabResultId(), dt, measValue.getText().trim());
        labTable.refresh();
        showMeasurements(labService.getById(lr.getLabResultId()));
        summaryArea.setText(reporting.buildPatientDossier(patientCombo.getSelectionModel().getSelectedItem()));
    }

    @FXML
    private void addNewLabResult() {
        Patient p = patientCombo.getSelectionModel().getSelectedItem();
        if (p == null) return;
        TextInputDialog nameDlg = new TextInputDialog();
        nameDlg.setTitle("New analyte");
        nameDlg.setHeaderText("English name (e.g. CXCL10)");
        Optional<String> name = nameDlg.showAndWait();
        if (name.isEmpty() || name.get().isBlank()) return;
        List<LabResultGroup> groups = new ArrayList<>(store.getAllGroups());
        ChoiceDialog<LabResultGroup> grpDlg = new ChoiceDialog<>(
                groups.isEmpty() ? null : groups.get(0), groups);
        grpDlg.setTitle("Lab group");
        grpDlg.setHeaderText("Choose a group");
        Optional<LabResultGroup> g = grpDlg.showAndWait();
        if (g.isEmpty()) return;
        TextInputDialog unitDlg = new TextInputDialog("pg/ml");
        unitDlg.setTitle("Unit");
        Optional<String> unit = unitDlg.showAndWait();
        labService.addLabResult(g.get().getGroupId(), p.getPatientId(), name.get().trim(),
                unit.orElse("").trim());
        refreshPatientContext();
    }

    @FXML
    private void deleteSelectedLab() {
        LabResult lr = labTable.getSelectionModel().getSelectedItem();
        if (lr == null) return;
        labService.deleteLabResult(lr.getLabResultId());
        refreshPatientContext();
    }

    @FXML
    private void logout() {
        SessionContext.clear();
        SceneManager.switchScene("login.fxml", "JDM Healthcare — Login");
    }
}
