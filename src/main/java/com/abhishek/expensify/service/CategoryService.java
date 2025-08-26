package com.abhishek.expensify.service;

import com.abhishek.expensify.dto.CategoryDto;
import com.abhishek.expensify.entity.CategoryEntity;
import com.abhishek.expensify.entity.ProfileEntity;
import com.abhishek.expensify.repository.CategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CategoryService {

    private final ProfileService profileService;
    private final CategoryRepository categoryRepository;

    public CategoryDto saveCategory(CategoryDto categoryDto){
        ProfileEntity profile = profileService.getCurrentProfile();

        // Trim and lowercase the name to avoid duplicates due to spaces or casing
        String categoryName = categoryDto.getName().trim();

        System.out.println("Checking existence for category name='" + categoryName
                + "' and profileId=" + profile.getId());

        if(categoryRepository.existsByNameAndProfileId(categoryName, profile.getId())){
            throw new ResponseStatusException(HttpStatus.CONFLICT,"Category with this name already exists");
        }

        // Update DTO with trimmed name
        categoryDto.setName(categoryName);

        CategoryEntity newCategory = toEntity(categoryDto, profile);
        newCategory = categoryRepository.save(newCategory);

        System.out.println("Saved new category: " + newCategory);

        return toDto(newCategory);
    }

    //Get categories for current user
    public List<CategoryDto> getCategoriesForCurrentUser(){
        ProfileEntity profile = profileService.getCurrentProfile();
        List<CategoryEntity> categoryEntities = categoryRepository.findByProfileId(profile.getId());
        return categoryEntities.stream().map(this::toDto).toList();
    }

    // Get Categories by type for current user
    public List<CategoryDto> getCategoriesByTypeForCurrentUser(String type){
        ProfileEntity profile = profileService.getCurrentProfile();
         List<CategoryEntity> entities = categoryRepository.findByTypeAndProfileId(type,profile.getId());
         return entities.stream().map(this::toDto).toList();

    }

    public CategoryDto updateCategory(Long categoryId, CategoryDto dto) {
        ProfileEntity profile = profileService.getCurrentProfile();
        CategoryEntity existingCategory = categoryRepository.findByIdAndProfileId(categoryId,profile.getId()).orElseThrow(
                () -> new RuntimeException("Category not found or not accessible")
        );
        existingCategory.setName(dto.getName());
        existingCategory.setIcon(dto.getIcon());
        existingCategory.setType(dto.getType());
        existingCategory = categoryRepository.save(existingCategory);
        return toDto(existingCategory);


    }

    // Helper methods
    private CategoryEntity toEntity(CategoryDto categoryDto, ProfileEntity profileEntity) {
        return CategoryEntity.builder()
                .name(categoryDto.getName())
                .icon(categoryDto.getIcon())
                .profile(profileEntity)
                .type(categoryDto.getType())
                .build();
    }

    private CategoryDto toDto(CategoryEntity categoryEntity){
        return CategoryDto.builder()
                .id(categoryEntity.getId())
                .profileId(categoryEntity.getProfile() != null ?  categoryEntity.getProfile().getId(): null)
                .name(categoryEntity.getName())
                .icon(categoryEntity.getIcon())
                .type(categoryEntity.getType())
                .createdAt(categoryEntity.getCreatedAt())
                .updatedAt(categoryEntity.getUpdatedAt())
                .build();
    }
}
