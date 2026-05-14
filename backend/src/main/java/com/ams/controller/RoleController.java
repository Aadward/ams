package com.ams.controller;

import com.ams.enums.UserRole;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/roles")
public class RoleController {

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<Map<String, String>>> listRoles() {
        List<Map<String, String>> roles = Arrays.stream(UserRole.values())
                .map(role -> Map.of(
                        "name", role.name(),
                        "description", getDescription(role)
                ))
                .toList();
        return ResponseEntity.ok(roles);
    }

    @GetMapping("/{name}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> getRole(@PathVariable String name) {
        try {
            UserRole role = UserRole.valueOf(name.toUpperCase());
            return ResponseEntity.ok(Map.of(
                    "name", role.name(),
                    "description", getDescription(role)
            ));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    private String getDescription(UserRole role) {
        return switch (role) {
            case ADMIN -> "Full access to all resources";
            case MANAGER -> "Manage assets, employees, departments, view dashboard";
            case USER -> "View assets, request assignment, view own profile";
        };
    }
}
