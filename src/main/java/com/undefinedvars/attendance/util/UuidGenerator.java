package com.undefinedvars.attendance.util;

/*
    Implementation of IdGenerator that generates unique identifiers using UUIDs.
 */
public final class UuidGenerator implements IdGenerator {
    @Override
    public String newId() {
        return java.util.UUID.randomUUID().toString();
    }
}
