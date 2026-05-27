package com.demo.bankaccount;

import com.demo.bankaccount.dto.CreateAccountRequest;
import com.demo.bankaccount.dto.TransactionRequest;
import com.demo.bankaccount.entity.Account;
import com.demo.bankaccount.repository.AccountRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.resttestclient.autoconfigure.AutoConfigureRestTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.client.RestTestClient;

import java.math.BigDecimal;
import java.util.UUID;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@SpringBootTest
@AutoConfigureRestTestClient
public class AccountIntegrationTest {
    @Autowired
    private RestTestClient restTestClient;

    @Autowired
    private AccountRepository accountRepository;

    @AfterEach
    void tearDown() {
        accountRepository.deleteAll();
    }

    @Test
    void whenCreateAndWithdraw_theBalanceIsCorrect() {
        // arrange
        CreateAccountRequest request = new CreateAccountRequest("Maria", new BigDecimal("100"));

        UUID id = restTestClient.post().uri("/api/accounts")
                .contentType(MediaType.APPLICATION_JSON)
                .body(request)
                .exchange()
                .expectStatus().isCreated()
                .expectBody(Account.class)
                .returnResult()
                .getResponseBody()
                .getId();

        // act
        restTestClient.post().uri("/api/accounts/" + id + "/withdraw")
                .contentType(MediaType.APPLICATION_JSON)
                .body(new TransactionRequest(new BigDecimal("40")))
                .exchange()
                .expectStatus().isOk();

        // assert
        Account saved = accountRepository.findById(id).orElseThrow();
        assertThat(saved.getBalance()).isEqualByComparingTo(new BigDecimal("60"));
    }
}

