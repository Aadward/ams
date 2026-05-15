package com.ams.repository;

import com.ams.entity.Supplier;
import com.ams.enums.SupplierStatus;
import com.ams.enums.SupplierType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SupplierRepository extends JpaRepository<Supplier, Long> {

    Page<Supplier> findByDeletedFalse(Pageable pageable);

    Page<Supplier> findByDeletedFalseAndType(SupplierType type, Pageable pageable);

    Page<Supplier> findByDeletedFalseAndStatus(SupplierStatus status, Pageable pageable);

    @Query("SELECT s FROM Supplier s WHERE s.deleted = false AND " +
           "(:type IS NULL OR s.type = :type) AND " +
           "(:status IS NULL OR s.status = :status) AND " +
           "(:keyword IS NULL OR s.name LIKE %:keyword% OR s.supplierCode LIKE %:keyword%)")
    Page<Supplier> searchSuppliers(
            @Param("type") SupplierType type,
            @Param("status") SupplierStatus status,
            @Param("keyword") String keyword,
            Pageable pageable);

    Optional<Supplier> findByIdAndDeletedFalse(Long id);

    Optional<Supplier> findBySupplierCodeAndDeletedFalse(String supplierCode);

    List<Supplier> findByDeletedFalse();

    List<Supplier> findByDeletedFalseAndStatus(SupplierStatus status);

    long countByStatus(SupplierStatus status);
}
