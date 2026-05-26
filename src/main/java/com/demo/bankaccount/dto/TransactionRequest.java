package com.demo.bankaccount.dto;

import java.math.BigDecimal;

public record TransactionRequest(BigDecimal amount) {
}
