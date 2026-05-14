package com.ams.controller;

import com.ams.dto.AssetAssignRequest;
import com.ams.dto.AssetCreateRequest;
import com.ams.dto.AssetResponse;
import com.ams.dto.AssetUpdateRequest;
import com.ams.dto.BatchAssignRequest;
import com.ams.dto.BatchLocationRequest;
import com.ams.dto.BatchRequest;
import com.ams.service.AssetService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/assets")
@RequiredArgsConstructor
public class AssetController {

    private final AssetService assetService;

    @GetMapping
    public ResponseEntity<?> listAssets(
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        try {
            Pageable pageable = PageRequest.of(page, size);
            Page<AssetResponse> result = assetService.searchAssets(category, status, keyword, pageable);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getAsset(@PathVariable Long id) {
        try {
            return ResponseEntity.ok(assetService.getAsset(id));
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping
    public ResponseEntity<?> createAsset(@RequestBody AssetCreateRequest request) {
        try {
            return ResponseEntity.ok(assetService.createAsset(request));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateAsset(@PathVariable Long id, @RequestBody AssetUpdateRequest request) {
        try {
            return ResponseEntity.ok(assetService.updateAsset(id, request));
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteAsset(@PathVariable Long id) {
        try {
            assetService.deleteAsset(id);
            return ResponseEntity.ok().build();
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/{id}/assign")
    public ResponseEntity<?> assignAsset(@PathVariable Long id, @RequestBody AssetAssignRequest request) {
        try {
            return ResponseEntity.ok(assetService.assignAsset(id, request.getAssigneeId()));
        } catch (RuntimeException e) {
            if (e.getMessage().contains("不存在")) {
                return ResponseEntity.notFound().build();
            }
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/{id}/unassign")
    public ResponseEntity<?> unassignAsset(@PathVariable Long id) {
        try {
            return ResponseEntity.ok(assetService.unassignAsset(id));
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/{id}/retire")
    public ResponseEntity<?> retireAsset(@PathVariable Long id) {
        try {
            return ResponseEntity.ok(assetService.retireAsset(id));
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // Batch endpoints
    @PostMapping("/batch/assign")
    public ResponseEntity<?> batchAssign(@RequestBody BatchAssignRequest request) {
        try {
            int count = assetService.batchAssign(request.getAssetIds(), request.getAssigneeId());
            return ResponseEntity.ok(Map.of("success", true, "count", count));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/batch/unassign")
    public ResponseEntity<?> batchUnassign(@RequestBody BatchRequest request) {
        try {
            int count = assetService.batchUnassign(request.getAssetIds());
            return ResponseEntity.ok(Map.of("success", true, "count", count));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/batch/retire")
    public ResponseEntity<?> batchRetire(@RequestBody BatchRequest request) {
        try {
            int count = assetService.batchRetire(request.getAssetIds());
            return ResponseEntity.ok(Map.of("success", true, "count", count));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/batch/location")
    public ResponseEntity<?> batchUpdateLocation(@RequestBody BatchLocationRequest request) {
        try {
            int count = assetService.batchUpdateLocation(request.getAssetIds(), request.getLocation());
            return ResponseEntity.ok(Map.of("success", true, "count", count));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}