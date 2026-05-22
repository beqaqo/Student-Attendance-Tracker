package com.undefinedvars.attendance.util;

public interface IdGenerator {
    /*
        IdGenerator is an interface that defines a method for generating unique identifiers.
        This can be implemented in various ways, such as using UUIDs, database sequences, or custom logic.
     */
    String newId();
}
