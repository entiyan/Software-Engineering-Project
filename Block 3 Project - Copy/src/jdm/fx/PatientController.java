package jdm.fx;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import jdm.model.*;
import jdm.service.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class PatientController {

    private static final DateTimeFormatter TIME_PARSE = DateTimeFormatter.ofPattern("H:mm");

    private final DataStore store = DataStore.getInstance();
    private final AppointmentService appointments = new AppointmentService();
    private final ReportingService reporting = new ReportingService();
    private final ClinicalAnalyticsService analytics = new ClinicalAnalyticsService();

    @FXML private Label welcomeLabel;
    @FXML private TextArea dossierArea;
    @FXML private TableView<LabResult> labTable;
    @FXML private TableView<CmasEntry> cmasTable;
    @FXML private javafx.scene.layout.VBox chartBox;
    @FXML private TableView<Appointment> apptTable;
    @FXML private DatePicker apDate;
    @FXML private TextField apTime;
    @FXML private TextField apReason;

    private LineChart<String, Number> chart;

    @FXML
    public void initialize() {
        SystemUser u = SessionContext.getCurrent();
        if (!(u instanceof PatientUser pu)) {
            SceneManager.switchScene("login.fxml", "JDM Healthcare — Login");
            return;
        }
        Patient p = store.getPatient(pu.getPatientId());
        if (p == null) {
            SceneManager.switchScene("login.fxml", "JDM Healthcare — Login");
            return;
        }

        welcomeLabel.setText("Welcome, " + p.getName());

        NumberAxis y = new NumberAxis(0, 52, 5);
        y.setLabel("CMAS score");
        chart = new LineChart<>(new javafx.scene.chart.CategoryAxis(), y);
        chart.setTitle("Your CMAS trend (primary row)");
        chart.setLegendVisible(false);
        chart.setPrefHeight(280);
        chartBox.getChildren().add(chart);

        initLabTable();
        initCmasTable();
        initApptTable();

        refresh(p);

        apDate.setValue(LocalDate.now());
        apTime.setPromptText("10:00");
    }

    private void initLabTable() {
        TableColumn<LabResult, String> n = new TableColumn<>("Analyte");
        n.setCellValueFactory(new PropertyValueFactory<>("resultNameEnglish"));
        n.setPrefWidth(260);
        TableColumn<LabResult, String> u = new TableColumn<>("Unit");
        u.setCellValueFactory(new PropertyValueFactory<>("unit"));
        TableColumn<LabResult, String> l = new TableColumn<>("Latest");
        l.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(c.getValue().getLatestValueString()));
        labTable.getColumns().setAll(n, u, l);
    }

    private void initCmasTable() {
        TableColumn<CmasEntry, String> d = new TableColumn<>("Date");
        d.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(c.getValue().getDate().toString()));
        TableColumn<CmasEntry, String> g = new TableColumn<>("Source row");
        g.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(c.getValue().getScoreGroup()));
        TableColumn<CmasEntry, Number> s = new TableColumn<>("Score");
        s.setCellValueFactory(c -> new javafx.beans.property.ReadOnlyObjectWrapper<>(c.getValue().getValue()));
        TableColumn<CmasEntry, String> sev = new TableColumn<>("Severity");
        sev.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(c.getValue().getSeverityLabel()));
        cmasTable.getColumns().setAll(d, g, s, sev);
    }

    private void initApptTable() {
        TableColumn<Appointment, String> w = new TableColumn<>("When");
        w.setCellValueFactory(c -> {
            LocalDateTime t = c.getValue().getDateTime();
            return new javafx.beans.property.SimpleStringProperty(t == null ? "" : t.toString().replace('T', ' '));
        });
        TableColumn<Appointment, String> r = new TableColumn<>("Reason");
        r.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(c.getValue().getReason()));
        TableColumn<Appointment, String> st = new TableColumn<>("Status");
        st.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(c.getValue().getStatus().name()));
        apptTable.getColumns().setAll(w, r, st);
    }

    private void refresh(Patient p) {
        dossierArea.setText(reporting.buildPatientDossier(p));

        List<LabResult> labs = store.getLabResultsForPatient(p.getPatientId());
        labTable.setItems(FXCollections.observableArrayList(labs));

        List<CmasEntry> cmas = p.getCmasEntriesSorted();
        cmasTable.setItems(FXCollections.observableArrayList(cmas));

        XYChart.Series<String, Number> series = new XYChart.Series<>();
        for (CmasEntry e : analytics.primaryCmasSeries(p)) {
            series.getData().add(new XYChart.Data<>(e.getDate().toString(), e.getValue()));
        }
        chart.getData().clear();
        if (!series.getData().isEmpty()) chart.getData().add(series);

        List<Appointment> ap = appointments.getForPatient(p.getPatientId()).stream()
                .sorted(Comparator.comparing(a -> a.getDateTime() != null
                        ? a.getDateTime() : LocalDateTime.MIN))
                .collect(Collectors.toList());
        apptTable.setItems(FXCollections.observableArrayList(ap));
    }

    @FXML
    private void scheduleOwn() {
        SystemUser u = SessionContext.getCurrent();
        if (!(u instanceof PatientUser pu)) return;
        Patient p = store.getPatient(pu.getPatientId());
        if (p == null) return;

        LocalDate d = apDate.getValue();
        if (d == null) {
            new Alert(Alert.AlertType.WARNING, "Pick a date.").showAndWait();
            return;
        }
        LocalTime time;
        try {
            time = LocalTime.parse(apTime.getText().trim(), TIME_PARSE);
        } catch (DateTimeParseException e) {
            new Alert(Alert.AlertType.WARNING, "Use time like 9:30 or 15:00").showAndWait();
            return;
        }
        String doc = "Care team";
        appointments.schedule(p.getPatientId(), LocalDateTime.of(d, time),
                apReason.getText() == null ? "" : apReason.getText().trim(), doc);
        refresh(p);
    }

    @FXML
    private void logout() {
        SessionContext.clear();
        SceneManager.switchScene("login.fxml", "JDM Healthcare — Login");
    }
}
