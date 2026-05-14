package com.ams.controller;

import com.ams.dto.AssetTagResponse;
import com.ams.service.AssetTagService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/assets")
@RequiredArgsConstructor
public class AssetTagController {

    private final AssetTagService assetTagService;

    @GetMapping("/{id}/tag")
    public ResponseEntity<?> getAssetTag(@PathVariable Long id) {
        try {
            AssetTagResponse response = assetTagService.generateQRCode(id, null);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            if (e.getMessage().contains("不存在")) {
                return ResponseEntity.notFound().build();
            }
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/{id}/tag/print")
    public ResponseEntity<?> printAssetTag(@PathVariable Long id) {
        try {
            AssetTagResponse response = assetTagService.generateQRCode(id, null);
            return ResponseEntity.ok()
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(response);
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
