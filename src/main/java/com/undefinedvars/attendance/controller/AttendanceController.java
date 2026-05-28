package com.undefinedvars.attendance.controller;

import com.undefinedvars.attendance.model.AttendanceRecord;
import com.undefinedvars.attendance.model.AttendanceStatus;
import com.undefinedvars.attendance.model.Student;
import com.undefinedvars.attendance.service.AttendanceService;
import com.undefinedvars.attendance.service.StudentService;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/*
    Controller for the attendance-marking screen.
    Loads all students when a date is picked, pre-fills existing statuses,
    and saves via markBulk() when the user clicks Save.
    Thin coordinator — all business logic lives in AttendanceService.
 */
public final class AttendanceController {

    /*
        A simple row model: one Student + a mutable status property
        so the ComboBox in the table cell can bind to it.
     */
    public static final class StudentRow {
        private final Student student;
        private final ObjectProperty<AttendanceStatus> status;

        public StudentRow(Student student, AttendanceStatus initialStatus) {
            this.student = student;
            this.status = new SimpleObjectProperty<>(initialStatus);
        }

        public Student getStudent() { return student; }
        public ObjectProperty<AttendanceStatus> statusProperty() { return status; }
        public AttendanceStatus getStatus() { return status.get(); }
        public void setStatus(AttendanceStatus s) { status.set(s); }
    }

    private final AttendanceService attendanceService;
    private final StudentService studentService;

    @FXML private DatePicker datePicker;
    @FXML private TableView<StudentRow> attendanceTable;
    @FXML private TableColumn<StudentRow, String> nameColumn;
    @FXML private TableColumn<StudentRow, AttendanceStatus> statusColumn;
    @FXML private Button saveButton;
    @FXML private Label statusLabel;

    public AttendanceController(AttendanceService attendanceService, StudentService studentService) {
        this.attendanceService = Objects.requireNonNull(attendanceService, "attendanceService cannot be null");
        this.studentService = Objects.requireNonNull(studentService, "studentService cannot be null");
    }

    /*
        Called by JavaFX after FXML fields are injected.
        Sets up columns, defaults the picker to today, and loads the first batch.
     */
    @FXML
    private void initialize() {
        // Name column — just show the student's full name
        nameColumn.setCellValueFactory(cell ->
                new SimpleStringProperty(cell.getValue().getStudent().getFullName()));

        // Status column — a ComboBox per row so the user can pick inline
        statusColumn.setCellValueFactory(cell -> cell.getValue().statusProperty());
        statusColumn.setCellFactory(col -> new TableCell<>() {
            private final ComboBox<AttendanceStatus> combo =
                    new ComboBox<>(FXCollections.observableArrayList(AttendanceStatus.values()));

            {
                combo.setOnAction(e -> {
                    StudentRow row = getTableRow().getItem();
                    if (row != null) {
                        row.setStatus(combo.getValue());
                    }
                });
            }

            @Override
            protected void updateItem(AttendanceStatus item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || getTableRow() == null || getTableRow().getItem() == null) {
                    setGraphic(null);
                } else {
                    combo.setValue(item != null ? item : AttendanceStatus.PRESENT);
                    setGraphic(combo);
                }
            }
        });

        // Default to today and load immediately
        datePicker.setValue(LocalDate.now());
        loadForDate(LocalDate.now());
    }

    /*
        Triggered when the user changes the date in the DatePicker.
        Reloads the student list and pre-fills any existing statuses for that date.
     */
    @FXML
    private void handleDateChange() {
        LocalDate selected = datePicker.getValue();
        if (selected == null) return;
        if (selected.isAfter(LocalDate.now())) {
            statusLabel.setStyle("-fx-text-fill: #e74c3c;");
            statusLabel.setText("Cannot mark attendance for a future date.");
            attendanceTable.getItems().clear();
            return;
        }
        statusLabel.setText("");
        loadForDate(selected);
    }

    /*
        Triggered when the user clicks "Save Attendance".
        Collects the current status from every row and calls markBulk.
     */
    @FXML
    private void handleSave() {
        LocalDate date = datePicker.getValue();
        if (date == null) {
            statusLabel.setStyle("-fx-text-fill: #e74c3c;");
            statusLabel.setText("Please select a date first.");
            return;
        }
        if (attendanceTable.getItems().isEmpty()) {
            statusLabel.setStyle("-fx-text-fill: #e74c3c;");
            statusLabel.setText("No students to save. Add students first.");
            return;
        }
        try {
            Map<Student, AttendanceStatus> statuses = new HashMap<>();
            for (StudentRow row : attendanceTable.getItems()) {
                statuses.put(row.getStudent(), row.getStatus());
            }
            attendanceService.markBulk(date, statuses);
            statusLabel.setStyle("-fx-text-fill: #27ae60;");
            statusLabel.setText("Attendance saved for " + date + ".");
        } catch (RuntimeException ex) {
            statusLabel.setStyle("-fx-text-fill: #e74c3c;");
            statusLabel.setText(ex.getMessage());
        }
    }

    // -------------------------------------------------------------------------
    // Private helpers
    // -------------------------------------------------------------------------

    /*
        Loads every student, looks up their existing record for the given date,
        and builds a StudentRow with the pre-filled status (PRESENT if none exists).
     */
    private void loadForDate(LocalDate date) {
        List<AttendanceRecord> existing = attendanceService.findByDate(date);

        // Build a lookup map: studentId → status
        Map<String, AttendanceStatus> existingMap = new HashMap<>();
        for (AttendanceRecord r : existing) {
            existingMap.put(r.getStudent().getId(), r.getStatus());
        }

        List<StudentRow> rows = new ArrayList<>();
        for (Student student : studentService.findAll()) {
            AttendanceStatus status = existingMap.getOrDefault(student.getId(), AttendanceStatus.PRESENT);
            rows.add(new StudentRow(student, status));
        }
        attendanceTable.getItems().setAll(rows);
    }
}
