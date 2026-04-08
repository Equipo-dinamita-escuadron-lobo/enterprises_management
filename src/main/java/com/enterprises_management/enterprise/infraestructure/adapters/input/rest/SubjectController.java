package com.enterprises_management.enterprise.infraestructure.adapters.input.rest;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.enterprises_management.enterprise.application.ports.input.ISubjectCreateManagerPort;
import com.enterprises_management.enterprise.application.ports.input.ISubjectDeleteManagerPort;
import com.enterprises_management.enterprise.application.ports.input.ISubjectSearchManagerPort;
import com.enterprises_management.enterprise.application.ports.input.ISubjectUpdateManagerPort;
import com.enterprises_management.enterprise.domain.models.Subject;
import com.enterprises_management.enterprise.infraestructure.adapters.input.rest.data.request.SubjectCreateRequest;
import com.enterprises_management.enterprise.infraestructure.adapters.input.rest.data.response.SubjectCreateResponse;
import com.enterprises_management.enterprise.infraestructure.adapters.input.rest.data.response.SubjectResponse;
import com.enterprises_management.enterprise.infraestructure.adapters.input.rest.mapper.interfaces.ISubjectRestMapper;

import jakarta.validation.Valid;
import lombok.AllArgsConstructor;

/**
 * Controlador REST para la gestión de materias.
 * Proporciona endpoints para operaciones CRUD sobre materias.
 *
 * @author CONTAPP
 * @version 1.0
 * @since 1.0.0
 */
@RestController
@RequestMapping("/api/enterprises/subjects")
@Validated
@AllArgsConstructor
public class SubjectController {

    private final ISubjectCreateManagerPort subjectCreateManagerPort;
    private final ISubjectSearchManagerPort subjectSearchManagerPort;
    private final ISubjectUpdateManagerPort subjectUpdateManagerPort;
    private final ISubjectDeleteManagerPort subjectDeleteManagerPort;
    private final ISubjectRestMapper subjectRestMapper;

    /**
     * Obtiene todas las materias.
     *
     * @return Lista de respuestas de materias
     */
    @GetMapping("/")
    public ResponseEntity<List<SubjectResponse>> getAllSubjects() {
        List<Subject> subjects = subjectSearchManagerPort.getAllSubjects();
        List<SubjectResponse> responses = subjects.stream()
                .map(subjectRestMapper::toResponse)
                .collect(Collectors.toList());
        return ResponseEntity.ok(responses);
    }

    /**
     * Obtiene una materia por su código.
     *
     * @param code el código de la materia
     * @return Respuesta de la materia
     */
    @GetMapping("/{code}")
    public ResponseEntity<SubjectResponse> getSubjectByCode(@PathVariable String code) {
        Subject subject = subjectSearchManagerPort.getSubjectByCode(code);
        if (subject == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(subjectRestMapper.toResponse(subject));
    }

    /**
     * Crea una nueva materia.
     *
     * @param request la solicitud de creación
     * @return Respuesta de la materia creada
     */
    @PostMapping("/")
    public ResponseEntity<SubjectCreateResponse> createSubject(@Valid @RequestBody SubjectCreateRequest request) {
        Subject subject = subjectRestMapper.toDomain(request);
        subject = subjectCreateManagerPort.createSubject(subject);
        return ResponseEntity.ok(subjectRestMapper.toCreateResponse(subject));
    }

    /**
     * Actualiza una materia existente.
     *
     * @param code el código de la materia a actualizar
     * @param request la solicitud de actualización
     * @return Respuesta de la materia actualizada
     */
    @PutMapping("/{code}")
    public ResponseEntity<SubjectCreateResponse> updateSubject(@PathVariable String code, @Valid @RequestBody SubjectCreateRequest request) {
        Subject subject = subjectRestMapper.toDomain(request);
        subject.setCode(code); 
        subject = subjectUpdateManagerPort.updateSubject(code, subject);
        if (subject == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(subjectRestMapper.toCreateResponse(subject));
    }

    /**
     * Elimina una materia por su código.
     *
     * @param code el código de la materia a eliminar
     * @return Respuesta vacía
     */
    @DeleteMapping("/{code}")
    public ResponseEntity<Void> deleteSubject(@PathVariable String code) {
        subjectDeleteManagerPort.deleteSubject(code);
        return ResponseEntity.noContent().build();
    }
}