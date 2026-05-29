package com.undefinedvars.attendance.repository;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.undefinedvars.attendance.model.Group;
import com.undefinedvars.attendance.model.Student;
import com.undefinedvars.attendance.persistence.StudentDto;
import com.undefinedvars.attendance.persistence.StudentMapper;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

/**
 * JSON-backed implementation of {@link StudentRepository}.
 *
 * Persistence strategy: write-through, full-file rewrite.
 *   - On construction, reads the JSON file once and populates an in-memory
 *     LinkedHashMap (which preserves insertion order across restarts).
 *   - On every save() or successful delete(), rewrites the entire file.
 *   - All read methods (findById, findAll, findByGroup, search) hit only
 *     the in-memory map; they never touch the disk. This keeps object
 *     identity stable across reads — two calls to findById return the
 *     same Student instance.
 *
 * On-disk shape: a top-level JSON array of {@link StudentDto} objects.
 * Student references to Group are stored as groupId strings; the mapper
 * re-attaches the actual Group object on load.
 *
 * Robustness:
 *   - Missing file on startup is treated as "empty repository" (first run).
 *   - Corrupt/unparseable file is logged to stderr and treated as empty;
 *     the app starts rather than crashes.
 *   - Individual records that fail domain validation (e.g. hand-edited
 *     bad email) are skipped and logged; the rest of the file still loads.
 *   - Write failures are logged but not retried; in-memory state may
 *     diverge from disk in that rare case.
 *
 * Out of scope for midterm-2: atomic writes (temp-file-then-rename),
 * proper logging framework, file locking for concurrent access.
 */
public final class JsonStudentRepository implements StudentRepository {
    private final Path path;
    private final ObjectMapper objectMapper;
    private final Group group;
    private final Map<String, Student> inMemoryStorage = new LinkedHashMap<>();

    public JsonStudentRepository(Path path, ObjectMapper objectMapper, Group group) {
        this.path = Objects.requireNonNull(path, "path cannot be null");
        this.objectMapper = Objects.requireNonNull(objectMapper, "objectMapper cannot be null");
        this.group = Objects.requireNonNull(group, "group cannot be null");
        load();
    }

    /*
        Called once from the constructor. Reads the JSON file into DTOs, then
        converts each DTO through StudentMapper.toDomain — which re-validates
        the data via Student.builder(). Bad records are skipped, not fatal.
     */
    private void load() {
        if (!Files.exists(path)) {
            return; // No file to load, start with an empty repository
        }

        try {
            List<StudentDto> dtos = objectMapper.readValue(
                    path.toFile(),
                    new TypeReference<List<StudentDto>>() {} // new TypeReference<List<StudentDto>>() {} // anonymous subclass preserves the generic type at runtime (Java erasure workaround)
            );

            for (StudentDto dto : dtos) {
                try {
                    Student student = StudentMapper.toDomain(dto, group);
                    inMemoryStorage.put(student.getId(), student);
                } catch (RuntimeException e) {
                    System.err.println("Skipping invalid student record [id=" + dto.getId()
                            + "]: " + e.getMessage());
                }
            }
        } catch (IOException e) {
            System.err.println("Failed to load students from " + path + ": " + e.getMessage()
                    + ". Starting with empty student list.");
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

            List<StudentDto> dtos = new ArrayList<>();
            for (Student student : inMemoryStorage.values()) {
                dtos.add(StudentMapper.toDto(student));
            }

            objectMapper.writerWithDefaultPrettyPrinter()
                    .writeValue(path.toFile(), dtos);
        } catch (IOException e) {
            System.err.println("Failed to persist students to " + path + ": " + e.getMessage());
        }
    }

    // TODO: read methods (findById/findAll/findByGroup/search) are duplicated from
    // InMemoryStudentRepository. Refactor via composition during final-project polish.
    @Override
    public Optional<Student> findById(String id) {
        return Optional.ofNullable(inMemoryStorage.get(id));
    }

    @Override
    public List<Student> findAll() {
        return new ArrayList<>(inMemoryStorage.values());
    }

    @Override
    public List<Student> findByGroup(Group group) {
        Objects.requireNonNull(group, "group cannot be null");
        List<Student> result = new ArrayList<>();
        for (Student student : inMemoryStorage.values()) {
            if (group.equals(student.getGroup())) {
                result.add(student);
            }
        }
        return result;
    }

    @Override
    public List<Student> search(String query) {
        Objects.requireNonNull(query, "query cannot be null");
        String needle = query.trim().toLowerCase(Locale.ROOT);
        if (needle.isEmpty()) {
            return new ArrayList<>();
        }
        List<Student> result = new ArrayList<>();
        for (Student student : inMemoryStorage.values()) {
            String name = student.getFullName().toLowerCase(Locale.ROOT);
            String email = student.getEmail().toLowerCase(Locale.ROOT);
            if (name.contains(needle) || email.contains(needle)) {
                result.add(student);
            }
        }
        return result;
    }

    @Override
    public Student save(Student student) {
        inMemoryStorage.put(student.getId(), student);
        persist();
        return student;
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
