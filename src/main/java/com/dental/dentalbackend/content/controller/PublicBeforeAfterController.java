package com.dental.dentalbackend.content.controller;

import com.dental.dentalbackend.common.dto.ApiResponse;
import com.dental.dentalbackend.content.dto.BeforeAfterCaseResponse;
import com.dental.dentalbackend.content.service.BeforeAfterService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/public/before-after")
@RequiredArgsConstructor
@Tag(name = "Public Before/After Cases", description = "Public-facing active before/after treatment cases with patient consent")
public class PublicBeforeAfterController {

    private final BeforeAfterService beforeAfterService;

    @GetMapping
    @Operation(summary = "List active before/after cases that have verified patient consent")
    public ResponseEntity<ApiResponse<List<BeforeAfterCaseResponse>>> listPublicCases(
            @RequestParam(required = false) UUID serviceId) {
        List<BeforeAfterCaseResponse> cases = beforeAfterService.listPublicCases(serviceId);
        return ResponseEntity.ok(ApiResponse.success(cases));
    }
}
