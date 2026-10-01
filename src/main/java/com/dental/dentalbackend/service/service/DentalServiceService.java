package com.dental.dentalbackend.service.service;

import com.dental.dentalbackend.audit.service.AuditService;
import com.dental.dentalbackend.common.exception.DuplicateResourceException;
import com.dental.dentalbackend.common.exception.ResourceNotFoundException;
import com.dental.dentalbackend.service.dto.*;
import com.dental.dentalbackend.service.entity.DentalService;
import com.dental.dentalbackend.service.entity.ServiceCategory;
import com.dental.dentalbackend.service.repository.DentalServiceRepository;
import com.dental.dentalbackend.service.repository.ServiceCategoryRepository;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class DentalServiceService {

    private final DentalServiceRepository serviceRepository;
    private final ServiceCategoryRepository categoryRepository;
    private final AuditService auditService;

    // ══════════════════════════════════════════════════════════════
    // CATEGORIES
    // ══════════════════════════════════════════════════════════════

    @Transactional
    public CategoryResponse createCategory(CreateCategoryRequest request, UUID performedBy, HttpServletRequest httpRequest) {
        if (categoryRepository.existsByName(request.getName())) {
            throw new DuplicateResourceException("ServiceCategory", "name", request.getName());
        }

        String slug = request.getSlug() != null && !request.getSlug().isBlank()
                ? request.getSlug()
                : generateSlug(request.getName());

        if (categoryRepository.existsBySlug(slug)) {
            throw new DuplicateResourceException("ServiceCategory", "slug", slug);
        }

        ServiceCategory category = new ServiceCategory();
        category.setName(request.getName());
        category.setSlug(slug);
        category.setDescription(request.getDescription());
        category.setIconUrl(request.getIconUrl());
        category.setDisplayOrder(request.getDisplayOrder() != null ? request.getDisplayOrder() : 0);
        category.setActive(true);
        category = categoryRepository.save(category);

        auditService.log(performedBy, "CATEGORY_CREATED", "ServiceCategory", category.getId(),
                "Service category created: " + category.getName(), httpRequest);

        return mapCategoryResponse(category);
    }

    @Transactional(readOnly = true)
    public List<CategoryResponse> listAllCategories() {
        return categoryRepository.findAll(Sort.by("displayOrder").ascending())
                .stream().map(this::mapCategoryResponse).toList();
    }

    @Transactional(readOnly = true)
    public List<CategoryResponse> listActiveCategories() {
        return categoryRepository.findAllByActiveTrue(Sort.by("displayOrder").ascending())
                .stream().map(this::mapCategoryResponse).toList();
    }

    @Transactional
    public CategoryResponse updateCategory(UUID categoryId, UpdateCategoryRequest request,
                                            UUID performedBy, HttpServletRequest httpRequest) {
        ServiceCategory category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new ResourceNotFoundException("ServiceCategory", "id", categoryId));

        if (request.getName() != null) category.setName(request.getName());
        if (request.getDescription() != null) category.setDescription(request.getDescription());
        if (request.getIconUrl() != null) category.setIconUrl(request.getIconUrl());
        if (request.getDisplayOrder() != null) category.setDisplayOrder(request.getDisplayOrder());
        if (request.getActive() != null) category.setActive(request.getActive());
        category = categoryRepository.save(category);

        auditService.log(performedBy, "CATEGORY_UPDATED", "ServiceCategory", category.getId(),
                "Service category updated: " + category.getName(), httpRequest);

        return mapCategoryResponse(category);
    }

    // ══════════════════════════════════════════════════════════════
    // DENTAL SERVICES
    // ══════════════════════════════════════════════════════════════

    @Transactional
    public ServiceResponse createService(CreateServiceRequest request, UUID performedBy, HttpServletRequest httpRequest) {
        String slug = request.getSlug() != null && !request.getSlug().isBlank()
                ? request.getSlug()
                : generateSlug(request.getName());

        if (serviceRepository.existsBySlug(slug)) {
            throw new DuplicateResourceException("DentalService", "slug", slug);
        }

        ServiceCategory category = null;
        if (request.getCategoryId() != null) {
            category = categoryRepository.findById(request.getCategoryId())
                    .orElseThrow(() -> new ResourceNotFoundException("ServiceCategory", "id", request.getCategoryId()));
        }

        DentalService service = new DentalService();
        service.setCategory(category);
        service.setName(request.getName());
        service.setSlug(slug);
        service.setShortDescription(request.getShortDescription());
        service.setFullDescription(request.getFullDescription());
        service.setDurationMinutes(request.getDurationMinutes());
        service.setRequiredPaymentAmount(request.getRequiredPaymentAmount());
        service.setImageUrl(request.getImageUrl());
        service.setActive(true);
        service = serviceRepository.save(service);

        auditService.log(performedBy, "SERVICE_CREATED", "DentalService", service.getId(),
                "Service created: " + service.getName(), httpRequest);

        return mapServiceResponse(service);
    }

    @Transactional(readOnly = true)
    public Page<ServiceResponse> listAllServices(Pageable pageable) {
        return serviceRepository.findAll(pageable).map(this::mapServiceResponse);
    }

    @Transactional(readOnly = true)
    public Page<ServiceResponse> listActiveServices(Pageable pageable) {
        return serviceRepository.findAllByActiveTrue(pageable).map(this::mapServiceResponse);
    }

    @Transactional(readOnly = true)
    public ServiceResponse getServiceBySlug(String slug) {
        DentalService service = serviceRepository.findBySlug(slug)
                .orElseThrow(() -> new ResourceNotFoundException("DentalService", "slug", slug));
        if (!service.isActive()) {
            throw new ResourceNotFoundException("DentalService", "slug", slug);
        }
        return mapServiceResponse(service);
    }

    @Transactional
    public ServiceResponse updateService(UUID serviceId, UpdateServiceRequest request,
                                          UUID performedBy, HttpServletRequest httpRequest) {
        DentalService service = serviceRepository.findById(serviceId)
                .orElseThrow(() -> new ResourceNotFoundException("DentalService", "id", serviceId));

        if (request.getCategoryId() != null) {
            ServiceCategory category = categoryRepository.findById(request.getCategoryId())
                    .orElseThrow(() -> new ResourceNotFoundException("ServiceCategory", "id", request.getCategoryId()));
            service.setCategory(category);
        }

        if (request.getName() != null) service.setName(request.getName());
        if (request.getShortDescription() != null) service.setShortDescription(request.getShortDescription());
        if (request.getFullDescription() != null) service.setFullDescription(request.getFullDescription());
        if (request.getDurationMinutes() != null) service.setDurationMinutes(request.getDurationMinutes());
        if (request.getRequiredPaymentAmount() != null) service.setRequiredPaymentAmount(request.getRequiredPaymentAmount());
        if (request.getImageUrl() != null) service.setImageUrl(request.getImageUrl());
        if (request.getActive() != null) service.setActive(request.getActive());
        service = serviceRepository.save(service);

        auditService.log(performedBy, "SERVICE_UPDATED", "DentalService", service.getId(),
                "Service updated: " + service.getName(), httpRequest);

        return mapServiceResponse(service);
    }

    // ── Helpers ───────────────────────────────────────────────────

    private String generateSlug(String name) {
        return name.toLowerCase()
                .replaceAll("[^a-z0-9\\s-]", "")
                .replaceAll("\\s+", "-")
                .replaceAll("-+", "-")
                .replaceAll("^-|-$", "");
    }

    private CategoryResponse mapCategoryResponse(ServiceCategory category) {
        return CategoryResponse.builder()
                .id(category.getId())
                .name(category.getName())
                .slug(category.getSlug())
                .description(category.getDescription())
                .iconUrl(category.getIconUrl())
                .displayOrder(category.getDisplayOrder())
                .active(category.isActive())
                .createdAt(category.getCreatedAt())
                .build();
    }

    private ServiceResponse mapServiceResponse(DentalService service) {
        return ServiceResponse.builder()
                .id(service.getId())
                .categoryId(service.getCategory() != null ? service.getCategory().getId() : null)
                .categoryName(service.getCategory() != null ? service.getCategory().getName() : null)
                .name(service.getName())
                .slug(service.getSlug())
                .shortDescription(service.getShortDescription())
                .fullDescription(service.getFullDescription())
                .durationMinutes(service.getDurationMinutes())
                .requiredPaymentAmount(service.getRequiredPaymentAmount())
                .imageUrl(service.getImageUrl())
                .active(service.isActive())
                .createdAt(service.getCreatedAt())
                .build();
    }
}
