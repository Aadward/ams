package com.ams.controller;

import com.ams.entity.Asset;
import com.ams.entity.MaintenanceRecord;
import com.ams.entity.Supplier;
import com.ams.service.SupplierService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/suppliers")
@RequiredArgsConstructor
public class SupplierController {

    private final SupplierService supplierService;

    @GetMapping
    public ResponseEntity<?> listSuppliers(
            @RequestParam(required = false) String type,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        try {
            Pageable pageable = PageRequest.of(page, size);
            Page<Supplier> result = supplierService.searchSuppliers(type, status, keyword, pageable);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getSupplier(@PathVariable Long id) {
        try {
            return ResponseEntity.ok(supplierService.getSupplier(id));
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping
    public ResponseEntity<?> createSupplier(@RequestBody Map<String, String> request) {
        try {
            Supplier supplier = supplierService.createSupplier(
                    request.get("supplierCode"),
                    request.get("name"),
                    request.get("type"),
                    request.get("status"),
                    request.get("contact"),
                    request.get("phone"),
                    request.get("email"),
                    request.get("address"),
                    request.get("remark"),
                    request.get("rating") != null ? Double.parseDouble(request.get("rating")) : null
            );
            return ResponseEntity.ok(supplier);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateSupplier(@PathVariable Long id, @RequestBody Map<String, String> request) {
        try {
            Supplier supplier = supplierService.updateSupplier(
                    id,
                    request.get("name"),
                    request.get("type"),
                    request.get("status"),
                    request.get("contact"),
                    request.get("phone"),
                    request.get("email"),
                    request.get("address"),
                    request.get("remark")
            );
            return ResponseEntity.ok(supplier);
        } catch (RuntimeException e) {
            if (e.getMessage().contains("不存在")) {
                return ResponseEntity.notFound().build();
            }
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteSupplier(@PathVariable Long id) {
        try {
            supplierService.deleteSupplier(id);
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

    @GetMapping("/active")
    public ResponseEntity<?> getActiveSuppliers() {
        try {
            List<Supplier> suppliers = supplierService.getAllActiveSuppliers();
            return ResponseEntity.ok(suppliers);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/statistics")
    public ResponseEntity<?> getStatistics() {
        try {
            return ResponseEntity.ok(supplierService.getStatistics());
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/{id}/assets")
    public ResponseEntity<?> getAssetsBySupplier(@PathVariable Long id) {
        try {
            List<Asset> assets = supplierService.getAssetsBySupplier(id);
            return ResponseEntity.ok(assets);
        } catch (RuntimeException e) {
            if (e.getMessage().contains("不存在")) {
                return ResponseEntity.notFound().build();
            }
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/{id}/maintenance-records")
    public ResponseEntity<?> getMaintenanceRecordsBySupplier(@PathVariable Long id) {
        try {
            List<MaintenanceRecord> records = supplierService.getMaintenanceRecordsBySupplier(id);
            return ResponseEntity.ok(records);
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
