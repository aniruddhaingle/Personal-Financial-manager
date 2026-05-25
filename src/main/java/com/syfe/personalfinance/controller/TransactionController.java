package com.syfe.personalfinance.controller;

import com.syfe.personalfinance.dto.AuthDto;
import com.syfe.personalfinance.dto.TransactionDto;
import com.syfe.personalfinance.dto.TransactionDto.TransactionResponse;
import com.syfe.personalfinance.enums.CategoryType;
import com.syfe.personalfinance.service.TransactionService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/transactions")
public class TransactionController {

    private final TransactionService transactionService;

    public TransactionController(TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    @PostMapping
    public ResponseEntity<TransactionResponse> createTransaction(@Valid @RequestBody TransactionDto.CreateTransactionRequest request) {
        TransactionResponse responseData = transactionService.createTransaction(request);
        return new ResponseEntity<>(responseData, HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<TransactionDto.TransactionListResponse> getTransactions(
            @RequestParam(required = false) LocalDate startDate,
            @RequestParam(required = false) LocalDate endDate,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) CategoryType categoryType) {

        List<TransactionResponse> responseData = transactionService.getTransactions(
                startDate, endDate, category, categoryId, categoryType);

        TransactionDto.TransactionListResponse response = TransactionDto.TransactionListResponse.builder()
                .transactions(responseData)
                .build();

        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<TransactionResponse> updateTransaction(
            @PathVariable Long id,
            @Valid @RequestBody TransactionDto.UpdateTransactionRequest request) {

        TransactionResponse responseData = transactionService.updateTransaction(id, request);
        return ResponseEntity.ok(responseData);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<AuthDto.SimpleMessageResponse> deleteTransaction(@PathVariable Long id) {
        transactionService.deleteTransaction(id);
        AuthDto.SimpleMessageResponse response = AuthDto.SimpleMessageResponse.builder()
                .message("Transaction deleted successfully")
                .build();
        return ResponseEntity.ok(response);
    }
}
