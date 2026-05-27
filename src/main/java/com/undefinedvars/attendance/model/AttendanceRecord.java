package com.undefinedvars.attendance.model;

import com.undefinedvars.attendance.util.Preconditions;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Objects;

public final class AttendanceRecord {
    private final String id;
    private final Student student;
    // private final Lecture lecture; // kept for future lecture-based tracking
    private final LocalDate date;
    private final AttendanceStatus status;
    private final LocalDateTime markedAt;

    private AttendanceRecord(String id, Student student, /*Lecture lecture,*/ LocalDate date, AttendanceStatus status, LocalDateTime markedAt) {
        this.id = id;
        this.student = student;
        // this.lecture = lecture;
        this.date = date;
        this.status = status;
        this.markedAt = markedAt;
    }

    public static Builder builder() {return new Builder();}

    public String getId() {return id;}

    public Student getStudent() {
        return student;
    }

    // public Lecture getLecture() { return lecture; } // kept for future lecture-based tracking

    public LocalDate getDate() {
        return date;
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
        // private Lecture lecture; // kept for future lecture-based tracking
        private LocalDate date;
        private AttendanceStatus status;
        private LocalDateTime markedAt;

        public Builder id(String id) {this.id = id; return this;}
        public Builder student(Student student) {this.student = student; return this;}
        // public Builder lecture(Lecture lecture) {this.lecture = lecture; return this;} // kept for future lecture-based tracking
        public Builder date(LocalDate date) {this.date = date; return this;}
        public Builder status(AttendanceStatus status) {this.status = status; return this;}
        public Builder markedAt(LocalDateTime markedAt) {this.markedAt = markedAt; return this;}

        public AttendanceRecord build() {
            Preconditions.requireNonBlank(id, "id");
            Objects.requireNonNull(student, "student cannot be null");
            // Objects.requireNonNull(lecture, "lecture cannot be null"); // kept for future lecture-based tracking
            Objects.requireNonNull(date, "date cannot be null");
            Objects.requireNonNull(status, "status cannot be null");

            if (date.isAfter(LocalDate.now())) {
                throw new IllegalArgumentException("date cannot be in the future");
            }

            LocalDateTime currentMarkingDate = LocalDateTime.now(); // capture LocalDateTime.now() once to avoid the race
            LocalDateTime effectiveMarkingDateTime = (markedAt != null) ? markedAt : currentMarkingDate;

            if (effectiveMarkingDateTime.isAfter(currentMarkingDate)) {
                throw new IllegalArgumentException("markedAt cannot be in the future");
            }

            return new AttendanceRecord(id, student, /*lecture,*/ date, status, effectiveMarkingDateTime);
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
