package com.demo.bankaccount.service;

import com.demo.bankaccount.entity.Account;
import com.demo.bankaccount.exception.AccountNotFoundException;
import com.demo.bankaccount.exception.InsufficientFundsException;
import com.demo.bankaccount.repository.AccountRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.UUID;

@Service
public class AccountServiceImpl implements AccountService {
    private final AccountRepository accountRepository;

    public AccountServiceImpl(AccountRepository accountRepository) {
        this.accountRepository = accountRepository;
    }

    @Override
    public Account createAccount(String owner, BigDecimal initialBalance) {
        if (owner == null || owner.isBlank()) {
            throw new IllegalArgumentException("Owner name cannot be empty");
        }

        if (initialBalance == null) {
            throw new IllegalArgumentException("Initial balance cannot be null");
        }

        if (initialBalance.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Initial balance cannot be negative");
        }

        Account account = new Account();
        account.setOwner(owner);
        account.setBalance(initialBalance);
        return accountRepository.save(account);
    }

    @Override
    public Account deposit(UUID id, BigDecimal amount) {
        if(amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Deposit must be positive");
        }

        Account account = accountRepository.findById(id).orElseThrow(() -> new AccountNotFoundException(id));

        account.setBalance(account.getBalance().add(amount));
        return accountRepository.save(account);
    }

    @Override
    public Account withdraw(UUID id, BigDecimal amount) {
        if(amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Withdrawal amount must be positive");
        }

        Account account = accountRepository.findById(id).orElseThrow(() -> new AccountNotFoundException(id));

        if(amount.compareTo(account.getBalance()) > 0) {
            throw new InsufficientFundsException(account.getBalance(), amount);
        }

        account.setBalance(account.getBalance().subtract(amount));
        return accountRepository.save(account);
    }

    @Override
    public Account getAccount(UUID id) {
        return accountRepository.findById(id).orElseThrow(() -> new AccountNotFoundException(id));
    }
}
