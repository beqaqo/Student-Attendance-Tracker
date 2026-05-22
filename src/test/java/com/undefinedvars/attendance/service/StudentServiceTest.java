package com.undefinedvars.attendance.service;

import com.undefinedvars.attendance.model.Group;
import com.undefinedvars.attendance.model.Student;
import com.undefinedvars.attendance.repository.InMemoryStudentRepository;
import com.undefinedvars.attendance.repository.Repository;
import com.undefinedvars.attendance.util.IdGenerator;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class StudentServiceTest {

    /*
        A test double: a predictable IdGenerator. Instead of random UUIDs
        it returns "id-1", "id-2", ... so tests can assert exact ids.
        This is only possible because StudentService depends on the
        IdGenerator interface, not on UuidGenerator directly.
    */
    private static final class SequentialIdGenerator implements IdGenerator {
        private int counter = 0;
        @Override
        public String newId() {
            counter++;
            return "id-" + counter;
        }
    }

    private StudentService service;

    /*
        @BeforeEach runs before every test method, giving each test a
        fresh service with empty storage - so tests never affect each other.
    */
    @BeforeEach
    void setUp() {
        Repository<Student, String> repository = new InMemoryStudentRepository();
        IdGenerator idGenerator = new SequentialIdGenerator();
        Group group = Group.builder().id("g1").name("Test Group").build();
        service = new StudentService(repository, idGenerator, group);
    }

    @Test
    void register_validInput_createsStudent() {
        Student student = service.register("Ana Beridze", "ana@example.com");

        assertEquals("Ana Beridze", student.getFullName());
        assertEquals("ana@example.com", student.getEmail());
        assertEquals("id-1", student.getId());
    }

    @Test
    void register_addsStudentToList() {
        service.register("Ana Beridze", "ana@example.com");
        service.register("Beka Kopadze", "beka@example.com");

        assertEquals(2, service.findAll().size());
    }

    @Test
    void register_blankName_throws() {
        assertThrows(IllegalArgumentException.class,
                () -> service.register("   ", "ana@example.com"));
    }

    @Test
    void register_invalidEmail_throws() {
        assertThrows(IllegalArgumentException.class,
                () -> service.register("Ana Beridze", "not-an-email"));
    }

    @Test
    void register_preservesInsertionOrder() {
        service.register("First Student", "first@example.com");
        service.register("Second Student", "second@example.com");

        assertEquals("First Student", service.findAll().get(0).getFullName());
        assertEquals("Second Student", service.findAll().get(1).getFullName());
    }
}