package com.undefinedvars.attendance.repository;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.undefinedvars.attendance.model.AttendanceRecord;
import com.undefinedvars.attendance.model.Student;
import com.undefinedvars.attendance.persistence.AttendanceRecordDto;
import com.undefinedvars.attendance.persistence.AttendanceRecordMapper;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.*;

/**
 * JSON-backed implementation of {@link AttendanceRepository}.
 *
 * Persistence strategy: write-through, full-file rewrite — same pattern
 * as {@link JsonStudentRepository}.
 *   - On construction, reads the JSON file once and populates an in-memory
 *     LinkedHashMap (preserves insertion order across restarts).
 *   - On every save() or successful delete(), rewrites the entire file.
 *   - Read methods (findById, findAll, findByDate, findByStudentAndDate)
 *     hit only the in-memory map; they never touch the disk.
 *
 * On-disk shape: a top-level JSON array of {@link AttendanceRecordDto} objects.
 * AttendanceRecord references to Student are stored as studentId strings;
 * the mapper re-attaches the actual Student object on load by looking it up
 * via the injected {@link StudentRepository}.
 *
 * Load-order requirement: the injected StudentRepository MUST already be
 * fully populated before this constructor runs. Otherwise every attendance
 * record will fail to resolve its studentId and be skipped as orphaned.
 * The {@link com.undefinedvars.attendance.AppContext} bootstrap is responsible
 * for honoring this order.
 *
 * Robustness:
 *   - Missing file on startup is treated as "empty repository" (first run).
 *   - Corrupt/unparseable file is logged to stderr and treated as empty;
 *     the app starts rather than crashes.
 *   - Records whose studentId cannot be resolved (orphaned, e.g. the
 *     referenced student was deleted or removed from the file externally)
 *     are skipped and logged. The rest of the file still loads.
 *   - Records that fail domain validation are skipped and logged similarly.
 *   - Write failures are logged but not retried; in-memory state may
 *     diverge from disk in that rare case.
 *
 * Out of scope for midterm-2: atomic writes (temp-file-then-rename),
 * proper logging framework, file locking for concurrent access, and
 * cascading deletes (if a Student is deleted, its attendance records
 * remain in this repo until manually cleaned).
 */
public final class JsonAttendanceRepository implements AttendanceRepository {
    private final Path path;
    private final ObjectMapper objectMapper;
    private final StudentRepository studentRepository;
    private final Map<String, AttendanceRecord> inMemoryStorage = new LinkedHashMap<>();

    public JsonAttendanceRepository(Path path, ObjectMapper objectMapper, StudentRepository studentRepository) {
        this.path = Objects.requireNonNull(path, "path cannot be null");
        this.objectMapper = Objects.requireNonNull(objectMapper, "objectMapper cannot be null");
        this.studentRepository = Objects.requireNonNull(studentRepository, "studentRepository cannot be null");
        load();
    }

    /*
        Called once from the constructor. Reads the JSON file into DTOs, then
        converts each DTO through AttendanceRecordMapper.toDomain — which re-validates
        the data via AttendanceRecord.builder(). Bad records are skipped, not fatal.
     */
    private void load() {
        if (!Files.exists(path)) {
            return; // No file to load, start with an empty repository
        }

        try {
            List<AttendanceRecordDto> dtos = objectMapper.readValue(
                    path.toFile(),
                    new TypeReference<List<AttendanceRecordDto>>() {}
            );

            for (AttendanceRecordDto dto : dtos) {
                try {
                    // resolve studentId → Student
                    Optional<Student> studentOpt = studentRepository.findById(dto.getStudentId());
                    if (studentOpt.isEmpty()) {
                        System.err.println("Skipping attendance record [id=" + dto.getId()
                                + "]: referenced studentId=" + dto.getStudentId() + " not found");
                        continue;
                    }
                    Student student = studentOpt.get();

                    // Same pattern as before from here on
                    AttendanceRecord record = AttendanceRecordMapper.toDomain(dto, student);
                    inMemoryStorage.put(record.getId(), record);
                } catch (RuntimeException e) {
                    System.err.println("Skipping invalid attendance record [id=" + dto.getId()
                            + "]: " + e.getMessage());
                }
            }

        } catch (IOException e) {
            System.err.println("Failed to load records from " + path + ": " + e.getMessage()
                    + ". Starting with empty record list.");
        }
    }

    /*
        Rewrites the entire JSON file from current in-memory state. Called
        after every mutation (save/delete). Iteration order of the LinkedHashMap
        is preserved, so insertion order survives restarts.
    */
    private void persist() {
        try {
            Files.createDirectories(path.getParent());

            List<AttendanceRecordDto> dtos = new ArrayList<>();
            for (AttendanceRecord record : inMemoryStorage.values()) {
                dtos.add(AttendanceRecordMapper.toDto(record));
            }

            objectMapper.writerWithDefaultPrettyPrinter()
                    .writeValue(path.toFile(), dtos);
        } catch (IOException e) {
            System.err.println("Failed to persist records to " + path + ": " + e.getMessage());
        }
    }

    // TODO: read methods (findById/findAll/findByDate/findByStudentAndDate) are
    // duplicated verbatim from InMemoryAttendanceRepository. Refactor via composition
    // during final-project polish — the read implementation should be delegated,
    // not copied.
    @Override
    public Optional<AttendanceRecord> findById(String id) {
        return Optional.ofNullable(inMemoryStorage.get(id));
    }

    @Override
    public List<AttendanceRecord> findAll() {
        return new ArrayList<>(inMemoryStorage.values());
    }

    @Override
    public List<AttendanceRecord> findByDate(LocalDate date) {
        Objects.requireNonNull(date, "date cannot be null");
        List<AttendanceRecord> result = new ArrayList<>();
        for (AttendanceRecord record : inMemoryStorage.values()) {
            if (date.equals(record.getDate())) {
                result.add(record);
            }
        }
        return result;
    }

    @Override
    public Optional<AttendanceRecord> findByStudentAndDate(String studentId, LocalDate date) {
        Objects.requireNonNull(studentId, "studentId cannot be null");
        Objects.requireNonNull(date, "date cannot be null");
        for (AttendanceRecord record : inMemoryStorage.values()) {
            if (studentId.equals(record.getStudent().getId()) && date.equals(record.getDate())) {
                return Optional.of(record);
            }
        }
        return Optional.empty();
    }

    @Override
    public AttendanceRecord save(AttendanceRecord record) {
        inMemoryStorage.put(record.getId(), record);
        persist();
        return record;
    }

    @Override
    public boolean delete(String id) {
        boolean removed = inMemoryStorage.remove(id) != null;
        if (removed) {
            persist();
        }
        return removed;
    }
}
