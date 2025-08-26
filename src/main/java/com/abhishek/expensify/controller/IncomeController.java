package com.abhishek.expensify.controller;

import com.abhishek.expensify.dto.IncomeDto;
import com.abhishek.expensify.service.IncomeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/incomes")
public class IncomeController {

    private final IncomeService incomeService;

    // ✅ Add new income
    @PostMapping
    public ResponseEntity<IncomeDto> addIncome(@RequestBody IncomeDto incomeDto) {
        IncomeDto createdIncome = incomeService.addIncome(incomeDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdIncome);
    }

    // ✅ Get current month incomes for current user
    @GetMapping
    public ResponseEntity<List<IncomeDto>> getIncomes() {
        List<IncomeDto> incomes = incomeService.getCurrentMonthIncomesForCurrentUser();
        return ResponseEntity.ok(incomes);
    }

    // ✅ Delete income by id
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteIncome(@PathVariable Long id) {
        incomeService.deleteIncome(id);
        return ResponseEntity.noContent().build();
    }
}
