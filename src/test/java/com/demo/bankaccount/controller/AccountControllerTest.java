package com.demo.bankaccount.controller;

import com.demo.bankaccount.dto.CreateAccountRequest;
import com.demo.bankaccount.dto.TransactionRequest;
import com.demo.bankaccount.entity.Account;
import com.demo.bankaccount.exception.AccountNotFoundException;
import com.demo.bankaccount.exception.InsufficientFundsException;
import com.demo.bankaccount.service.AccountService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.resttestclient.autoconfigure.AutoConfigureRestTestClient;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.client.RestTestClient;

import java.math.BigDecimal;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@WebMvcTest(AccountController.class)
@AutoConfigureRestTestClient
public class AccountControllerTest {
    @Autowired
    private RestTestClient restTestClient;

    @MockitoBean
    private AccountService accountService;

    @Test
    void whenCreateAccount_thenReturn201() throws Exception {
        Account saved = new Account();
        saved.setOwner("Maria");
        saved.setBalance(BigDecimal.TEN);

        when(accountService.createAccount(any(), any())).thenReturn(saved);

        CreateAccountRequest request = new CreateAccountRequest("Maria", BigDecimal.TEN);

        restTestClient.post().uri("/api/accounts")
                .contentType(MediaType.APPLICATION_JSON)
                .body(request)
                .exchange()
                .expectStatus().isCreated()
                .expectBody()
                .jsonPath("$.owner").isEqualTo("Maria");
    }

    @Test
    void whenAccountNotFound_thenReturn404() {
        UUID id = UUID.randomUUID();
        when(accountService.getAccount(id))
                .thenThrow(new AccountNotFoundException(id));

        restTestClient.get().uri("/api/accounts" + id)
                .exchange() // is the send request and is called exchange because is a request-response exchange
                .expectStatus().isNotFound();
    }

    @Test
    void whenWithdrawInsufficientFounds_thenReturn400() {
        UUID id = UUID.randomUUID();
        when(accountService.withdraw(eq(id), any()))
                .thenThrow(new InsufficientFundsException(BigDecimal.TEN, new BigDecimal("200")));

        TransactionRequest request = new TransactionRequest(new BigDecimal("200"));

        restTestClient.post().uri("/api/accounts/" + id + "/withdraw")
                .contentType(MediaType.APPLICATION_JSON)
                .body(request)
                .exchange()
                .expectStatus().isBadRequest();
    }
}
