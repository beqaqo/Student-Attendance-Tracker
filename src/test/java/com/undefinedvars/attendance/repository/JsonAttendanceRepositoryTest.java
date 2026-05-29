package com.undefinedvars.attendance.repository;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.undefinedvars.attendance.model.AttendanceRecord;
import com.undefinedvars.attendance.model.AttendanceStatus;
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
    Round-trip tests for JsonAttendanceRepository.

    These tests are subtler than the student tests because AttendanceRecord
    holds a Student reference, which is stored on disk as a studentId string
    and resolved at load time against the student repository. So every test
    builds BOTH repos in setUp(), and the order matters: students first,
    then attendance (mirroring AppContext's load order).
 */
class JsonAttendanceRepositoryTest {

    @TempDir
    Path tempDir;

    private ObjectMapper objectMapper;
    private Group group;
    private Path studentsFile;
    private Path attendanceFile;
    private JsonStudentRepository studentRepo;
    private Student ana;
    private Student beka;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        group = Group.builder().id("test-group").name("Test Group").build();
        studentsFile = tempDir.resolve("students.json");
        attendanceFile = tempDir.resolve("attendance.json");

        // Build the student repo first and pre-populate it with two students.
        // The attendance repo needs these in place to resolve studentId references.
        studentRepo = new JsonStudentRepository(studentsFile, objectMapper, group);
        ana = newStudent("student-1", "Ana Beridze", "ana@example.com");
        beka = newStudent("student-2", "Beka Kopadze", "beka@example.com");
        studentRepo.save(ana);
        studentRepo.save(beka);
    }

    private Student newStudent(String id, String fullName, String email) {
        return Student.builder()
                .id(id)
                .fullName(fullName)
                .email(email)
                .group(group)
                .enrolledOn(LocalDate.of(2026, 1, 1))
                .build();
    }

    private AttendanceRecord newRecord(String id, Student student, LocalDate date, AttendanceStatus status) {
        return AttendanceRecord.builder()
                .id(id)
                .student(student)
                .date(date)
                .status(status)
                .build();
    }

    // -------------------------------------------------------------------------
    // Construction / robustness
    // -------------------------------------------------------------------------

    @Test
    void nonExistentFile_constructsEmpty() {
        JsonAttendanceRepository repo = new JsonAttendanceRepository(attendanceFile, objectMapper, studentRepo);

        assertTrue(repo.findAll().isEmpty());
        assertFalse(Files.exists(attendanceFile));
    }

    @Test
    void corruptFile_startsEmpty() throws Exception {
        Files.writeString(attendanceFile, "not valid json");

        JsonAttendanceRepository repo = new JsonAttendanceRepository(attendanceFile, objectMapper, studentRepo);

        assertTrue(repo.findAll().isEmpty());
    }

    // -------------------------------------------------------------------------
    // Round-trip
    // -------------------------------------------------------------------------

    @Test
    void roundTrip_singleRecord() {
        JsonAttendanceRepository repoA = new JsonAttendanceRepository(attendanceFile, objectMapper, studentRepo);
        LocalDate today = LocalDate.now();
        repoA.save(newRecord("rec-1", ana, today, AttendanceStatus.LATE));

        // Fresh instance, same file.
        JsonAttendanceRepository repoB = new JsonAttendanceRepository(attendanceFile, objectMapper, studentRepo);

        List<AttendanceRecord> loaded = repoB.findAll();
        assertEquals(1, loaded.size());
        AttendanceRecord reloaded = loaded.get(0);
        assertEquals("rec-1", reloaded.getId());
        assertEquals(today, reloaded.getDate());
        assertEquals(AttendanceStatus.LATE, reloaded.getStatus());
        // The Student reference was re-resolved from its id — should match
        // the same Ana instance the student repo returns.
        assertEquals(ana, reloaded.getStudent());
    }

    @Test
    void roundTrip_preservesInsertionOrder() {
        JsonAttendanceRepository repoA = new JsonAttendanceRepository(attendanceFile, objectMapper, studentRepo);
        LocalDate today = LocalDate.now();
        repoA.save(newRecord("rec-1", ana, today, AttendanceStatus.PRESENT));
        repoA.save(newRecord("rec-2", beka, today, AttendanceStatus.ABSENT));

        JsonAttendanceRepository repoB = new JsonAttendanceRepository(attendanceFile, objectMapper, studentRepo);
        List<AttendanceRecord> loaded = repoB.findAll();

        assertEquals(2, loaded.size());
        assertEquals("rec-1", loaded.get(0).getId());
        assertEquals("rec-2", loaded.get(1).getId());
    }

    @Test
    void roundTrip_findByStudentAndDate() {
        JsonAttendanceRepository repoA = new JsonAttendanceRepository(attendanceFile, objectMapper, studentRepo);
        LocalDate today = LocalDate.now();
        repoA.save(newRecord("rec-1", ana, today, AttendanceStatus.LATE));

        JsonAttendanceRepository repoB = new JsonAttendanceRepository(attendanceFile, objectMapper, studentRepo);

        Optional<AttendanceRecord> found = repoB.findByStudentAndDate("student-1", today);
        assertTrue(found.isPresent());
        assertEquals(AttendanceStatus.LATE, found.get().getStatus());

        Optional<AttendanceRecord> missing = repoB.findByStudentAndDate("student-1", today.minusDays(1));
        assertTrue(missing.isEmpty());
    }

    @Test
    void delete_removesRecordFromDisk() {
        JsonAttendanceRepository repoA = new JsonAttendanceRepository(attendanceFile, objectMapper, studentRepo);
        LocalDate today = LocalDate.now();
        repoA.save(newRecord("rec-1", ana, today, AttendanceStatus.LATE));
        repoA.save(newRecord("rec-2", beka, today, AttendanceStatus.ABSENT));

        assertTrue(repoA.delete("rec-1"));

        JsonAttendanceRepository repoB = new JsonAttendanceRepository(attendanceFile, objectMapper, studentRepo);
        List<AttendanceRecord> loaded = repoB.findAll();

        assertEquals(1, loaded.size());
        assertEquals("rec-2", loaded.get(0).getId());
    }

    // -------------------------------------------------------------------------
    // Orphan handling — the AttendanceRepository-specific case
    // -------------------------------------------------------------------------

    @Test
    void orphanRecord_isSkippedOnLoad() {
        // Write a record referencing a student that DOES exist in studentRepo,
        // plus one referencing a student that doesn't.
        // Easiest way to create the orphan: write both, then delete the student
        // from studentRepo and construct a fresh attendance repo.
        JsonAttendanceRepository repoA = new JsonAttendanceRepository(attendanceFile, objectMapper, studentRepo);
        LocalDate today = LocalDate.now();
        repoA.save(newRecord("rec-good", ana, today, AttendanceStatus.PRESENT));
        repoA.save(newRecord("rec-orphan", beka, today, AttendanceStatus.ABSENT));

        // Remove beka from the student repo — rec-orphan now references a
        // student that no longer exists, simulating an externally-edited file
        // or a future delete-student-but-not-records scenario.
        studentRepo.delete("student-2");

        // Fresh attendance repo — should skip the orphan and load only rec-good.
        JsonAttendanceRepository repoB = new JsonAttendanceRepository(attendanceFile, objectMapper, studentRepo);
        List<AttendanceRecord> loaded = repoB.findAll();

        assertEquals(1, loaded.size());
        assertEquals("rec-good", loaded.get(0).getId());
    }
}