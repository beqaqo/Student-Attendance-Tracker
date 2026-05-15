package com.undefinedvars.attendance.model;

import com.undefinedvars.attendance.util.Preconditions;

import java.util.Objects;

public final class Group {
    private final String id;
    private final String name;

    /*
        We keep the group constructor private to enforce the use of the builder pattern,
        which allows for better validation and immutability.
    */
    private Group(String id, String name) {
        this.id = id;
        this.name = name;
    }

    /*
        Call to the static object
    */
    public static Builder builder() {return new Builder();}

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    /*
        The Builder class provides a fluent API for constructing Group instances.
        It includes validation to ensure that the id and name are not null or empty,
        which helps maintain the integrity of the Group objects created.
    */
    public static final class Builder {
        private String id;
        private String name;

        public Builder id(String id) { this.id = id; return this; }
        public Builder name(String name) { this.name = name; return this; }

        public Group build() {
            Preconditions.requireNonBlank(id, "id");
            Preconditions.requireNonBlank(name, "name");

            return new Group(id, name);
        }
    }

    /*
        The equals and hashCode methods are overridden to ensure that Group instances are compared based on their unique id.
        This allows for proper functioning in collections and when checking for equality between Group objects.
     */
    @Override
    public boolean equals(Object o) {
        if (!(o instanceof Group group)) return false;
        return Objects.equals(id, group.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}
