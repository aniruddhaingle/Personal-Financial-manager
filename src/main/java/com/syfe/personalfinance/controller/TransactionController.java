package com.syfe.personalfinance.controller;

import com.syfe.personalfinance.dto.ApiResponse;
import com.syfe.personalfinance.dto.TransactionDto;
import com.syfe.personalfinance.dto.TransactionDto.TransactionResponse;
import com.syfe.personalfinance.enums.CategoryType;
import com.syfe.personalfinance.service.TransactionService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/transactions")
public class TransactionController {

    private final TransactionService transactionService;

    // Constructor injection only
    public TransactionController(TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    @PostMapping
    public ResponseEntity<ApiResponse<TransactionResponse>> createTransaction(@Valid @RequestBody TransactionDto.CreateTransactionRequest request) {
        TransactionResponse responseData = transactionService.createTransaction(request);
        ApiResponse<TransactionResponse> response = ApiResponse.<TransactionResponse>builder()
                .success(true)
                .message("Transaction created successfully")
                .data(responseData)
                .timestamp(LocalDateTime.now())
                .build();
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<ApiResponse<Page<TransactionResponse>>> getTransactions(
            @RequestParam(required = false) LocalDate startDate,
            @RequestParam(required = false) LocalDate endDate,
            @RequestParam(required = false) String categoryName,
            @RequestParam(required = false) CategoryType categoryType,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "date,desc") String sort) {

        // Build sorting criteria manually for precise query controls
        String[] sortParams = sort.split(",");
        String sortProperty = sortParams[0];
        Sort.Direction direction = Sort.Direction.DESC;
        if (sortParams.length > 1 && "asc".equalsIgnoreCase(sortParams[1])) {
            direction = Sort.Direction.ASC;
        }

        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortProperty));

        Page<TransactionResponse> responseData = transactionService.getTransactions(
                startDate, endDate, categoryName, categoryType, pageable);

        ApiResponse<Page<TransactionResponse>> response = ApiResponse.<Page<TransactionResponse>>builder()
                .success(true)
                .message("Transactions retrieved successfully")
                .data(responseData)
                .timestamp(LocalDateTime.now())
                .build();

        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<TransactionResponse>> updateTransaction(
            @PathVariable Long id,
            @Valid @RequestBody TransactionDto.UpdateTransactionRequest request) {

        TransactionResponse responseData = transactionService.updateTransaction(id, request);
        ApiResponse<TransactionResponse> response = ApiResponse.<TransactionResponse>builder()
                .success(true)
                .message("Transaction updated successfully")
                .data(responseData)
                .timestamp(LocalDateTime.now())
                .build();

        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteTransaction(@PathVariable Long id) {
        transactionService.deleteTransaction(id);
        ApiResponse<Void> response = ApiResponse.<Void>builder()
                .success(true)
                .message("Transaction deleted successfully")
                .data(null)
                .timestamp(LocalDateTime.now())
                .build();

        return ResponseEntity.ok(response);
    }
}
