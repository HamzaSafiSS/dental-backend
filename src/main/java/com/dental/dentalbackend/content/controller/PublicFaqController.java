package com.dental.dentalbackend.content.controller;

import com.dental.dentalbackend.common.dto.ApiResponse;
import com.dental.dentalbackend.content.dto.FaqCategoryResponse;
import com.dental.dentalbackend.content.dto.FaqResponse;
import com.dental.dentalbackend.content.service.FaqService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/public")
@RequiredArgsConstructor
@Tag(name = "Public FAQs", description = "Public-facing FAQ endpoints")
public class PublicFaqController {

    private final FaqService faqService;

    @GetMapping("/faqs")
    @Operation(summary = "List active FAQs ordered by display order")
    public ResponseEntity<ApiResponse<List<FaqResponse>>> listActiveFaqs(
            @RequestParam(required = false) UUID categoryId) {
        List<FaqResponse> faqs = faqService.listPublicFaqs(categoryId);
        return ResponseEntity.ok(ApiResponse.success(faqs));
    }

    @GetMapping("/faq-categories")
    @Operation(summary = "List active FAQ categories")
    public ResponseEntity<ApiResponse<List<FaqCategoryResponse>>> listActiveCategories() {
        List<FaqCategoryResponse> categories = faqService.listCategories(true);
        return ResponseEntity.ok(ApiResponse.success(categories));
    }
}
