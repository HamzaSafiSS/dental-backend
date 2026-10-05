package com.dental.dentalbackend.contact.controller;

import com.dental.dentalbackend.common.dto.ApiResponse;
import com.dental.dentalbackend.contact.dto.ContactRequest;
import com.dental.dentalbackend.contact.service.ContactEmailService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Public endpoint — no authentication required.
 * Accessible at POST /api/v1/public/contact
 *
 * The /api/v1/public/** pattern is already permitted for all in SecurityConfig.
 */
@RestController
@RequestMapping("/api/v1/public/contact")
@RequiredArgsConstructor
@Tag(name = "Contact", description = "Public contact form — no authentication required")
public class ContactController {

    private final ContactEmailService contactEmailService;

    @PostMapping
    @Operation(summary = "Submit contact form",
               description = "Sends a message to the clinic and an auto-reply to the visitor. No login required.")
    public ResponseEntity<ApiResponse<Void>> submit(@Valid @RequestBody ContactRequest request) {
        contactEmailService.sendContactEmails(request);
        return ResponseEntity.ok(
                ApiResponse.success("Your message has been sent! We'll get back to you soon.")
        );
    }
}
