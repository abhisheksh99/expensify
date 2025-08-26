package com.abhishek.expensify.service;

import com.abhishek.expensify.dto.ExpenseDto;
import com.abhishek.expensify.dto.IncomeDto;
import com.abhishek.expensify.dto.RecentTransactionDto;
import com.abhishek.expensify.entity.ProfileEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
public class DashboardService {

    private final IncomeService incomeService;
    private final ExpenseService expenseService;
    private final ProfileService profileService;

    public Map<String, Object> getDashboardData() {
        ProfileEntity profile = profileService.getCurrentProfile();
        Map<String, Object> returnValue = new LinkedHashMap<>();

        List<IncomeDto> latestIncomes = incomeService.getLatest5IncomesForCurrentUser();
        List<ExpenseDto> latestExpenses = expenseService.getLatest5ExpensesForCurrentUser();

        // Merge incomes + expenses into transactions
        List<RecentTransactionDto> recentTransactions = Stream.concat(
                        latestIncomes.stream().map(
                                income -> RecentTransactionDto.builder()
                                        .id(income.getId())
                                        .profileId(profile.getId())
                                        .icon(income.getIcon())
                                        .name(income.getName())
                                        .amount(income.getAmount())
                                        .date(income.getDate())
                                        .createdAt(income.getCreatedAt())
                                        .updatedAt(income.getUpdatedAt())
                                        .type("income")
                                        .build()
                        ),
                        latestExpenses.stream().map(
                                expense -> RecentTransactionDto.builder()
                                        .id(expense.getId())
                                        .profileId(profile.getId())
                                        .icon(expense.getIcon())
                                        .name(expense.getName())
                                        .amount(expense.getAmount())
                                        .date(expense.getDate())
                                        .createdAt(expense.getCreatedAt())
                                        .updatedAt(expense.getUpdatedAt())
                                        .type("expense")
                                        .build()
                        )
                )
                // ✅ Sort by date (latest first)
                .sorted(Comparator.comparing(RecentTransactionDto::getDate).reversed())
                .collect(Collectors.toList());

        // Add to return map
        returnValue.put("totalBalance",
                incomeService.getTotalIncomeForCurrentUser()
                        .subtract(expenseService.getTotalExpenseForCurrentUser()));
        returnValue.put("totalIncome", incomeService.getTotalIncomeForCurrentUser());
        returnValue.put("totalExpense", expenseService.getTotalExpenseForCurrentUser());
        returnValue.put("recentTransactions", recentTransactions);
        returnValue.put("latestIncomes", latestIncomes);
        returnValue.put("latestExpenses", latestExpenses);

        return returnValue;
    }
}
