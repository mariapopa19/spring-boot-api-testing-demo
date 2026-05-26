package com.demo.bankaccount.controller;

import com.demo.bankaccount.dto.CreateAccountRequest;
import com.demo.bankaccount.dto.TransactionRequest;
import com.demo.bankaccount.entity.Account;
import com.demo.bankaccount.service.AccountService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/accounts")
public class AccountController {
    private final AccountService accountService;

    public AccountController(AccountService accountService) {
        this.accountService = accountService;
    }

    @PostMapping
    public ResponseEntity<Account> createAccount(@RequestBody CreateAccountRequest request) {
        Account account = accountService.createAccount(request.owner(), request.initialBalance());
        return ResponseEntity.status(HttpStatus.CREATED).body(account);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Account> getAccount(@PathVariable UUID id) {
        Account account = accountService.getAccount(id);
        return ResponseEntity.ok(account);
    }

    @PostMapping("/{id}/deposit")
    public ResponseEntity<Account> deposit(@PathVariable UUID id, @RequestBody TransactionRequest request) {
        Account account = accountService.deposit(id, request.amount());
        return ResponseEntity.ok(account);
    }

    @PostMapping("/{id}/withdraw")
    public ResponseEntity<Account> withdraw(@PathVariable UUID id, @RequestBody TransactionRequest request) {
        Account account = accountService.withdraw(id, request.amount());
        return ResponseEntity.ok(account);
    }
}
