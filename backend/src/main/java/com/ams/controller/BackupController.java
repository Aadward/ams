package com.ams.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Slf4j
@RestController
@RequestMapping("/api/backup")
@RequiredArgsConstructor
public class BackupController {

    @Value("${app.backup.directory:/tmp/ams-backups}")
    private String backupDirectory;

    @Value("${app.backup.command:scripts/backup-db.sh}")
    private String backupCommand;

    @PostMapping("/create")
    public ResponseEntity<Map<String, Object>> createBackup() {
        log.info("Received backup creation request");
        
        try {
            ProcessBuilder pb = new ProcessBuilder("/bin/bash", backupCommand, backupDirectory);
            pb.redirectErrorStream(true);
            Process process = pb.start();
            
            StringBuilder output = new StringBuilder();
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    output.append(line).append("\n");
                }
            }
            
            int exitCode = process.waitFor();
            
            Map<String, Object> response = new HashMap<>();
            if (exitCode == 0) {
                String backupFile = output.toString().trim();
                File file = new File(backupFile);
                
                response.put("success", true);
                response.put("message", "Backup created successfully");
                response.put("file", file.getName());
                response.put("path", backupFile);
                response.put("size", file.exists() ? file.length() : 0);
                response.put("timestamp", Instant.now().toString());
                
                log.info("Backup created: {}", backupFile);
                return ResponseEntity.ok(response);
            } else {
                response.put("success", false);
                response.put("message", "Backup failed with exit code: " + exitCode);
                response.put("output", output.toString());
                
                log.error("Backup failed: {}", output);
                return ResponseEntity.status(500).body(response);
            }
        } catch (IOException | InterruptedException e) {
            log.error("Backup error", e);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Backup error: " + e.getMessage());
            
            return ResponseEntity.status(500).body(response);
        }
    }

    @GetMapping("/list")
    public ResponseEntity<Map<String, Object>> listBackups() {
        log.info("Listing backups from: {}", backupDirectory);
        
        Map<String, Object> response = new HashMap<>();
        List<Map<String, Object>> backups = new ArrayList<>();
        
        try {
            Path backupPath = Paths.get(backupDirectory);
            if (!Files.exists(backupPath)) {
                response.put("success", true);
                response.put("backups", backups);
                response.put("count", 0);
                response.put("message", "Backup directory does not exist");
                return ResponseEntity.ok(response);
            }
            
            File[] files = backupPath.toFile().listFiles((dir, name) -> 
                name.endsWith(".sql") && name.startsWith("ams_db"));
            
            if (files != null) {
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
                    .withZone(ZoneId.systemDefault());
                
                Arrays.sort(files, (f1, f2) -> Long.compare(f2.lastModified(), f1.lastModified()));
                
                for (File file : files) {
                    Map<String, Object> backup = new HashMap<>();
                    backup.put("name", file.getName());
                    backup.put("path", file.getAbsolutePath());
                    backup.put("size", file.length());
                    backup.put("sizeFormatted", formatFileSize(file.length()));
                    backup.put("lastModified", formatter.format(Instant.ofEpochMilli(file.lastModified())));
                    backups.add(backup);
                }
            }
            
            response.put("success", true);
            response.put("backups", backups);
            response.put("count", backups.size());
            response.put("directory", backupDirectory);
            
            log.info("Found {} backups", backups.size());
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("Error listing backups", e);
            response.put("success", false);
            response.put("message", "Error listing backups: " + e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }

    private String formatFileSize(long bytes) {
        if (bytes < 1024) return bytes + " B";
        int exp = (int) (Math.log(bytes) / Math.log(1024));
        String pre = "KMGTPE".charAt(exp - 1) + "B";
        return String.format("%.2f %s", bytes / Math.pow(1024, exp), pre);
    }
}
