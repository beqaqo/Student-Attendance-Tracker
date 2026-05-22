package com.undefinedvars.attendance.controller;

import com.undefinedvars.attendance.model.Student;
import com.undefinedvars.attendance.service.StudentService;

import javafx.beans.property.SimpleStringProperty;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;

import java.util.Objects;

/*
    Controller for the add-student screen.
    Translates between the UI and StudentService: read what the user
    typed, hand it to the service, reflect the result back on screen.
    No validation, no storage, no id generation. A thin coordinator.
*/
public final class StudentController {

    private final StudentService studentService;

    @FXML private TextField nameField;
    @FXML private TextField emailField;
    @FXML private Button addButton;
    @FXML private TableView<Student> studentTable;
    @FXML private TableColumn<Student, String> nameColumn;
    @FXML private TableColumn<Student, String> emailColumn;
    @FXML private Label errorLabel;

    public StudentController(StudentService studentService) {
        this.studentService = Objects.requireNonNull(
                studentService, "studentService cannot be null");
    }

    /*
        Initializes the controller after the FXML fields have been injected.
        Sets up the table columns to display student data and refreshes the table with existing students.
     */
    @FXML
    private void initialize() {
        nameColumn.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getFullName()));
        emailColumn.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getEmail()));
        refreshTable();
    }

    @FXML
    private void handleAdd() {
        try {
            studentService.register(nameField.getText(), emailField.getText());
            refreshTable();
            clearForm();
            errorLabel.setText("");
        } catch (RuntimeException ex) {
            errorLabel.setText(ex.getMessage());
        }
    }

    private void refreshTable() {
        studentTable.getItems().setAll(studentService.findAll());
    }

    private void clearForm() {
        nameField.clear();
        emailField.clear();
    }
}