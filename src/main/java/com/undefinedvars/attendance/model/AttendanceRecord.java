package com.undefinedvars.attendance.model;

import com.undefinedvars.attendance.util.Preconditions;

import java.time.LocalDateTime;
import java.util.Objects;

public final class AttendanceRecord {
    private final String id;
    private final Student student;
    private final Lecture lecture;
    private final AttendanceStatus status;
    private final LocalDateTime markedAt;

    private AttendanceRecord(String id, Student student, Lecture lecture, AttendanceStatus status, LocalDateTime markedAt) {
        this.id = id;
        this.student = student;
        this.lecture = lecture;
        this.status = status;
        this.markedAt = markedAt;
    }

    public static Builder builder() {return new Builder();}

    public String getId() {return id;}

    public Student getStudent() {
        return student;
    }

    public Lecture getLecture() {
        return lecture;
    }

    public AttendanceStatus getStatus() {
        return status;
    }

    public LocalDateTime getMarkedAt() {
        return markedAt;
    }

    public static final class Builder {
        private String id;
        private Student student;
        private Lecture lecture;
        private AttendanceStatus status;
        private LocalDateTime markedAt;

        public Builder id(String id) {this.id = id; return this;}
        public Builder student(Student student) {this.student = student; return this;}
        public Builder lecture(Lecture lecture) {this.lecture = lecture; return this;}
        public Builder status(AttendanceStatus status) {this.status = status; return this;}
        public Builder markedAt(LocalDateTime markedAt) {this.markedAt = markedAt; return this;}

        public AttendanceRecord build() {
            Preconditions.requireNonBlank(id, "id");
            Objects.requireNonNull(student, "student cannot be null");
            Objects.requireNonNull(lecture, "lecture cannot be null");
            Objects.requireNonNull(status, "status cannot be null");
            LocalDateTime currentMarkingDate = LocalDateTime.now(); // capture LocalDateTime.now() once to avoid the race
            LocalDateTime effectiveMarkingDateTime = (markedAt != null) ? markedAt : currentMarkingDate;

            if (effectiveMarkingDateTime.isAfter(currentMarkingDate)) {
                throw new IllegalArgumentException("markedAt cannot be in the future");
            }

            return new AttendanceRecord(id, student, lecture, status, effectiveMarkingDateTime);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof AttendanceRecord record)) return false;
        return Objects.equals(id, record.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}
