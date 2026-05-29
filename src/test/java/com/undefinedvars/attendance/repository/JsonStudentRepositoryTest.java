package com.undefinedvars.attendance.repository;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.undefinedvars.attendance.model.Group;
import com.undefinedvars.attendance.model.Student;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/*
    Round-trip tests for JsonStudentRepository.
    Each test simulates an app restart by constructing a SECOND repository
    instance pointed at the same file — proving that saved data survives
    the loss of in-memory state.

    @TempDir provides a fresh directory per test method, so tests are
    isolated from each other and from the dev machine's real ./data/.
 */
class JsonStudentRepositoryTest {

    @TempDir
    Path tempDir;

    private ObjectMapper objectMapper;
    private Group group;
    private Path studentsFile;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        group = Group.builder().id("test-group").name("Test Group").build();
        studentsFile = tempDir.resolve("students.json");
    }

    /*
        Helper: build a Student with sensible defaults. Avoids repeating
        the builder chain in every test (DRY for test code).
     */
    private Student newStudent(String id, String fullName, String email) {
        return Student.builder()
                .id(id)
                .fullName(fullName)
                .email(email)
                .group(group)
                .enrolledOn(LocalDate.of(2026, 1, 1))
                .build();
    }

    // -------------------------------------------------------------------------
    // Construction / robustness
    // -------------------------------------------------------------------------

    @Test
    void nonExistentFile_constructsEmpty() {
        // First-ever run: no file on disk yet.
        JsonStudentRepository repo = new JsonStudentRepository(studentsFile, objectMapper, group);

        assertTrue(repo.findAll().isEmpty());
        // The file is NOT created merely by constructing the repo;
        // persist() only runs on a mutation.
        assertFalse(Files.exists(studentsFile));
    }

    @Test
    void corruptFile_startsEmpty() throws Exception {
        // Simulate a hand-edited or partially-written file.
        Files.writeString(studentsFile, "this is not valid json");

        JsonStudentRepository repo = new JsonStudentRepository(studentsFile, objectMapper, group);

        // Should not throw — the app must survive a corrupt save file.
        assertTrue(repo.findAll().isEmpty());
    }

    // -------------------------------------------------------------------------
    // Write path
    // -------------------------------------------------------------------------

    @Test
    void save_writesFileToDisk() {
        JsonStudentRepository repo = new JsonStudentRepository(studentsFile, objectMapper, group);
        repo.save(newStudent("id-1", "Ana Beridze", "ana@example.com"));

        assertTrue(Files.exists(studentsFile));
    }

    // -------------------------------------------------------------------------
    // Round-trip: the central tests
    // -------------------------------------------------------------------------

    @Test
    void roundTrip_singleStudent() {
        // Write through repo A...
        JsonStudentRepository repoA = new JsonStudentRepository(studentsFile, objectMapper, group);
        Student ana = newStudent("id-1", "Ana Beridze", "ana@example.com");
        repoA.save(ana);

        // ...and read back through a FRESH repo B (simulates restart).
        JsonStudentRepository repoB = new JsonStudentRepository(studentsFile, objectMapper, group);

        List<Student> loaded = repoB.findAll();
        assertEquals(1, loaded.size());
        Student reloaded = loaded.get(0);
        assertEquals("id-1", reloaded.getId());
        assertEquals("Ana Beridze", reloaded.getFullName());
        assertEquals("ana@example.com", reloaded.getEmail());
        assertEquals(LocalDate.of(2026, 1, 1), reloaded.getEnrolledOn());
        // Group is resolved via the injected group, so identity is reattached.
        assertEquals(group, reloaded.getGroup());
    }

    @Test
    void roundTrip_preservesInsertionOrder() {
        JsonStudentRepository repoA = new JsonStudentRepository(studentsFile, objectMapper, group);
        repoA.save(newStudent("id-1", "Ana Beridze", "ana@example.com"));
        repoA.save(newStudent("id-2", "Beka Kopadze", "beka@example.com"));
        repoA.save(newStudent("id-3", "Cako Smith", "cako@example.com"));

        JsonStudentRepository repoB = new JsonStudentRepository(studentsFile, objectMapper, group);
        List<Student> loaded = repoB.findAll();

        assertEquals(3, loaded.size());
        assertEquals("Ana Beridze", loaded.get(0).getFullName());
        assertEquals("Beka Kopadze", loaded.get(1).getFullName());
        assertEquals("Cako Smith", loaded.get(2).getFullName());
    }

    @Test
    void roundTrip_findByIdLocatesAllStudents() {
        JsonStudentRepository repoA = new JsonStudentRepository(studentsFile, objectMapper, group);
        repoA.save(newStudent("id-1", "Ana Beridze", "ana@example.com"));
        repoA.save(newStudent("id-2", "Beka Kopadze", "beka@example.com"));

        JsonStudentRepository repoB = new JsonStudentRepository(studentsFile, objectMapper, group);

        Optional<Student> ana = repoB.findById("id-1");
        Optional<Student> beka = repoB.findById("id-2");
        Optional<Student> nobody = repoB.findById("id-does-not-exist");

        assertTrue(ana.isPresent());
        assertTrue(beka.isPresent());
        assertTrue(nobody.isEmpty());
        assertEquals("Ana Beridze", ana.get().getFullName());
    }

    // -------------------------------------------------------------------------
    // Delete is persistent
    // -------------------------------------------------------------------------

    @Test
    void delete_removesStudentFromDisk() {
        JsonStudentRepository repoA = new JsonStudentRepository(studentsFile, objectMapper, group);
        repoA.save(newStudent("id-1", "Ana Beridze", "ana@example.com"));
        repoA.save(newStudent("id-2", "Beka Kopadze", "beka@example.com"));

        boolean removed = repoA.delete("id-1");
        assertTrue(removed);

        JsonStudentRepository repoB = new JsonStudentRepository(studentsFile, objectMapper, group);
        List<Student> loaded = repoB.findAll();

        assertEquals(1, loaded.size());
        assertEquals("Beka Kopadze", loaded.get(0).getFullName());
    }
}