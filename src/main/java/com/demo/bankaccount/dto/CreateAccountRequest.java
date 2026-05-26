package com.demo.bankaccount.dto;

import java.math.BigDecimal;

public record CreateAccountRequest(String owner, BigDecimal initialBalance) {
}
