package com.undefinedvars.attendance.model;

import com.undefinedvars.attendance.util.Preconditions;

import java.util.Objects;

public final class Course {
    private final String id;
    private final String title;
    private final Lecturer lecturer;
    private final Group group;

    private Course(String id, String title, Lecturer lecturer, Group group) {
        this.id = id;
        this.title = title;
        this.lecturer = lecturer;
        this.group = group;
    }

    public static Builder builder() {return new Builder();}

    public String getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public Lecturer getLecturer() {
        return lecturer;
    }

    public Group getGroup() {
        return group;
    }

    public static final class Builder {
        private String id;
        private String title;
        private Lecturer lecturer;
        private Group group;

        public Builder id(String id) {this.id = id; return this;}
        public Builder title(String title) {this.title = title; return this;}
        public Builder lecturer(Lecturer lecturer) {this.lecturer = lecturer; return this;}
        public Builder group(Group group) {this.group = group; return this;}

        public Course build() {
            Preconditions.requireNonBlank(id, "id");
            Preconditions.requireNonBlank(title, "title");
            Objects.requireNonNull(lecturer, "lecturer cannot be null");
            Objects.requireNonNull(group, "group cannot be null");

            return new Course(id, title, lecturer, group);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof Course course)) return false;
        return Objects.equals(id, course.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}
