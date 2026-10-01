package com.dental.dentalbackend.content.service;

import com.dental.dentalbackend.audit.service.AuditService;
import com.dental.dentalbackend.common.exception.DuplicateResourceException;
import com.dental.dentalbackend.common.exception.ResourceNotFoundException;
import com.dental.dentalbackend.content.dto.*;
import com.dental.dentalbackend.content.entity.Faq;
import com.dental.dentalbackend.content.entity.FaqCategory;
import com.dental.dentalbackend.content.repository.FaqCategoryRepository;
import com.dental.dentalbackend.content.repository.FaqRepository;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class FaqService {

    private final FaqCategoryRepository categoryRepository;
    private final FaqRepository faqRepository;
    private final AuditService auditService;

    // ── Categories ────────────────────────────────────────────────────────

    @Transactional
    public FaqCategoryResponse createCategory(FaqCategoryRequest request, UUID adminId, HttpServletRequest httpRequest) {
        if (categoryRepository.existsByName(request.getName())) {
            throw new DuplicateResourceException("FaqCategory", "name", request.getName());
        }

        String slug = (request.getSlug() != null && !request.getSlug().isBlank())
                ? generateSlug(request.getSlug())
                : generateSlug(request.getName());

        if (categoryRepository.existsBySlug(slug)) {
            throw new DuplicateResourceException("FaqCategory", "slug", slug);
        }

        FaqCategory category = new FaqCategory();
        category.setName(request.getName());
        category.setSlug(slug);
        category.setDescription(request.getDescription());
        category.setDisplayOrder(request.getDisplayOrder() != null ? request.getDisplayOrder() : 0);
        category.setActive(request.getActive() != null ? request.getActive() : true);

        category = categoryRepository.save(category);

        auditService.log(adminId, "FAQ_CATEGORY_CREATED", "FaqCategory", category.getId(),
                "Created FAQ category: " + category.getName(), httpRequest);

        return mapCategoryResponse(category);
    }

    @Transactional
    public FaqCategoryResponse updateCategory(UUID id, FaqCategoryRequest request, UUID adminId, HttpServletRequest httpRequest) {
        FaqCategory category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("FaqCategory", id));

        if (!category.getName().equalsIgnoreCase(request.getName()) && categoryRepository.existsByName(request.getName())) {
            throw new DuplicateResourceException("FaqCategory", "name", request.getName());
        }

        if (request.getSlug() != null && !request.getSlug().isBlank()) {
            String newSlug = generateSlug(request.getSlug());
            if (!newSlug.equals(category.getSlug()) && categoryRepository.existsBySlug(newSlug)) {
                throw new DuplicateResourceException("FaqCategory", "slug", newSlug);
            }
            category.setSlug(newSlug);
        }

        category.setName(request.getName());
        category.setDescription(request.getDescription());
        if (request.getDisplayOrder() != null) {
            category.setDisplayOrder(request.getDisplayOrder());
        }
        if (request.getActive() != null) {
            category.setActive(request.getActive());
        }

        category = categoryRepository.save(category);

        auditService.log(adminId, "FAQ_CATEGORY_UPDATED", "FaqCategory", category.getId(),
                "Updated FAQ category: " + category.getName(), httpRequest);

        return mapCategoryResponse(category);
    }

    @Transactional(readOnly = true)
    public List<FaqCategoryResponse> listCategories(boolean onlyActive) {
        List<FaqCategory> categories = onlyActive
                ? categoryRepository.findByActiveTrueOrderByDisplayOrderAsc()
                : categoryRepository.findAllByOrderByDisplayOrderAsc();

        return categories.stream().map(this::mapCategoryResponse).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public FaqCategoryResponse getCategoryById(UUID id) {
        FaqCategory category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("FaqCategory", id));
        return mapCategoryResponse(category);
    }

    // ── FAQs ──────────────────────────────────────────────────────────────

    @Transactional
    public FaqResponse createFaq(CreateFaqRequest request, UUID adminId, HttpServletRequest httpRequest) {
        Faq faq = new Faq();
        faq.setQuestion(request.getQuestion());
        faq.setAnswer(request.getAnswer());
        faq.setDisplayOrder(request.getDisplayOrder() != null ? request.getDisplayOrder() : 0);
        faq.setActive(request.getActive() != null ? request.getActive() : true);

        if (request.getCategoryId() != null) {
            FaqCategory category = categoryRepository.findById(request.getCategoryId())
                    .orElseThrow(() -> new ResourceNotFoundException("FaqCategory", request.getCategoryId()));
            faq.setCategory(category);
        }

        faq = faqRepository.save(faq);

        auditService.log(adminId, "FAQ_CREATED", "Faq", faq.getId(),
                "Created FAQ: " + faq.getQuestion(), httpRequest);

        return mapFaqResponse(faq);
    }

    @Transactional
    public FaqResponse updateFaq(UUID id, UpdateFaqRequest request, UUID adminId, HttpServletRequest httpRequest) {
        Faq faq = faqRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Faq", id));

        faq.setQuestion(request.getQuestion());
        faq.setAnswer(request.getAnswer());
        if (request.getDisplayOrder() != null) {
            faq.setDisplayOrder(request.getDisplayOrder());
        }
        if (request.getActive() != null) {
            faq.setActive(request.getActive());
        }

        if (request.getCategoryId() != null) {
            FaqCategory category = categoryRepository.findById(request.getCategoryId())
                    .orElseThrow(() -> new ResourceNotFoundException("FaqCategory", request.getCategoryId()));
            faq.setCategory(category);
        } else {
            faq.setCategory(null);
        }

        faq = faqRepository.save(faq);

        auditService.log(adminId, "FAQ_UPDATED", "Faq", faq.getId(),
                "Updated FAQ: " + faq.getQuestion(), httpRequest);

        return mapFaqResponse(faq);
    }

    @Transactional
    public void deleteFaq(UUID id, UUID adminId, HttpServletRequest httpRequest) {
        Faq faq = faqRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Faq", id));

        faqRepository.delete(faq);

        auditService.log(adminId, "FAQ_DELETED", "Faq", id,
                "Deleted FAQ: " + faq.getQuestion(), httpRequest);
    }

    @Transactional(readOnly = true)
    public List<FaqResponse> listPublicFaqs(UUID categoryId) {
        List<Faq> faqs = categoryId != null
                ? faqRepository.findByCategoryIdAndActiveTrueOrderByDisplayOrderAsc(categoryId)
                : faqRepository.findByActiveTrueOrderByDisplayOrderAsc();

        return faqs.stream().map(this::mapFaqResponse).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public Page<FaqResponse> listAllFaqs(UUID categoryId, Pageable pageable) {
        Page<Faq> page = categoryId != null
                ? faqRepository.findByCategoryId(categoryId, pageable)
                : faqRepository.findAll(pageable);

        return page.map(this::mapFaqResponse);
    }

    @Transactional(readOnly = true)
    public FaqResponse getFaqById(UUID id) {
        Faq faq = faqRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Faq", id));
        return mapFaqResponse(faq);
    }

    // ── Helpers ───────────────────────────────────────────────────────────

    private String generateSlug(String text) {
        if (text == null) return "";
        return text.toLowerCase()
                .replaceAll("[^a-z0-9\\s-]", "")
                .replaceAll("\\s+", "-")
                .replaceAll("-+", "-")
                .replaceAll("^-|-$", "");
    }

    private FaqCategoryResponse mapCategoryResponse(FaqCategory category) {
        return FaqCategoryResponse.builder()
                .id(category.getId())
                .name(category.getName())
                .slug(category.getSlug())
                .description(category.getDescription())
                .displayOrder(category.getDisplayOrder())
                .active(category.isActive())
                .createdAt(category.getCreatedAt())
                .updatedAt(category.getUpdatedAt())
                .build();
    }

    private FaqResponse mapFaqResponse(Faq faq) {
        return FaqResponse.builder()
                .id(faq.getId())
                .categoryId(faq.getCategory() != null ? faq.getCategory().getId() : null)
                .categoryName(faq.getCategory() != null ? faq.getCategory().getName() : null)
                .question(faq.getQuestion())
                .answer(faq.getAnswer())
                .displayOrder(faq.getDisplayOrder())
                .active(faq.isActive())
                .createdAt(faq.getCreatedAt())
                .updatedAt(faq.getUpdatedAt())
                .build();
    }
}
