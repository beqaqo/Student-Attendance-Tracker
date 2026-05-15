package com.undefinedvars.attendance.model;

import com.undefinedvars.attendance.util.Preconditions;

import java.time.LocalDateTime;
import java.util.Objects;

public final class Lecture {
    private final String id;
    private final Course course;
    private final LocalDateTime date;
    private final String room;

    private Lecture(String id, Course course, LocalDateTime date, String room) {
        this.id = id;
        this.course = course;
        this.date = date;
        this.room = room;
    }

    public static Builder builder() {return new Builder();}

    public String getId() {
        return id;
    }

    public Course getCourse() {
        return course;
    }

    public LocalDateTime getDate() {
        return date;
    }

    public String getRoom() {
        return room;
    }

    public static final class Builder {
        private String id;
        private Course course;
        private LocalDateTime date;
        private String room;

        public Builder id(String id) {this.id = id; return this;}
        public Builder course(Course course) {this.course = course; return this;}
        public Builder date(LocalDateTime date) {this.date = date; return this;}
        public Builder room(String room) {this.room = room; return this;}

        public Lecture build() {
            Preconditions.requireNonBlank(id, "id");
            Preconditions.requireNonBlank(room, "room");
            Objects.requireNonNull(course, "course cannot be null");
            Objects.requireNonNull(date, "date cannot be null");

            return new Lecture(id, course, date, room);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof Lecture lecture)) return false;
        return Objects.equals(id, lecture.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}
