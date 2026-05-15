package com.ams.service;

import com.ams.entity.Asset;
import com.ams.entity.MaintenanceRecord;
import com.ams.entity.Supplier;
import com.ams.enums.SupplierStatus;
import com.ams.enums.SupplierType;
import com.ams.repository.AssetRepository;
import com.ams.repository.MaintenanceRecordRepository;
import com.ams.repository.SupplierRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class SupplierService {

    private final SupplierRepository supplierRepository;
    private final AssetRepository assetRepository;
    private final MaintenanceRecordRepository maintenanceRecordRepository;

    @Transactional
    public Supplier createSupplier(String supplierCode, String name, String type, String contact,
                                   String phone, String email, String address, String remark) {
        // Auto-generate supplierCode if not provided
        if (supplierCode == null || supplierCode.isBlank()) {
            long count = supplierRepository.count() + 1;
            supplierCode = String.format("SUP%05d", count);
            // Ensure unique
            while (supplierRepository.findBySupplierCodeAndDeletedFalse(supplierCode).isPresent()) {
                count++;
                supplierCode = String.format("SUP%05d", count);
            }
        } else {
            if (supplierRepository.findBySupplierCodeAndDeletedFalse(supplierCode).isPresent()) {
                throw new RuntimeException("供应商编号已存在");
            }
        }

        Supplier supplier = Supplier.builder()
                .supplierCode(supplierCode)
                .name(name)
                .type(SupplierType.valueOf(type))
                .status(SupplierStatus.ACTIVE)
                .contact(contact)
                .phone(phone)
                .email(email)
                .address(address)
                .remark(remark)
                .deleted(false)
                .build();
        return supplierRepository.save(supplier);
    }

    @Transactional
    public Supplier updateSupplier(Long id, String name, String type, String status,
                                   String contact, String phone, String email, String address, String remark) {
        Supplier supplier = supplierRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new RuntimeException("供应商不存在"));

        if (name != null) {
            supplier.setName(name);
        }
        if (type != null) {
            supplier.setType(SupplierType.valueOf(type));
        }
        if (status != null) {
            supplier.setStatus(SupplierStatus.valueOf(status));
        }
        if (contact != null) {
            supplier.setContact(contact);
        }
        if (phone != null) {
            supplier.setPhone(phone);
        }
        if (email != null) {
            supplier.setEmail(email);
        }
        if (address != null) {
            supplier.setAddress(address);
        }
        if (remark != null) {
            supplier.setRemark(remark);
        }

        return supplierRepository.save(supplier);
    }

    @Transactional(readOnly = true)
    public Supplier getSupplier(Long id) {
        return supplierRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new RuntimeException("供应商不存在"));
    }

    @Transactional
    public void deleteSupplier(Long id) {
        Supplier supplier = supplierRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new RuntimeException("供应商不存在"));
        supplier.setDeleted(true);
        supplierRepository.save(supplier);
    }

    @Transactional(readOnly = true)
    public Page<Supplier> searchSuppliers(String type, String status, String keyword, Pageable pageable) {
        SupplierType supplierType = null;
        SupplierStatus supplierStatus = null;

        if (type != null && !type.isEmpty()) {
            supplierType = SupplierType.valueOf(type);
        }
        if (status != null && !status.isEmpty()) {
            supplierStatus = SupplierStatus.valueOf(status);
        }

        return supplierRepository.searchSuppliers(supplierType, supplierStatus, keyword, pageable);
    }

    @Transactional(readOnly = true)
    public List<Supplier> getAllActiveSuppliers() {
        return supplierRepository.findByDeletedFalseAndStatus(SupplierStatus.ACTIVE);
    }

    @Transactional(readOnly = true)
    public Map<String, Object> getStatistics() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("total", supplierRepository.findByDeletedFalse().size());
        stats.put("active", supplierRepository.countByStatus(SupplierStatus.ACTIVE));
        stats.put("inactive", supplierRepository.countByStatus(SupplierStatus.INACTIVE));
        return stats;
    }

    @Transactional(readOnly = true)
    public List<Asset> getAssetsBySupplier(Long supplierId) {
        Supplier supplier = supplierRepository.findByIdAndDeletedFalse(supplierId)
                .orElseThrow(() -> new RuntimeException("供应商不存在"));
        return assetRepository.findByDeletedFalseAndSupplierContainingIgnoreCase(supplier.getName());
    }

    @Transactional(readOnly = true)
    public List<MaintenanceRecord> getMaintenanceRecordsBySupplier(Long supplierId) {
        Supplier supplier = supplierRepository.findByIdAndDeletedFalse(supplierId)
                .orElseThrow(() -> new RuntimeException("供应商不存在"));
        return maintenanceRecordRepository.findByVendorContainingIgnoreCase(supplier.getName());
    }
}
