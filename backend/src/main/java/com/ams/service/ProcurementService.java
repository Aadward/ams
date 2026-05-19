package com.ams.service;

import com.ams.dto.ProcurementRequestDTO;
import com.ams.entity.Asset;
import com.ams.entity.Employee;
import com.ams.entity.ProcurementRequest;
import com.ams.enums.AssetCategory;
import com.ams.enums.AssetStatus;
import com.ams.enums.NotificationType;
import com.ams.enums.ProcurementStatus;
import com.ams.enums.UserRole;
import com.ams.repository.AssetRepository;
import com.ams.repository.DepartmentRepository;
import com.ams.repository.EmployeeRepository;
import com.ams.repository.ProcurementRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProcurementService {

    private final ProcurementRepository procurementRepository;
    private final EmployeeRepository employeeRepository;
    private final DepartmentRepository departmentRepository;
    private final NotificationService notificationService;
    private final AssetRepository assetRepository;

    private ProcurementRequestDTO toResponse(ProcurementRequest p) {
        String requesterName = employeeRepository.findById(p.getRequesterId())
                .map(e -> e.getName()).orElse("未知");
        String deptName = departmentRepository.findById(p.getDepartmentId())
                .map(d -> d.getName()).orElse("未知");
        String approverName = p.getApproverId() != null ?
                employeeRepository.findById(p.getApproverId())
                        .map(e -> e.getName()).orElse("未知") : null;

        return ProcurementRequestDTO.builder()
                .id(p.getId())
                .requesterId(p.getRequesterId())
                .requesterName(requesterName)
                .departmentId(p.getDepartmentId())
                .departmentName(deptName)
                .assetName(p.getAssetName())
                .category(p.getCategory())
                .budget(p.getBudget())
                .status(p.getStatus())
                .reason(p.getReason())
                .managerComment(p.getManagerComment())
                .approverId(p.getApproverId())
                .approverName(approverName)
                .createdAt(p.getCreatedAt())
                .updatedAt(p.getUpdatedAt())
                .resolvedAt(p.getResolvedAt())
                .build();
    }

    @Transactional
    public ProcurementRequest createProcurementRequest(Long requesterId, Long departmentId,
                                                       String assetName, String category,
                                                       BigDecimal budget, String reason) {
        log.info("Creating procurement request: requesterId={}, assetName={}", requesterId, assetName);

        ProcurementRequest request = ProcurementRequest.builder()
                .requesterId(requesterId)
                .departmentId(departmentId)
                .assetName(assetName)
                .category(category)
                .budget(budget)
                .reason(reason)
                .status(ProcurementStatus.PENDING)
                .build();

        ProcurementRequest saved = procurementRepository.save(request);

        // Notify all ADMIN/MANAGER about the new procurement request
        String requesterName = employeeRepository.findById(requesterId)
                .map(e -> e.getName()).orElse("未知");
        List<Employee> managers = employeeRepository.findByRole(UserRole.ADMIN);
        managers.addAll(employeeRepository.findByRole(UserRole.MANAGER));
        for (Employee manager : managers) {
            notificationService.createNotification(
                    manager.getId(),
                    "新的采购申请待审批",
                    "员工 " + requesterName + " 提交了采购申请「" + assetName + "」，请尽快审批",
                    NotificationType.APPROVAL_REQUIRED
            );
        }

        return saved;
    }

    @Transactional(readOnly = true)
    public List<ProcurementRequestDTO> getAllProcurementRequests() {
        return procurementRepository.findAll().stream()
                .map(this::toResponse).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<ProcurementRequestDTO> getProcurementRequestsByRequester(Long requesterId) {
        return procurementRepository.findByRequesterId(requesterId).stream()
                .map(this::toResponse).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<ProcurementRequestDTO> getProcurementRequestsByDepartment(Long departmentId) {
        return procurementRepository.findByDepartmentId(departmentId).stream()
                .map(this::toResponse).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<ProcurementRequestDTO> getPendingProcurementRequests() {
        return procurementRepository.findByStatus(ProcurementStatus.PENDING).stream()
                .map(this::toResponse).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<ProcurementRequestDTO> getProcurementRequestsByStatus(ProcurementStatus status) {
        return procurementRepository.findByStatus(status).stream()
                .map(this::toResponse).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public ProcurementRequestDTO getProcurementRequest(Long id) {
        ProcurementRequest request = procurementRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Procurement request not found"));
        return toResponse(request);
    }

    @Transactional
    public ProcurementRequest approveProcurement(Long id, Long approverId, String managerComment) {
        log.info("Approving procurement request id={}, approverId={}", id, approverId);

        ProcurementRequest request = procurementRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Procurement request not found"));

        if (request.getStatus() != ProcurementStatus.PENDING) {
            throw new RuntimeException("Only pending requests can be approved");
        }

        request.setStatus(ProcurementStatus.APPROVED);
        request.setApproverId(approverId);
        request.setManagerComment(managerComment);
        request.setResolvedAt(LocalDateTime.now());

        ProcurementRequest saved = procurementRepository.save(request);

        // Auto-create Asset from approved procurement request
        AssetCategory assetCategory = AssetCategory.HARDWARE;
        if (request.getCategory() != null) {
            try {
                assetCategory = AssetCategory.valueOf(request.getCategory().toUpperCase());
            } catch (IllegalArgumentException e) {
                log.warn("Unknown category '{}', defaulting to HARDWARE", request.getCategory());
            }
        }

        // Generate asset code: PROC-{id}-{timestamp}
        String assetCode = String.format("PROC-%d-%d", request.getId(), System.currentTimeMillis() % 10000);

        Asset asset = Asset.builder()
                .assetCode(assetCode)
                .name(request.getAssetName())
                .category(assetCategory)
                .status(AssetStatus.IN_STOCK)
                .purchaseDate(LocalDate.now())
                .purchasePrice(request.getBudget())
                .assignee(employeeRepository.findById(request.getRequesterId()).orElse(null))
                .deleted(false)
                .build();
        assetRepository.save(asset);
        log.info("Auto-created asset {} from approved procurement request {}", assetCode, id);

        // Notify the requester
        String requesterName = employeeRepository.findById(request.getRequesterId())
                .map(e -> e.getName()).orElse("");
        notificationService.createNotification(
                saved.getRequesterId(),
                "采购申请已批准",
                "您的采购申请「" + request.getAssetName() + "」已批准",
                NotificationType.PROCUREMENT_APPROVED
        );

        return saved;
    }

    @Transactional
    public ProcurementRequest rejectProcurement(Long id, Long approverId, String managerComment) {
        log.info("Rejecting procurement request id={}, approverId={}", id, approverId);

        ProcurementRequest request = procurementRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Procurement request not found"));

        if (request.getStatus() != ProcurementStatus.PENDING) {
            throw new RuntimeException("Only pending requests can be rejected");
        }

        request.setStatus(ProcurementStatus.REJECTED);
        request.setApproverId(approverId);
        request.setManagerComment(managerComment);
        request.setResolvedAt(LocalDateTime.now());

        ProcurementRequest saved = procurementRepository.save(request);

        // Notify the requester
        notificationService.createNotification(
                saved.getRequesterId(),
                "采购申请被拒绝",
                "您的采购申请「" + request.getAssetName() + "」已被拒绝",
                NotificationType.PROCUREMENT_REJECTED
        );

        return saved;
    }

    @Transactional
    public ProcurementRequest updateProcurementRequest(Long id, String assetName, String category,
                                                        BigDecimal budget, String reason) {
        log.info("Updating procurement request id={}", id);

        ProcurementRequest request = procurementRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Procurement request not found"));

        if (request.getStatus() != ProcurementStatus.PENDING) {
            throw new RuntimeException("Only pending requests can be updated");
        }

        if (assetName != null) {
            request.setAssetName(assetName);
        }
        if (category != null) {
            request.setCategory(category);
        }
        if (budget != null) {
            request.setBudget(budget);
        }
        if (reason != null) {
            request.setReason(reason);
        }

        return procurementRepository.save(request);
    }
}
