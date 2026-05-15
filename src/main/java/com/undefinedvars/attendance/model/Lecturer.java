package com.undefinedvars.attendance.model;

import com.undefinedvars.attendance.util.Preconditions;

public final class Lecturer extends Person {
    private final String title;
    private final String department;

    private Lecturer(String id, String fullName, String email, String title, String department) {
        super(id, fullName, email);
        this.title = title;
        this.department = department;
    }

    public static Builder builder() {return new Builder();}

    public String getTitle() {
        return title;
    }

    public String getDepartment() {
        return department;
    }

    public static final class Builder {
        private String id;
        private String fullName;
        private String email;
        private String title;
        private String department;

        public Builder id(String id) {this.id = id; return this;}
        public Builder fullName(String fullName) {this.fullName = fullName; return this;}
        public Builder email(String email) {this.email = email; return this;}
        public Builder title(String title) {this.title = title; return this;}
        public Builder department(String department) {this.department = department; return this;}

        /*
            SUBJECT TO CHANGE: Both title and department are required fields,
            but we might want to make them optional in the future.
         */
        public Lecturer build() {
            Preconditions.requireNonBlank(title, "title");
            Preconditions.requireNonBlank(department, "department");

            return new Lecturer(id, fullName, email, title, department);
        }
    }
}
