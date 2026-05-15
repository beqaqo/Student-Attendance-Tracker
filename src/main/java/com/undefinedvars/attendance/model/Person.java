package com.undefinedvars.attendance.model;

import com.undefinedvars.attendance.util.Preconditions;

import java.util.Objects;

/*
    Abstract class representing a person in the attendance system.
    This class serves as a base for specific types of people, such as students and lecturers
    Meant to be inherited
 */
public abstract class Person {
    private final String id;
    private final String fullName;
    private final String email;

    /*
        We validate in the constructor, which is package-private,
        since we can't have Builder in the abstract class (we can't instantiate it)
     */
    Person(String id, String fullName, String email) {
        this.id = Preconditions.requireNonBlank(id, "id");
        this.fullName = Preconditions.requireNonBlank(fullName, "fullName");
        this.email = Preconditions.requireValidEmail(email, "email");
    }

    public String getId() {
        return id;
    }

    public String getFullName() {
        return fullName;
    }

    public String getEmail() {
        return email;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof Person person)) return false;
        return Objects.equals(id, person.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}
