package com.enterprises_management.enterprise.infraestructure.adapters.input.rest;

import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.enterprises_management.enterprise.infraestructure.adapters.output.jpaAdapter.entity.SubjectEntity;
import com.enterprises_management.enterprise.infraestructure.adapters.output.jpaAdapter.repository.ISubjectRepository;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/subjects")
@RequiredArgsConstructor
@PreAuthorize("hasRole('admin_client') or hasRole('user_client') or hasRole('super_client') or hasRole('Administrador') or hasRole('Estudiante') or hasRole('Profesor')")
public class SubjectController {

    private final ISubjectRepository subjectRepository;

    @GetMapping("/")
    public ResponseEntity<List<Map<String, String>>> findAll() {
        List<Map<String, String>> subjects = subjectRepository.findAll().stream()
                .map(this::toDto)
                .toList();
        return ResponseEntity.ok(subjects);
    }

    @GetMapping("/{code}")
    public ResponseEntity<Map<String, String>> findByCode(@PathVariable String code) {
        return subjectRepository.findByCode(code)
                .map(entity -> ResponseEntity.ok(toDto(entity)))
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping("/")
    public ResponseEntity<?> create(@RequestBody Map<String, String> body) {
        String code = body.get("code");
        String name = body.get("name");
        if (code == null || code.isBlank() || name == null || name.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("message", "code and name are required"));
        }
        if (subjectRepository.existsByCode(code)) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(Map.of("message", "Subject with code already exists"));
        }
        SubjectEntity entity = new SubjectEntity();
        entity.setCode(code);
        entity.setName(name);
        SubjectEntity saved = subjectRepository.save(entity);
        return ResponseEntity.status(HttpStatus.CREATED).body(toDto(saved));
    }

    @PutMapping("/{code}")
    public ResponseEntity<?> update(@PathVariable String code, @RequestBody Map<String, String> body) {
        return subjectRepository.findByCode(code)
                .map(entity -> {
                    String name = body.get("name");
                    if (name == null || name.isBlank()) {
                        return ResponseEntity.badRequest().body(Map.of("message", "name is required"));
                    }
                    entity.setName(name);
                    return ResponseEntity.ok(toDto(subjectRepository.save(entity)));
                })
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{code}")
    public ResponseEntity<Void> delete(@PathVariable String code) {
        return subjectRepository.findByCode(code)
                .map(entity -> {
                    subjectRepository.delete(entity);
                    return ResponseEntity.noContent().<Void>build();
                })
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    private Map<String, String> toDto(SubjectEntity entity) {
        return Map.of("code", entity.getCode(), "name", entity.getName());
    }
}
