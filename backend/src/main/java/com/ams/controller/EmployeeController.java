package com.ams.controller;

import com.ams.dto.EmployeeRequest;
import com.ams.dto.EmployeeResponse;
import com.ams.dto.EmployeeRoleUpdateRequest;
import com.ams.enums.UserRole;
import com.ams.service.EmployeeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/employees")
@RequiredArgsConstructor
@Slf4j
public class EmployeeController {

    private final EmployeeService employeeService;

    @GetMapping
    public ResponseEntity<?> listEmployees(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        try {
            Pageable pageable = PageRequest.of(page, size);
            Page<EmployeeResponse> result = employeeService.listEmployees(pageable);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getEmployee(@PathVariable Long id) {
        try {
            return ResponseEntity.ok(employeeService.getEmployee(id));
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping
    public ResponseEntity<?> createEmployee(@RequestBody EmployeeRequest request) {
        try {
            return ResponseEntity.ok(employeeService.createEmployee(request));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateEmployee(@PathVariable Long id, @RequestBody EmployeeRequest request) {
        try {
            return ResponseEntity.ok(employeeService.updateEmployee(id, request));
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteEmployee(@PathVariable Long id) {
        try {
            employeeService.deleteEmployee(id);
            return ResponseEntity.ok().build();
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PatchMapping("/{id}/role")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> updateEmployeeRole(@PathVariable Long id, @RequestBody EmployeeRoleUpdateRequest request) {
        try {
            UserRole newRole = UserRole.valueOf(request.getNewRole().toUpperCase());
            return ResponseEntity.ok(employeeService.updateRole(id, newRole));
        } catch (IllegalArgumentException e) {
            log.warn("Invalid role value: {}", request.getNewRole());
            return ResponseEntity.badRequest().body("无效的角色: " + request.getNewRole());
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            log.error("Error updating employee role", e);
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}