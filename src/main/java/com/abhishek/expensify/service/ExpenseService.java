package com.abhishek.expensify.service;

import com.abhishek.expensify.dto.ExpenseDto;
import com.abhishek.expensify.entity.CategoryEntity;
import com.abhishek.expensify.entity.ExpenseEntity;
import com.abhishek.expensify.entity.ProfileEntity;
import com.abhishek.expensify.repository.CategoryRepository;
import com.abhishek.expensify.repository.ExpenseRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;


import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ExpenseService {

    private final ExpenseRepository expenseRepository;
    private final CategoryRepository categoryRepository;
    private final ProfileService profileService;

    public ExpenseDto addExpense(ExpenseDto dto){
        ProfileEntity profile = profileService.getCurrentProfile();
        CategoryEntity category = categoryRepository.findById(dto.getCategoryId())
                .orElseThrow(() -> new RuntimeException("Category not found"));

        ExpenseEntity newExpense = toEntity(dto,profile,category);
        newExpense = expenseRepository.save(newExpense);
        return toDto(newExpense);

    }

    public List<ExpenseDto> getCurrentMonthExpensesForCurrentUser() {
        ProfileEntity profile = profileService.getCurrentProfile();
        LocalDate now = LocalDate.now();
        LocalDate startDate = now.withDayOfMonth(1);
        LocalDate endDate = now.withDayOfMonth(now.lengthOfMonth());

        // should be List<ExpenseEntity>, not List<ExpenseRepository>
        List<ExpenseEntity> list = expenseRepository.findByProfileIdAndDateBetween(
                profile.getId(), startDate, endDate
        );

        return list.stream()
                .map(this::toDto) // convert each entity to DTO
                .toList();
    }

    public void deleteExpense(Long expenseId){
        ProfileEntity profile = profileService.getCurrentProfile();
        ExpenseEntity entity = expenseRepository.findById(expenseId).orElseThrow(
                () -> new RuntimeException("Expense not found")
        );
        if(!entity.getProfile().getId().equals(profile.getId())){
            throw new RuntimeException("Unauthorized to delete this expense");


        }
        expenseRepository.delete(entity);



    }

    public List<ExpenseDto> getLatest5ExpensesForCurrentUser() {
        ProfileEntity profile = profileService.getCurrentProfile();
        List<ExpenseEntity> list = expenseRepository.findTop5ByProfileIdOrderByDateDesc(profile.getId());
        return list.stream().map(this::toDto).toList();
    }

    public BigDecimal getTotalExpenseForCurrentUser() {
        ProfileEntity profile =profileService.getCurrentProfile();
        BigDecimal total = expenseRepository.findTotalExpenseByProfileId(profile.getId());
        return total !=null ? total : BigDecimal.ZERO;

    }

    //Filter Expenses
    public List<ExpenseDto> filterExpenses(LocalDate startDate, LocalDate endDate , String keyword, Sort sort) {
        ProfileEntity profile = profileService.getCurrentProfile();
        List<ExpenseEntity> list = expenseRepository.findByProfileIdAndDateBetweenAndNameContainingIgnoreCase(profile.getId(),startDate,endDate,keyword,sort);
        return list.stream().map(this::toDto).toList();

    }

    //Notifications
    public List<ExpenseDto> getExpensesForUserOnDate(Long profileId, LocalDate date) {
       List<ExpenseEntity> list = expenseRepository.findByProfileIdAndDate(profileId,date);
       return list.stream().map(this::toDto).toList();

    }






    //Helper methods
    private ExpenseEntity toEntity(ExpenseDto dto, ProfileEntity profile, CategoryEntity category) {
        return ExpenseEntity.builder()
                .name(dto.getName())
                .icon(dto.getIcon())
                .amount(dto.getAmount())
                .date(dto.getDate())
                .profile(profile)
                .category(category)
                .build();
    }

    private ExpenseDto toDto(ExpenseEntity entity){
        return ExpenseDto.builder()
                .id(entity.getId())
                .name(entity.getName())
                .icon(entity.getIcon())
                .categoryId(entity.getCategory() !=null ? entity.getCategory().getId(): null)
                .categoryName(entity.getCategory() !=null ? entity.getCategory().getName(): "N/A")
                .amount(entity.getAmount())
                .date(entity.getDate())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }
}
