package com.undefinedvars.attendance.controller;

import com.undefinedvars.attendance.service.AbsenceStatistics;
import com.undefinedvars.attendance.service.AttendanceService;
import com.undefinedvars.attendance.service.StudentService;

import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;

import java.util.Objects;

/*
    Controller for the absence statistics screen.
    It reads students and attendance records from the services and displays
    calculated absence totals and percentages.
 */
public final class StatisticsController {

    private final AttendanceService attendanceService;
    private final StudentService studentService;

    @FXML private TableView<AbsenceStatistics.StudentAbsenceStats> statisticsTable;
    @FXML private TableColumn<AbsenceStatistics.StudentAbsenceStats, String> nameColumn;
    @FXML private TableColumn<AbsenceStatistics.StudentAbsenceStats, Number> totalColumn;
    @FXML private TableColumn<AbsenceStatistics.StudentAbsenceStats, Number> absentColumn;
    @FXML private TableColumn<AbsenceStatistics.StudentAbsenceStats, Number> lateColumn;
    @FXML private TableColumn<AbsenceStatistics.StudentAbsenceStats, String> absenceRateColumn;
    @FXML private Button refreshButton;
    @FXML private Label summaryLabel;

    public StatisticsController(AttendanceService attendanceService, StudentService studentService) {
        this.attendanceService = Objects.requireNonNull(attendanceService, "attendanceService cannot be null");
        this.studentService = Objects.requireNonNull(studentService, "studentService cannot be null");
    }

    @FXML
    private void initialize() {
        nameColumn.setCellValueFactory(cell ->
                new SimpleStringProperty(cell.getValue().getStudent().getFullName()));

        totalColumn.setCellValueFactory(cell ->
                new SimpleIntegerProperty(cell.getValue().getTotalMarked()));

        absentColumn.setCellValueFactory(cell ->
                new SimpleIntegerProperty(cell.getValue().getAbsentCount()));

        lateColumn.setCellValueFactory(cell ->
                new SimpleIntegerProperty(cell.getValue().getLateCount()));

        absenceRateColumn.setCellValueFactory(cell ->
                new SimpleStringProperty(cell.getValue().getAbsenceRateText()));

        refreshStatistics();
    }

    @FXML
    private void handleRefresh() {
        refreshStatistics();
    }

    private void refreshStatistics() {
        var students = studentService.findAll();
        var records = attendanceService.findAll();

        var stats = AbsenceStatistics.calculate(students, records);
        statisticsTable.getItems().setAll(stats);

        int totalAbsences = 0;
        int totalLate = 0;

        for (AbsenceStatistics.StudentAbsenceStats stat : stats) {
            totalAbsences += stat.getAbsentCount();
            totalLate += stat.getLateCount();
        }

        summaryLabel.setText("Students: " + students.size()
                + " | Attendance records: " + records.size()
                + " | Absences: " + totalAbsences
                + " | Late marks: " + totalLate);
    }
}