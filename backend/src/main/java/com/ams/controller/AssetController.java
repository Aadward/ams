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
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/assets")
@RequiredArgsConstructor
public class AssetController {

    private final AssetService assetService;

    private Long getCurrentEmployeeId() {
        var authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new RuntimeException("用户未登录");
        }
        String username = authentication.getName();
        // In our system, username is actually the userId for the JWT token
        // The JwtAuthenticationFilter stores userId as the "username" claim
        try {
            return Long.valueOf(username);
        } catch (NumberFormatException e) {
            throw new RuntimeException("无效的用户信息");
        }
    }

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

    // Photo endpoints
    @PostMapping("/{id}/photo")
    public ResponseEntity<?> updateAssetPhoto(@PathVariable Long id, @RequestBody Map<String, String> body) {
        try {
            String photoUrl = body.get("photoUrl");
            return ResponseEntity.ok(assetService.updateAssetPhoto(id, photoUrl));
        } catch (RuntimeException e) {
            if (e.getMessage().contains("不存在")) {
                return ResponseEntity.notFound().build();
            }
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/my")
    public ResponseEntity<?> getMyAssets() {
        try {
            Long employeeId = getCurrentEmployeeId();
            List<AssetResponse> result = assetService.getMyAssets(employeeId);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/{id}/photo")
    public ResponseEntity<?> getAssetPhoto(@PathVariable Long id) {
        try {
            AssetResponse response = assetService.getAsset(id);
            return ResponseEntity.ok(Map.of("photoUrl", response.getPhotoUrl() != null ? response.getPhotoUrl() : ""));
        } catch (RuntimeException e) {
            if (e.getMessage().contains("不存在")) {
                return ResponseEntity.notFound().build();
            }
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @DeleteMapping("/{id}/photo")
    public ResponseEntity<?> deleteAssetPhoto(@PathVariable Long id) {
        try {
            assetService.deleteAssetPhoto(id);
            return ResponseEntity.ok().build();
        } catch (RuntimeException e) {
            if (e.getMessage().contains("不存在")) {
                return ResponseEntity.notFound().build();
            }
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}