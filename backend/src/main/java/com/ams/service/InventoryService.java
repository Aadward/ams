package com.ams.service;

import com.ams.dto.*;
import com.ams.entity.*;
import com.ams.enums.*;
import com.ams.repository.*;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayOutputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class InventoryService {

    private final InventoryPlanRepository planRepository;
    private final InventoryTaskRepository taskRepository;
    private final InventoryRecordRepository recordRepository;
    private final AssetRepository assetRepository;
    private final DepartmentRepository departmentRepository;
    private final EmployeeRepository employeeRepository;
    private final ObjectMapper objectMapper;

    // ==================== Plan Operations ====================

    @Transactional
    public InventoryPlanResponse createPlan(InventoryPlanRequest request, Long creatorId) {
        InventoryPlan plan = InventoryPlan.builder()
                .name(request.getName())
                .scopeType(InventoryScopeType.valueOf(request.getScopeType()))
                .departmentIds(toJson(request.getDepartmentIds()))
                .categoryIds(toJson(request.getCategoryIds()))
                .planDate(request.getPlanDate())
                .status(InventoryPlanStatus.PENDING)
                .creatorId(creatorId)
                .createdAt(LocalDateTime.now())
                .build();
        plan = planRepository.save(plan);

        // Generate tasks based on scope
        int taskCount = generateTasks(plan, request);
        log.info("Created inventory plan {} with {} tasks", plan.getId(), taskCount);

        return toPlanResponse(plan);
    }

    private int generateTasks(InventoryPlan plan, InventoryPlanRequest request) {
        List<Asset> assets = new ArrayList<>();

        if (plan.getScopeType() == InventoryScopeType.DEPARTMENT && request.getDepartmentIds() != null) {
            // Find assets assigned to these departments
            List<Employee> employees = employeeRepository.findAll().stream()
                    .filter(e -> e.getDepartment() != null && request.getDepartmentIds().contains(e.getDepartment().getId()))
                    .collect(Collectors.toList());
            List<Long> assigneeIds = employees.stream().map(Employee::getId).collect(Collectors.toList());
            assets = assetRepository.findAll().stream()
                    .filter(a -> !a.getDeleted() && a.getAssignee() != null && assigneeIds.contains(a.getAssignee().getId()))
                    .collect(Collectors.toList());
        } else if (plan.getScopeType() == InventoryScopeType.CATEGORY && request.getCategoryIds() != null) {
            List<AssetCategory> categories = request.getCategoryIds().stream()
                    .map(id -> AssetCategory.values()[id.intValue()])
                    .collect(Collectors.toList());
            assets = assetRepository.findAll().stream()
                    .filter(a -> !a.getDeleted() && categories.contains(a.getCategory()))
                    .collect(Collectors.toList());
        }

        // Use first assignee if multiple, otherwise use creator
        Long primaryAssigneeId = (request.getAssigneeIds() != null && !request.getAssigneeIds().isEmpty())
                ? request.getAssigneeIds().get(0) : plan.getCreatorId();

        for (Asset asset : assets) {
            InventoryTask task = InventoryTask.builder()
                    .planId(plan.getId())
                    .assetId(asset.getId())
                    .assigneeId(primaryAssigneeId)
                    .status(InventoryTaskStatus.PENDING)
                    .createdAt(LocalDateTime.now())
                    .build();
            taskRepository.save(task);
        }
        return assets.size();
    }

    public List<InventoryPlanResponse> listPlans() {
        return planRepository.findAllByOrderByCreatedAtDesc().stream()
                .map(this::toPlanResponse)
                .collect(Collectors.toList());
    }

    public InventoryPlanResponse getPlan(Long id) {
        InventoryPlan plan = planRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Plan not found: " + id));
        return toPlanResponse(plan);
    }

    @Transactional
    public InventoryPlanResponse startPlan(Long id) {
        InventoryPlan plan = planRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Plan not found: " + id));
        plan.setStatus(InventoryPlanStatus.IN_PROGRESS);
        plan.setUpdatedAt(LocalDateTime.now());
        plan = planRepository.save(plan);
        return toPlanResponse(plan);
    }

    @Transactional
    public InventoryPlanResponse completePlan(Long id) {
        InventoryPlan plan = planRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Plan not found: " + id));

        // Mark unchecked tasks as MISSING
        List<InventoryTask> uncheckedTasks = taskRepository.findByPlanIdAndStatus(id, InventoryTaskStatus.PENDING);
        for (InventoryTask task : uncheckedTasks) {
            task.setStatus(InventoryTaskStatus.CHECKED); // Mark as checked for record
            Asset asset = assetRepository.findById(task.getAssetId()).orElse(null);
            if (asset != null) {
                InventoryRecord record = InventoryRecord.builder()
                        .taskId(task.getId())
                        .planId(id)
                        .assetId(asset.getId())
                        .assetCode(asset.getAssetCode())
                        .assetName(asset.getName())
                        .departmentId(asset.getAssignee() != null && asset.getAssignee().getDepartment() != null
                                ? asset.getAssignee().getDepartment().getId() : null)
                        .departmentName(asset.getAssignee() != null ? asset.getAssignee().getDeptName() : null)
                        .result(InventoryResult.MISSING)
                        .checkedBy(task.getAssigneeId())
                        .checkedAt(LocalDateTime.now())
                        .createdAt(LocalDateTime.now())
                        .build();
                recordRepository.save(record);
            }
        }
        taskRepository.saveAll(uncheckedTasks);

        plan.setStatus(InventoryPlanStatus.COMPLETED);
        plan.setUpdatedAt(LocalDateTime.now());
        plan = planRepository.save(plan);
        return toPlanResponse(plan);
    }

    @Transactional
    public void deletePlan(Long id) {
        // Delete all records, tasks, then plan
        recordRepository.findByPlanId(id).forEach(recordRepository::delete);
        taskRepository.findByPlanId(id).forEach(taskRepository::delete);
        planRepository.deleteById(id);
    }

    // ==================== Task Operations ====================

    public List<InventoryTaskResponse> listTasks(Long planId, Long assigneeId, String status) {
        List<InventoryTask> tasks;
        if (planId != null && assigneeId != null && status != null) {
            tasks = taskRepository.findByAssigneeIdAndStatus(assigneeId, InventoryTaskStatus.valueOf(status));
        } else if (planId != null) {
            tasks = taskRepository.findByPlanId(planId);
        } else if (assigneeId != null) {
            tasks = taskRepository.findByAssigneeId(assigneeId);
        } else {
            tasks = taskRepository.findAll();
        }
        return tasks.stream().map(this::toTaskResponse).collect(Collectors.toList());
    }

    public List<InventoryTaskResponse> getMyTasks(Long assigneeId) {
        return taskRepository.findByAssigneeId(assigneeId).stream()
                .map(this::toTaskResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public InventoryTaskResponse checkAsset(Long taskId, Long userId, String remark) {
        InventoryTask task = taskRepository.findById(taskId)
                .orElseThrow(() -> new RuntimeException("Task not found: " + taskId));

        // Verify user is the assignee
        if (!task.getAssigneeId().equals(userId)) {
            throw new RuntimeException("Not authorized to check this task");
        }

        task.setStatus(InventoryTaskStatus.CHECKED);
        task.setCheckedAt(LocalDateTime.now());
        task.setUpdatedAt(LocalDateTime.now());
        taskRepository.save(task);

        // Create record
        Asset asset = assetRepository.findById(task.getAssetId())
                .orElseThrow(() -> new RuntimeException("Asset not found: " + task.getAssetId()));

        InventoryRecord record = InventoryRecord.builder()
                .taskId(task.getId())
                .planId(task.getPlanId())
                .assetId(asset.getId())
                .assetCode(asset.getAssetCode())
                .assetName(asset.getName())
                .departmentId(asset.getAssignee() != null && asset.getAssignee().getDepartment() != null
                        ? asset.getAssignee().getDepartment().getId() : null)
                .departmentName(asset.getAssignee() != null ? asset.getAssignee().getDeptName() : null)
                .result(InventoryResult.NORMAL)
                .checkedBy(userId)
                .checkedAt(LocalDateTime.now())
                .remark(remark)
                .createdAt(LocalDateTime.now())
                .build();
        recordRepository.save(record);

        return toTaskResponse(task);
    }

    @Transactional
    public InventoryTaskResponse uncheckAsset(Long taskId, Long userId) {
        InventoryTask task = taskRepository.findById(taskId)
                .orElseThrow(() -> new RuntimeException("Task not found: " + taskId));

        if (!task.getAssigneeId().equals(userId)) {
            throw new RuntimeException("Not authorized");
        }

        task.setStatus(InventoryTaskStatus.PENDING);
        task.setCheckedAt(null);
        task.setUpdatedAt(LocalDateTime.now());
        taskRepository.save(task);

        // Delete the associated record
        recordRepository.findAll().stream()
                .filter(r -> r.getTaskId().equals(taskId))
                .findFirst()
                .ifPresent(recordRepository::delete);

        return toTaskResponse(task);
    }

    // ==================== Record / Report Operations ====================

    public List<InventoryRecordResponse> listRecords(Long planId, String result) {
        List<InventoryRecord> records;
        if (result != null) {
            records = recordRepository.findByPlanIdAndResult(planId, InventoryResult.valueOf(result));
        } else {
            records = recordRepository.findByPlanId(planId);
        }
        return records.stream().map(this::toRecordResponse).collect(Collectors.toList());
    }

    public InventoryReportResponse getReport(Long planId) {
        InventoryPlan plan = planRepository.findById(planId)
                .orElseThrow(() -> new RuntimeException("Plan not found: " + planId));

        List<InventoryRecord> allRecords = recordRepository.findByPlanId(planId);
        List<InventoryRecord> normalRecords = recordRepository.findByPlanIdAndResult(planId, InventoryResult.NORMAL);
        List<InventoryRecord> surplusRecords = recordRepository.findByPlanIdAndResult(planId, InventoryResult.SURPLUS);
        List<InventoryRecord> missingRecords = recordRepository.findByPlanIdAndResult(planId, InventoryResult.MISSING);

        long totalTasks = taskRepository.countByPlanId(planId);
        long checkedTasks = taskRepository.countByPlanIdAndStatus(planId, InventoryTaskStatus.CHECKED);
        double completionRate = totalTasks > 0 ? (double) checkedTasks / totalTasks * 100 : 0;

        return InventoryReportResponse.builder()
                .planId(planId)
                .planName(plan.getName())
                .totalAssets((int) totalTasks)
                .checkedAssets((int) checkedTasks)
                .normalCount(normalRecords.size())
                .surplusCount(surplusRecords.size())
                .missingCount(missingRecords.size())
                .pendingCount(allRecords.size() - normalRecords.size() - surplusRecords.size() - missingRecords.size())
                .completionRate(Math.round(completionRate * 100) / 100.0)
                .surplusRecords(surplusRecords.stream().map(this::toRecordResponse).collect(Collectors.toList()))
                .missingRecords(missingRecords.stream().map(this::toRecordResponse).collect(Collectors.toList()))
                .normalRecords(normalRecords.stream().map(this::toRecordResponse).collect(Collectors.toList()))
                .build();
    }

    public byte[] exportToExcel(Long planId) {
        List<InventoryRecord> records = recordRepository.findByPlanId(planId);

        try (Workbook workbook = new XSSFWorkbook();
             ByteArrayOutputStream out = new ByteArrayOutputStream()) {

            Sheet sheet = workbook.createSheet("盘点报告");

            // Create header style
            CellStyle headerStyle = workbook.createCellStyle();
            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerStyle.setFont(headerFont);
            headerStyle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
            headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            headerStyle.setBorderBottom(BorderStyle.THIN);
            headerStyle.setBorderTop(BorderStyle.THIN);
            headerStyle.setBorderLeft(BorderStyle.THIN);
            headerStyle.setBorderRight(BorderStyle.THIN);

            // Create header row
            Row headerRow = sheet.createRow(0);
            String[] headers = {"资产编码", "资产名称", "部门", "盘点结果", "盘点人", "盘点时间", "备注"};
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }

            // Create data rows
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            int rowNum = 1;
            for (InventoryRecord record : records) {
                Row row = sheet.createRow(rowNum++);

                row.createCell(0).setCellValue(record.getAssetCode() != null ? record.getAssetCode() : "");
                row.createCell(1).setCellValue(record.getAssetName() != null ? record.getAssetName() : "");
                row.createCell(2).setCellValue(record.getDepartmentName() != null ? record.getDepartmentName() : "");
                row.createCell(3).setCellValue(record.getResult() != null ? record.getResult().name() : "");

                String checkedByName = employeeRepository.findById(record.getCheckedBy())
                        .map(Employee::getName).orElse("");
                row.createCell(4).setCellValue(checkedByName);

                String checkedAt = record.getCheckedAt() != null ? record.getCheckedAt().format(formatter) : "";
                row.createCell(5).setCellValue(checkedAt);

                row.createCell(6).setCellValue(record.getRemark() != null ? record.getRemark() : "");
            }

            // Auto-size columns
            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
            }

            workbook.write(out);
            return out.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException("Failed to export Excel: " + e.getMessage(), e);
        }
    }

    // ==================== Helper Methods ====================

    private String toJson(List<Long> list) {
        if (list == null || list.isEmpty()) return null;
        try {
            return objectMapper.writeValueAsString(list);
        } catch (JsonProcessingException e) {
            return null;
        }
    }

    private List<Long> fromJson(String json) {
        if (json == null || json.isEmpty()) return new ArrayList<>();
        try {
            return objectMapper.readValue(json,
                objectMapper.getTypeFactory().constructCollectionType(List.class, Long.class));
        } catch (JsonProcessingException e) {
            return new ArrayList<>();
        }
    }

    private InventoryPlanResponse toPlanResponse(InventoryPlan plan) {
        long totalTasks = taskRepository.countByPlanId(plan.getId());
        long checkedTasks = taskRepository.countByPlanIdAndStatus(plan.getId(), InventoryTaskStatus.CHECKED);
        String creatorName = employeeRepository.findById(plan.getCreatorId())
                .map(Employee::getName).orElse("Unknown");

        return InventoryPlanResponse.builder()
                .id(plan.getId())
                .name(plan.getName())
                .scopeType(plan.getScopeType().name())
                .departmentIds(plan.getDepartmentIds())
                .categoryIds(plan.getCategoryIds())
                .planDate(plan.getPlanDate())
                .status(plan.getStatus().name())
                .creatorId(plan.getCreatorId())
                .creatorName(creatorName)
                .createdAt(plan.getCreatedAt())
                .totalTasks((int) totalTasks)
                .checkedTasks((int) checkedTasks)
                .build();
    }

    private InventoryTaskResponse toTaskResponse(InventoryTask task) {
        Asset asset = assetRepository.findById(task.getAssetId()).orElse(null);
        String planName = planRepository.findById(task.getPlanId())
                .map(InventoryPlan::getName).orElse("Unknown");
        String assigneeName = employeeRepository.findById(task.getAssigneeId())
                .map(Employee::getName).orElse("Unknown");

        return InventoryTaskResponse.builder()
                .id(task.getId())
                .planId(task.getPlanId())
                .planName(planName)
                .assetId(task.getAssetId())
                .assetCode(asset != null ? asset.getAssetCode() : null)
                .assetName(asset != null ? asset.getName() : null)
                .category(asset != null ? asset.getCategory().name() : null)
                .assigneeId(task.getAssigneeId())
                .assigneeName(assigneeName)
                .status(task.getStatus().name())
                .checkedAt(task.getCheckedAt())
                .createdAt(task.getCreatedAt())
                .build();
    }

    private InventoryRecordResponse toRecordResponse(InventoryRecord record) {
        String checkedByName = employeeRepository.findById(record.getCheckedBy())
                .map(Employee::getName).orElse(null);

        return InventoryRecordResponse.builder()
                .id(record.getId())
                .taskId(record.getTaskId())
                .planId(record.getPlanId())
                .assetId(record.getAssetId())
                .assetCode(record.getAssetCode())
                .assetName(record.getAssetName())
                .departmentId(record.getDepartmentId())
                .departmentName(record.getDepartmentName())
                .result(record.getResult().name())
                .checkedBy(record.getCheckedBy())
                .checkedByName(checkedByName)
                .checkedAt(record.getCheckedAt())
                .remark(record.getRemark())
                .build();
    }
}
