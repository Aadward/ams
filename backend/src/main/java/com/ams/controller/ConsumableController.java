package com.ams.controller;

import com.ams.dto.*;
import com.ams.enums.ConsumableCategory;
import com.ams.service.ConsumableService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/consumables")
@RequiredArgsConstructor
@CrossOrigin
public class ConsumableController {

    private final ConsumableService consumableService;

    @GetMapping
    public ResponseEntity<List<ConsumableResponse>> list(
            @RequestParam(required = false) ConsumableCategory category) {
        return ResponseEntity.ok(consumableService.list(category));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ConsumableResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(consumableService.getById(id));
    }

    @PostMapping
    public ResponseEntity<ConsumableResponse> create(@Valid @RequestBody ConsumableRequest req) {
        return ResponseEntity.ok(consumableService.create(req));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ConsumableResponse> update(@PathVariable Long id,
            @Valid @RequestBody ConsumableRequest req) {
        return ResponseEntity.ok(consumableService.update(id, req));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        consumableService.delete(id);
        return ResponseEntity.ok().build();
    }
}
