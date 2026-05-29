package com.undefinedvars.attendance.persistence;

import com.undefinedvars.attendance.model.AttendanceStatus;

import java.time.LocalDate;
import java.time.LocalDateTime;

/*
    This is a simple DTO (Data Transfer Object) class that represents an AttendanceRecord in the persistence layer.
    It has the same fields as the AttendanceRecord model, but without any business logic or validation.
 */
public final class AttendanceRecordDto {
    private String id;
    private String studentId;
    private LocalDate date;
    private AttendanceStatus status;
    private LocalDateTime markedAt;

    public AttendanceRecordDto() {}

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getStudentId() {
        return studentId;
    }

    public void setStudentId(String studentId) {
        this.studentId = studentId;
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public AttendanceStatus getStatus() {
        return status;
    }

    public void setStatus(AttendanceStatus status) {
        this.status = status;
    }

    public LocalDateTime getMarkedAt() {
        return markedAt;
    }

    public void setMarkedAt(LocalDateTime markedAt) {
        this.markedAt = markedAt;
    }
}
