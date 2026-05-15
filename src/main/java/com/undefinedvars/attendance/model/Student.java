package com.undefinedvars.attendance.model;

import java.time.LocalDate;
import java.util.Objects;

public final class Student extends Person {
    private final Group group;
    private final LocalDate enrolledOn;

    private Student(String id, String fullName, String email, Group group, LocalDate enrolledOn) {
        super(id, fullName, email);
        this.group = group;
        this.enrolledOn = enrolledOn;
    }

    public static Builder builder() {return new Builder();}

    public Group getGroup() {
        return group;
    }

    public LocalDate getEnrolledOn() {
        return enrolledOn;
    }

    public static final class Builder {
        private String id;
        private String fullName;
        private String email;
        private Group group;
        private LocalDate enrolledOn;

        public Builder id(String id) {this.id = id; return this;}
        public Builder fullName(String fullName) {this.fullName = fullName; return this;}
        public Builder email(String email) {this.email = email; return this;}
        public Builder group(Group group) {this.group = group; return this;}
        public Builder enrolledOn(LocalDate enrolledOn) {this.enrolledOn = enrolledOn; return this;}

        public Student build() {
            /*
                Person-specific fields are validated in the Person class,
                so we don't have to do it here and repeat ourselves (DRY principle)
             */
            Objects.requireNonNull(group, "group cannot be null");
            LocalDate today = LocalDate.now(); // capture LocalDate.now() once to avoid the race
            LocalDate effectiveEnrolledOn = (enrolledOn != null) ? enrolledOn : today;

            /*
                Cannot enroll from the future
             */
            if (effectiveEnrolledOn.isAfter(today)) {
                throw new IllegalArgumentException("enrolledOn cannot be in the future");
            }

            return new Student(id, fullName, email, group, effectiveEnrolledOn);
        }
    }
}