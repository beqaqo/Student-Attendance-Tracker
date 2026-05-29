package com.undefinedvars.attendance.persistence;

import java.time.LocalDate;

/*
    This is a simple DTO (Data Transfer Object) class that represents a Student in the persistence layer.
    It has the same fields as the Student model, but without any business logic or validation.
 */
public final class StudentDto {
    private String id;
    private String fullName;
    private String email;
    private String groupId;
    private LocalDate enrolledOn;

    public StudentDto() {}

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getGroupId() {
        return groupId;
    }

    public void setGroupId(String groupId) {
        this.groupId = groupId;
    }

    public LocalDate getEnrolledOn() {
        return enrolledOn;
    }

    public void setEnrolledOn(LocalDate enrolledOn) {
        this.enrolledOn = enrolledOn;
    }
}
