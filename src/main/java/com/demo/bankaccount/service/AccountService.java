package com.demo.bankaccount.service;

import com.demo.bankaccount.entity.Account;

import java.math.BigDecimal;
import java.util.UUID;

public interface AccountService {
    Account createAccount(String owner, BigDecimal initialBalance);
    Account deposit(UUID id, BigDecimal amount);
    Account withdraw(UUID id, BigDecimal amount);
    Account getAccount(UUID id);
}
